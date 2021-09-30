package remotecontrolbackend.auth_part

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import remotecontrolbackend.AuthComponent
import remotecontrolbackend.AuthScope

@AuthScope
class MockAuthHandler(authComponent: AuthComponent):AbstractAuthHandler(authComponent) {
    fun checkAuth(string:String):Boolean=true

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
        if(ctx!=null){
            if (msg != null) {
                println("recieved message in auth: $msg")
                val authHeaderContents = msg.headers().get(AUTH)
                when (authHeaderContents) {
                    null -> {
                        println("Sending unAuthorizedResponse because of noAuthHeader in request")
                        ctx.writeAndFlush(constructUnAuthorizedRespose())
                    }
                    else -> if (checkAuth(authHeaderContents)) {
                        println("passin trough")
                        println("msg is instance of fulllHTTPRequest: ${msg is FullHttpRequest}")
                        println("ctx: $ctx")
//                    ctx?.writeAndFlush(msg)
                        msg.retain()
                        ctx.fireChannelRead(msg)
                    } else {
                        println("Sending unAuthorizedResponse because of non correct credentials")
//                    ctx?.writeAndFlush(constructUnAuthorizedRespose())
                        ctx.writeAndFlush(constructUnAuthorizedRespose())

                    }
                }
            }
        }
    }
}