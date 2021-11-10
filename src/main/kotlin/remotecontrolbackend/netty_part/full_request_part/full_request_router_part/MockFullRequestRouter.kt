package remotecontrolbackend.netty_part.full_request_part.full_request_router_part

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*

import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.netty_part.full_request_part.command_handler_part.AbstractCommandHandler
import javax.inject.Inject

@NettyScope

class MockFullRequestRouter @Inject constructor():AbstractFullRequestRouter() {
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