package remotecontrolbackend.netty_part.auth_part

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import remotecontrolbackend.UserRepo
import remotecontrolbackend.dagger.NettyScope
import javax.inject.Inject



@NettyScope
class ConcreteAuthHandler  @Inject constructor() : AbstractAuthHandler() {

    @Inject
    lateinit var userRepo: UserRepo
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: HttpRequest?) {
        ctx?.let {
            if (msg != null) {
                val authHeaderContents = msg.headers().get(HttpHeaderNames.AUTHORIZATION)
                when (authHeaderContents) {
                    null -> {
                        println("Sending unAuthorizedResponse because of noAuthHeader in request")
                        ctx.writeAndFlush(constructUnAuthorizedRespose())
                    }
                    else -> if (checkAuth(authHeaderContents)) {

                        ctx.fireChannelRead(msg)
                    } else {
                        ctx.writeAndFlush(constructUnAuthorizedRespose())

                    }
                }
            }
        }

        }



    fun checkAuth(authHeaderContent: String): Boolean {
        val basicProcessedString = authHeaderContent.removePrefix("Basic ")
        for (user in userRepo.getAlowedUsers()) {
            if(user.getBase64Credentials()==basicProcessedString){
                return true
            }
        }
        return false
    }


}



