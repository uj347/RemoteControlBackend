package remotecontrolbackend.request_handler_part

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http2.HttpConversionUtil
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class MockRequestHandler @Inject constructor():AbstractRequestHandler() {
    init{
        println("Mock Request handler instantiated")}



    override fun acceptInboundMessage(msg: Any?): Boolean {
     return true
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {

       //TODO MEssage and Content
        ctx?.let{
            println("Request handler recieved msg: $msg")
            val content=ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap("Vualya blya"),StandardCharsets.US_ASCII)
//            content.retain()
            val responseMessage: HttpMessage = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
                .apply { HttpUtil.setContentLength(this,content.writerIndex().toLong())
                this.headers().add(HttpHeaderNames.CONTENT_TYPE,"text/plain")
                }

            val responseContent:DefaultLastHttpContent=DefaultLastHttpContent(content)
            val fullResponse=DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,content)
                .apply { HttpUtil.setContentLength(this,content.writerIndex().toLong())
                this.headers().add(HttpHeaderNames.CONTENT_TYPE,"text/plain")

            }
            println("Writing/flushing: $fullResponse")
            ctx.writeAndFlush(fullResponse)
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        println("Invoked channel read complete")

    }
}