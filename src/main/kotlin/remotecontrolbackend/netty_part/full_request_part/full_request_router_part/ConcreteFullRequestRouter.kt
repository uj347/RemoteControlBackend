package remotecontrolbackend.netty_part.full_request_part.full_request_router_part

import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent.Companion.FULL_REQUEST_ROUTER_LITERAL
import remotecontrolbackend.netty_part.full_request_part.FullRequestWorkModeHandler
import remotecontrolbackend.netty_part.full_request_part.command_handler_part.AbstractCommandHandler
import remotecontrolbackend.netty_part.send501Response
import remotecontrolbackend.netty_part.utils.FullRequestChain

import javax.inject.Inject
@Sharable
@FullRequestChain
@NettyScope
class ConcreteFullRequestRouter @Inject constructor() : AbstractFullRequestRouter() {


    @Inject
    lateinit var fullRequestHandlers: Map<String, @JvmSuppressWildcards FullRequestWorkModeHandler>

    @Inject
    lateinit var commandHandler: AbstractCommandHandler


    companion object {
        val logger = LogManager.getLogger()


    }


    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
        if (HttpUtil.is100ContinueExpected(msg)) {
            ctx?.writeAndFlush(DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE))
        }
        msg?.let {
            val httpMsg=it
            val queryStringDecoder = QueryStringDecoder(it.uri())
            val workMode=queryStringDecoder.path().lowercase().substring(1)
            when (workMode) {
                in fullRequestHandlers -> {
                    ctx?.let{
                        val targetHandler=fullRequestHandlers.get(workMode)!!
                        it.pipeline().addAfter(FULL_REQUEST_ROUTER_LITERAL,targetHandler.handlerQuery ,targetHandler)
                        logger.debug("Modifying pipeline, adding handler: ${targetHandler.handlerQuery}")
                        httpMsg.retain()
                        it.fireChannelRead(httpMsg)
                    }

                }


                else -> {
                    ctx.send501Response()
                    ctx?.close()
                }
            }
        }
    }
}

