package remotecontrolbackend.netty_part.chunked_part.file_handler_part.content_handler

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.DefaultHttpContent
import io.netty.handler.codec.http.HttpChunkedInput
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.LastHttpContent
import io.netty.handler.stream.ChunkedStream
import io.netty.handler.stream.ChunkedWriteHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.produce
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.file_service_part.FileService
import remotecontrolbackend.moshi.PathAdapter
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.DispatchResult
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.GetListedFilesDispatch
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.PostFileDispatch
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.constructChunkedFileResponse
import remotecontrolbackend.netty_part.send200Response
import remotecontrolbackend.netty_part.utils.Exception404
import remotecontrolbackend.netty_part.utils.SpecificChain
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.channels.Channels
import java.nio.channels.WritableByteChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

@SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
sealed class FileBodyHandler(
    val fileService: FileService,
    open val dispatchResult: DispatchResult,
    val coroutineContext: CoroutineContext
) : SimpleChannelInboundHandler<HttpContent>() {
    val fileBodyHandlerScope = CoroutineScope(coroutineContext +
            CoroutineExceptionHandler { context, exc -> channelHandlerContext.fireExceptionCaught(exc) } +
            SupervisorJob(coroutineContext.job))
    lateinit var channelHandlerContext: ChannelHandlerContext
    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        channelHandlerContext = ctx!!
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        fileBodyHandlerScope.coroutineContext.job.cancelChildren()
    }

    companion object {
        const val FILE_BODY_HANDLER_LITERAL = "FILE_BODY_HANDLER"
        val logger = LogManager.getLogger()

        // все наследники этого класса будут создаваться тольько через этот статик метод
        fun provideAppropriateContentHandler(
            fileService: FileService,
            dispatchResult: DispatchResult,
            coroutineContext: CoroutineContext
        ): FileBodyHandler {
            return when (dispatchResult) {
                is PostFileDispatch -> {
                    logger.debug("Providing PostFileHandler")
                    PostFileHandler(fileService, dispatchResult, coroutineContext)
                }

                is GetListedFilesDispatch -> {
                    logger.debug("Providing GetListedFilesBodyHandler")
                    GetListedFilesBodyHandler(fileService, dispatchResult, coroutineContext)
                }
                else -> {
                    throw IllegalStateException(
                        "Every other possibility most be cut off on previous stages," +
                                "there most be no dispatch results except[PostfileDispatch and GetListedFilesDispatch]"
                    )
                }

            }
        }
    }


    //TODO Ухх как потно это нужно проверить
    @SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
    private class PostFileHandler(
        fileService: FileService,
        override val dispatchResult: PostFileDispatch,
        coroutineContext: CoroutineContext
    ) :
        FileBodyHandler(fileService, dispatchResult, coroutineContext) {
        lateinit var pipeIn: PipedInputStream
        lateinit var pipeOutChannel: WritableByteChannel
        lateinit var context: ChannelHandlerContext
        lateinit var actorJob: Job
        var writeActor = fileBodyHandlerScope.actor<HttpContent>(capacity = 1) {
            actorJob = this@actor.coroutineContext.job
            for (content in channel) {
                logger.debug("Another msg in Actor: ${content::class}")
                yield()
                when (content) {
                    is DefaultHttpContent -> {
                        logger.debug(
                            "POSTFILE_BODYHANDLER received DefaultHttpContent " +
                                    "with size of ${content.content().readableBytes()}"
                        )

                        logger.debug("Before writeChunk in Actor")
                        writeChunk(content.content())
                        logger.debug("After writeChunk in Actor")
                        content.content().release()
                    }
                    is LastHttpContent -> {
                        logger.debug("closing savechannel")
                        writeChunk(content.content())
                        content.content().release()
                        context.send200Response()
                        pipeOutChannel.close()
                        break
                    }
                }
            }
        }


        override fun handlerAdded(ctx: ChannelHandlerContext?) {
            super.handlerAdded(ctx)
            context = ctx!!
            fileBodyHandlerScope.launch {
                pipeIn = PipedInputStream()
                pipeOutChannel = Channels.newChannel(PipedOutputStream(pipeIn))
                fileService.saveFile(pipeIn, dispatchResult.fileName)

                logger.debug("AFTER HANDLER ADDED!!!!!!!!!")
            }
        }

        suspend fun CoroutineScope.writeChunk(msg: ByteBuf) {//TODO
            if (!pipeOutChannel.isOpen) {
                throw IllegalStateException("File out Channel is closed")
            }
            logger.debug("In chhunk write")
            val nioBuf = msg.nioBuffer()
            while (nioBuf.hasRemaining()) {
                yield()
                pipeOutChannel.write(nioBuf)
            }
            logger.debug("POSTFILE_BODYHANDLER end of ChunkWrite")
        }

        override fun channelRead0(ctx: ChannelHandlerContext?, msg: HttpContent?) {
            msg?.let {
                runBlocking {

                    when (it) {
                        is DefaultHttpContent -> {
                            it.retain()
                            logger.debug("Before sending another msg to actor: $msg     ${msg::class.qualifiedName}")
                            writeActor.send(it)
                            logger.debug("After sending another msg to actor")
                        }

                        is LastHttpContent -> {
                            logger.debug("Last content consumed")
                            it.retain()
                            logger.debug("Before sending another msg to actor: $msg     ${msg::class.qualifiedName}")
                            writeActor.send(it)
                            logger.debug("Start waiting for actore to complete")
                            actorJob.join()
                        }
                    }
                }
            }

        }
    }


    @SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
    private class GetListedFilesBodyHandler(
        fileService: FileService,
        dispatchResult: GetListedFilesDispatch,
        coroutineContext: CoroutineContext
    ) : FileBodyHandler(fileService, dispatchResult, coroutineContext) {
        val moshi: Moshi = Moshi.Builder().add(PathAdapter()).build()
        val pathCollectionAdapter =
            moshi.adapter<Set<Path>>(Types.newParameterizedType(Set::class.java, Path::class.java))
        val stringBuffer = StringBuffer()

        override fun handlerAdded(ctx: ChannelHandlerContext?) {
            logger.debug("${this::class.java.simpleName} added to pipeline that conatains: ${ctx?.pipeline()}")
            super.handlerAdded(ctx)
            ctx?.let {
                it.pipeline().addAfter(
                    FILE_BODY_HANDLER_LITERAL,
                    AnnotatedChunkedWriter.CHUNKED_WRITE_HANDLER_LITERAL,
                    AnnotatedChunkedWriter()
                )
                logger.debug(
                    "ChunkedWriteHandler added, check: ${
                        it.pipeline().get(AnnotatedChunkedWriter.CHUNKED_WRITE_HANDLER_LITERAL) != null
                    }"
                )
            }
        }

        override fun channelRead0(ctx: ChannelHandlerContext?, msg: HttpContent?) {
            msg.let { message ->
                if (message is DefaultHttpContent) {
                    message.content().toString(StandardCharsets.UTF_8).let {
                        logger.debug("Received msg ith body containing {$it}")
                        stringBuffer.append(it)
                    }
                } else {
                    runBlocking {
                        fileBodyHandlerScope.launch {
                            if (!fileService.initialized) {
                                fileService.reInitializeFileService()
                                delay(200)
                                if (!fileService.initialized) {
                                    throw IllegalStateException("File Service most be initialized")
                                }
                            }
                            val resultingJson = stringBuffer.toString()
                            val paths = kotlin.runCatching { pathCollectionAdapter.fromJson(resultingJson)!! }
                                .getOrElse { throw Exception404("Got invalid JSON body") }
                            constructChunkedFileResponse(paths).let {
                                logger.debug("Writing response headers")
                                ctx!!.writeAndFlush(it)
                            }
                            fileService.provideFileStream(paths).let {
                                logger.debug("Writing response body stream")
                                ctx!!.channel().writeAndFlush(HttpChunkedInput(ChunkedStream(it)))
                            }
                        }.join()
                    }


                }
            }
        }


        @SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
        private class AnnotatedChunkedWriter : ChunkedWriteHandler() {
            companion object {
                const val CHUNKED_WRITE_HANDLER_LITERAL = "CHUNKED_WRITE_HANDLER"
            }
        }

    }


}
