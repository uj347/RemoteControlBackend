package remotecontrolbackend.netty_part

import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent.Companion.TRANSFER_ENCODING_INTERCEPTOR_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.CHUNKED_REQUEST_ROUTER_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.HTTP_AGGREGATOR_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.HTTP_CODEC_LITERAL
import remotecontrolbackend.netty_part.chunked_part.chunked_request_router_part.AbstractChunkedRequestRouter
import remotecontrolbackend.netty_part.full_request_part.full_request_router_part.AbstractFullRequestRouter
import remotecontrolbackend.netty_part.utils.ChunkedChain
import remotecontrolbackend.netty_part.utils.FullRequestChain
import javax.inject.Inject
import kotlin.reflect.full.hasAnnotation


@NettyScope
@Sharable
class TransferEncodingInterceptor @Inject constructor() : ChannelInboundHandlerAdapter() {


    @Inject
    lateinit var chunnkedRequestRouter: AbstractChunkedRequestRouter

    @Inject
    lateinit var fullRequestRouter: AbstractFullRequestRouter


    companion object {
        val logger = LogManager.getLogger()
        val handlerDescription = TRANSFER_ENCODING_INTERCEPTOR_LITERAL

//        val handlersToRemoveForChunked = setOf<String>(
//            HTTP_AGGREGATOR_LITERAL,
//            FULL_REQUEST_ROUTER_LITERAL
//        )


    }

    val handlerDescription
        get() = Companion.handlerDescription


    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        msg?.let { message ->
            if (message is HttpRequest) {
                logger.debug("Chunked interceptor received request: $message")
                if (HttpUtil.isTransferEncodingChunked(message)) {

                    logger.debug("Encoding of msg is CHUNKED, interceptor is reconfiguring pipeline")
                    ctx?.let { context ->
                        val pipeIter = context.pipeline().iterator()
                        var hasChunkedRouter = false
                        while (pipeIter.hasNext()) {

                            val nextHandlerEntry = pipeIter.next()
                            logger.debug("In Pipeline iterator chekkin ${nextHandlerEntry.value::class.simpleName}")

                            val handlerIsForFullChain = nextHandlerEntry.value::class.hasAnnotation<FullRequestChain>()

                            if (nextHandlerEntry.value is AbstractChunkedRequestRouter) {
                                hasChunkedRouter = true
                            }

                            if (handlerIsForFullChain || (nextHandlerEntry.value is HttpObjectAggregator)) {
                                logger.debug(
                                    "Removing FullChainHandler of class " +
                                            "${nextHandlerEntry.value::class.simpleName} from the pipeline "
                                )
                                pipeIter.remove()
                            }
                        }

                        if (!hasChunkedRouter) {
                            context.pipeline().addAfter(
                                TRANSFER_ENCODING_INTERCEPTOR_LITERAL,
                                CHUNKED_REQUEST_ROUTER_LITERAL,
                                chunnkedRequestRouter
                            )
                        }
                        logger.debug("Firing read on the chunked-modified pipeline")
                        //Раньше здесь было обращение к пайплайну в целом
                        context.fireChannelRead(message)
                    }
                } else {
                    logger.debug("Encoding of msg is NOT-CHUNKED, interceptor is reconfiguring pipeline")
                    ctx?.let { context ->
                        val pipeIter = context.pipeline().iterator()
                        var hasFullRouter = false
                        var hasAggregator = false
                        while (pipeIter.hasNext()) {

                            val nextHandlerEntry = pipeIter.next()
                            logger.debug("In Pipeline iterator chekkin ${nextHandlerEntry.value::class.simpleName}")

                            val handlerIsForChunkChain = nextHandlerEntry.value::class.hasAnnotation<ChunkedChain>()

                            if (nextHandlerEntry.value is AbstractFullRequestRouter) {
                                hasFullRouter = true
                            }
                            if (nextHandlerEntry.value is HttpObjectAggregator) {
                                hasAggregator = true
                            }

                            if (handlerIsForChunkChain) {
                                logger.debug(
                                    "Removing ChunkedHandler of class " +
                                            "${nextHandlerEntry.value::class.simpleName} from the pipeline "
                                )
                                pipeIter.remove()
                            }
                        }

                        if (!hasFullRouter) {
                            context.pipeline().addAfter(
                                TRANSFER_ENCODING_INTERCEPTOR_LITERAL,
                                fullRequestRouter.handlerDescription,
                                fullRequestRouter
                            )
                        }
                        if (!hasAggregator) {
                            context.pipeline().addAfter(
                                HTTP_CODEC_LITERAL,
                                HTTP_AGGREGATOR_LITERAL,
                                HttpObjectAggregator(Int.MAX_VALUE)
                            )
                        }

                        logger.debug("Firing channeel read in FullRequest-configured pipeline")
                        context.fireChannelRead(message)
                    }
                }
            } else {
                logger.debug("Passing NON HTTP-REQUEST message trough without modification of pipeline")
                ctx?.fireChannelRead(message)
            }
        }
    }
}
