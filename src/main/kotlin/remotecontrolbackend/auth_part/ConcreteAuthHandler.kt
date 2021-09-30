package remotecontrolbackend.auth_part

import DaggerMainComponent
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import remotecontrolbackend.UserRepo
import remotecontrolbackend.AuthComponent
import remotecontrolbackend.AuthScope
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject



@AuthScope
class ConcreteAuthHandler  constructor(authComponent: AuthComponent) : AbstractAuthHandler(authComponent) {
   init {
       authComponent.inject(this)
   }
    @Inject
    lateinit var userRepo: UserRepo
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
        ctx?.let {
            if (msg != null) {
                val authHeaderContents = msg.headers().get(AUTH)
                when (authHeaderContents) {
                    null -> {
                        println("Sending unAuthorizedResponse because of noAuthHeader in request")
                        ctx.writeAndFlush(constructUnAuthorizedRespose())
                    }
                    else -> if (checkAuth(authHeaderContents)) {
                        msg.retain()
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



