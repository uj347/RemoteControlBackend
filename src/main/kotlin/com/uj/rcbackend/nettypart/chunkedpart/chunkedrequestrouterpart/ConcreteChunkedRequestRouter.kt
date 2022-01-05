package com.uj.rcbackend.nettypart.chunkedpart.chunkedrequestrouterpart

import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import com.uj.rcbackend.dagger.NettyScope
import com.uj.rcbackend.dagger.NettySubComponent
import com.uj.rcbackend.nettypart.chunkedpart.ChunkWorkModeHandler
import com.uj.rcbackend.nettypart.send501Response
import com.uj.rcbackend.nettypart.utils.SpecificChain
import javax.inject.Inject

//TODO
@SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
@Sharable
@NettyScope
class ConcreteChunkedRequestRouter @Inject constructor() : AbstractChunkedRequestRouter() {
    companion object {
        val logger = LogManager.getLogger()

    }


    @Inject
    lateinit var chunkedRequestHandlers: Map<String, @JvmSuppressWildcards ChunkWorkModeHandler>

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        super.handlerAdded(ctx)
        logger.debug("ConcreteChunkedRequestRouter added to pipeline")
    }


    override fun channelRead0(ctx: ChannelHandlerContext?, msg: HttpRequest?) {
        logger.debug("In channelRead0")
        if (HttpUtil.is100ContinueExpected(msg)) {
            logger.debug("Received 100Continue")
            ctx?.writeAndFlush(DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE))
        }
        msg?.let {
            val httpMsg = it
            val queryStringDecoder = QueryStringDecoder(it.uri())
            val workMode = queryStringDecoder.path().lowercase().substring(1).substringBefore("/")
            logger.debug("Received message with workMode: $workMode")
            when (workMode) {
                in chunkedRequestHandlers -> {
                    ctx?.let { context ->
                        val targetHandler = chunkedRequestHandlers.get(workMode)!!
                        logger.debug("Modifying pipeline, adding handler: ${targetHandler.handlerDescription}")
                        context.pipeline().addAfter(
                            NettySubComponent.CHUNKED_REQUEST_ROUTER_LITERAL,
                            targetHandler.handlerDescription,
                            targetHandler
                        )
                        logger.debug("Firing read to the next handler with msg : ${httpMsg::class}")
                        context.fireChannelRead(httpMsg)

                    }

                }
                else -> {
                    logger.debug("Unsupported work mode received, sending 501 msg")
                    ctx.send501Response()
                    ctx?.close()
                }
            }
        }
    }
}