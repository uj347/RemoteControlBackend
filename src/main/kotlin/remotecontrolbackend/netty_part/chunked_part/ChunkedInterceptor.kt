package remotecontrolbackend.netty_part.chunked_part

import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent.Companion.CHUNKED_INTERCEPTOR_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.CHUNKED_REQUEST_ROUTER_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.HTTP_AGGREGATOR_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.FULL_REQUEST_ROUTER_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.HTTP_CODEC_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.ROBOT_HANDLER_LITERAL
import remotecontrolbackend.netty_part.chunked_part.chunked_request_handler_part.AbstractChunkedRequestRouter
import remotecontrolbackend.netty_part.chunked_part.robot_handler_part.ConcreteRobotHandler
import remotecontrolbackend.netty_part.send501Response
import java.lang.RuntimeException
import javax.inject.Inject

//TODO Разнести отвественность между чанкед интерсептером и чанкедРеквестХэндлером
@NettyScope
@Sharable
class ChunkedInterceptor @Inject constructor() : ChannelInboundHandlerAdapter() {


    @Inject
    lateinit var chunnkedRequestRouter: AbstractChunkedRequestRouter


    companion object {
        val logger = LogManager.getLogger()

        val handlersToRemoveForChunked = setOf<String>(
            HTTP_AGGREGATOR_LITERAL,
            FULL_REQUEST_ROUTER_LITERAL
        )


    }


    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        msg?.let {
            if (it is HttpRequest) {
                val httpRequest=it
              //TODO Впихурить сюда работу с фьючерами ,возможно месседж не поступает изза того что рид случается раньше добавления хэндлера в пайплайн
                logger.debug("Chunked interceptor received request: $it")
                if (HttpUtil.isTransferEncodingChunked(it)) {
                    logger.debug("Encoding of msg is CHUNKED, interceptor is reconfiguring pipeline")
                    ctx?.let {
                        handlersToRemoveForChunked.forEach { handlerName ->
                            if (handlerName in ctx.pipeline().names()) {
                                logger.debug("Removing handler from pipeline: $handlerName")
                                ctx.pipeline().remove(handlerName)
                            }
                        }
                        logger.debug("Removing chunkedInterceptor from pipeline")
                        it.pipeline().remove(CHUNKED_INTERCEPTOR_LITERAL)
                        logger.debug("Adding $CHUNKED_REQUEST_ROUTER_LITERAL to the pipeline")
                        ctx.pipeline()?.addAfter(
                            HTTP_CODEC_LITERAL,
                            CHUNKED_REQUEST_ROUTER_LITERAL,
                            chunnkedRequestRouter
                        )
                        logger.debug("Firing read on the chunked-modified pipeline")
                        ctx.pipeline().fireChannelRead(httpRequest)
                    }
                } else {
                    logger.debug("Passing message trough without modification of pipeline")
                    ctx?.fireChannelRead(msg)
                }
            }
        }
    }
}
