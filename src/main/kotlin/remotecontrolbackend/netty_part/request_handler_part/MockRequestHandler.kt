package remotecontrolbackend.netty_part.request_handler_part

import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*

import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommand
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.netty_part.command_handler_part.handler.AbstractCommandHandler
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@NettyScope

class MockRequestHandler @Inject constructor():AbstractRequestHandler() {
    init{
        println("Mock Request handler instantiated")
    }
    @Inject
    lateinit var commandHandler: AbstractCommandHandler


    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
if(HttpUtil.is100ContinueExpected(msg)){
    ctx?.write(DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.CONTINUE))
}

        val uri=msg?.uri()
        val queryStringDecoder=QueryStringDecoder(uri)
        if(queryStringDecoder.path().equals("/command",true)) {
            if (msg!=null){
                println("Recieved command request in mock request handler")
                ctx?.pipeline()?.addLast(commandHandler)
                msg.retain()
                ctx?.fireChannelRead(msg)
            }
        }else{
            println("received nocommand request in mock request handler")
            ctx?.writeAndFlush(
                DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK)
            )
        }
        }


    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        println("Invoked channel read complete")

    }
}