package remotecontrolbackend.netty_part.chunked_part.file_handler_part


import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettySubComponent.Companion.FILE_HANDLER_LITERAL
import remotecontrolbackend.file_service_part.FileService
import remotecontrolbackend.netty_part.chunked_part.ChunkWorkModeHandler
import kotlin.jvm.Throws
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.DispatchResult.Companion.FileDirective
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.content_handler.FileBodyHandler
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.content_handler.FileBodyHandler.Companion.FILE_BODY_HANDLER_LITERAL
import remotecontrolbackend.netty_part.utils.SpecificChain
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

@SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
abstract class AbstractFileHandler(
    val nettyCoroutineContext: CoroutineContext,
    val fileService: FileService
) : ChunkWorkModeHandler() {
    companion object {
        const val FILE_QUERY = "file"
       private val logger = LogManager.getLogger()
    }

    override val handlerQuery: String = FILE_QUERY
    override val handlerDescription = FILE_HANDLER_LITERAL

    val handlerScope = CoroutineScope(nettyCoroutineContext +
                         CoroutineExceptionHandler { context,exc ->throw RuntimeException(exc) }+
            SupervisorJob(nettyCoroutineContext.job)
    )



    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        logger.debug("Handler removed canceling coroutineScope")
        handlerScope.coroutineContext.job.cancelChildren()
    }

    protected fun HttpRequest.extractPathVariable(): String {
        val queryStringDecoder = QueryStringDecoder(this.uri())
        return queryStringDecoder.path().lowercase().substringAfter("file/")
    }

    @Throws(IllegalStateException::class)
    protected suspend fun HttpRequest.dispatchRequest(): DispatchResult {
        val currentMethod = method()
        val pathVariable = this.extractPathVariable()

        val directives = FileDirective.values()
        val resultDirectives = directives
            .filter { currentMethod in it.compatMethods }
            .filter { it.pathLiteralRestriction.invoke(pathVariable) }
        return when (resultDirectives.count()) {
            0 -> {
                NotConsistentDispatch("Not Consistent Request")
            }
            1 -> {
                val finDirective = resultDirectives.get(0)
                when (finDirective) {
                    FileDirective.AVAILABLEFILES -> AvailableFilesDispatch()
                    FileDirective.GETLISTEDFILES -> GetListedFilesDispatch()
                    FileDirective.POSTFILE -> PostFileDispatch(pathVariable)
                    else -> throw IllegalStateException("Very unlikely IllegalState")
                }
            }

            else -> {
                logger.error("Extracted more than 1 directive: ${resultDirectives}")
                throw IllegalStateException("More than one directive exctracted")
            }

        }
    }

    protected fun ChannelHandlerContext.appendBodyHandler(fileService: FileService, dispatchResult: DispatchResult) {
        val pipeline = this.pipeline()
        if (pipeline.get(FileBodyHandler::class.java) != null) {
            throw IllegalStateException("Trying to add more than one BodyHandler to pipeline")
        }
        pipeline.addAfter(
            FILE_HANDLER_LITERAL, FILE_BODY_HANDLER_LITERAL,
            FileBodyHandler.provideAppropriateContentHandler(fileService, dispatchResult, nettyCoroutineContext)
        )
    }

    /** Processes requests, that doesn't require RqBody processing and writes apropriate response message to channel*/
    protected suspend fun ChannelHandlerContext.processRequestOnly(
        fileService: FileService,
        moshi: Moshi,
        dispatchResult: DispatchResult
    ) {

        withContext(handlerScope.coroutineContext) {
            val pipeline = this@processRequestOnly.pipeline()
            when (dispatchResult) {
                is AvailableFilesDispatch -> {
                    val repoPaths = fileService.getAllPaths()
                    val json = moshi
                        .adapter<Collection<Path>>(Types.newParameterizedType(Collection::class.java, Path::class.java))
                        .toJson(repoPaths)
                    val jsonBuf = this@processRequestOnly.alloc().buffer().also {
                        it.writeCharSequence(
                            json,
                            StandardCharsets.UTF_8
                        )
                    }
                    val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, jsonBuf)
                    HttpUtil.setContentLength(response,jsonBuf.readableBytes().toLong())
                    this@processRequestOnly.writeAndFlush(response)


                }
                else -> {
                    throw IllegalStateException("There most be no Not RequestOnlyDispatchResults")
                }
            }
        }
    }

}

