package remotecontrolbackend.netty_part.request_handler_part

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.netty_part.command_handler_part.AbstractCommandHandler
import remotecontrolbackend.netty_part.send501Response

import javax.inject.Inject

@NettyScope
class ConcreteRequestHandler @Inject constructor() : AbstractRequestHandler() {




    @Inject
    lateinit var commandHandler: AbstractCommandHandler


    companion object {
        val logger= LogManager.getLogger()

        fun ChannelHandlerContext.handleCommandMsg(commandHandler: AbstractCommandHandler, msg: FullHttpRequest) {
            this.pipeline().addLast(commandHandler)
            msg.retain()
            this.fireChannelRead(msg)
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
        if (HttpUtil.is100ContinueExpected(msg)) {
            ctx?.writeAndFlush(DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE))
        }
        msg?.uri()?.let {
            val queryStringDecoder = QueryStringDecoder(it)
            when (queryStringDecoder.path().lowercase()) {
                "/command" -> ctx?.handleCommandMsg(commandHandler, msg)
                else -> ctx.send501Response()
            }
        }
    }
}

