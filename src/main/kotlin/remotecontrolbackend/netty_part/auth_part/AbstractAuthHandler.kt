package remotecontrolbackend.netty_part.auth_part

import io.netty.channel.ChannelHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import remotecontrolbackend.AuthComponent
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject

const val AUTH = "Authorization"
@ChannelHandler.Sharable
abstract class AbstractAuthHandler (authComponent: AuthComponent.AuthBuilder): SimpleChannelInboundHandler<FullHttpRequest>()


fun String.base64ToAscii(): String {
    val base64Decoder = Base64.getDecoder()
    val bytes = base64Decoder.decode(this).let { ByteBuffer.wrap(it) }
    return StandardCharsets.US_ASCII.decode(bytes).toString()
}

fun String.asciiToBase64(): String {
    val base64Encoder = Base64.getEncoder()
    val bytes = StandardCharsets.US_ASCII.encode(this)
    return base64Encoder.encodeToString(bytes.array())
}
fun constructUnAuthorizedRespose(): FullHttpResponse {
    val response: FullHttpResponse = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED)
        .apply {
            headers().add(HttpHeaderNames.WWW_AUTHENTICATE,"Basic realm=\"ACCES TO REMOTE CONTROL\"")
            HttpUtil.setContentLength(this,0)
        }
    return response
}
