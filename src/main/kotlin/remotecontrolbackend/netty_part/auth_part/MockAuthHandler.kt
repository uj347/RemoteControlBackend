package remotecontrolbackend.netty_part.auth_part

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import remotecontrolbackend.dagger.NettyScope
import javax.inject.Inject


@NettyScope
class MockAuthHandler @Inject constructor():AbstractAuthHandler() {
    fun checkAuth(string:String):Boolean=true

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
        if(ctx!=null){
            if (msg != null) {
                println("recieved message in auth: $msg")
                val authHeaderContents = msg.headers().get(HttpHeaderNames.AUTHORIZATION)
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