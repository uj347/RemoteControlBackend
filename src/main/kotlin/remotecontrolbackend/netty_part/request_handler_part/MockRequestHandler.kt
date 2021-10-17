package remotecontrolbackend.netty_part.request_handler_part

import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import remotecontrolbackend.dagger.RequestHandlerSubComponent
import remotecontrolbackend.dagger.RhScope
import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommand
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets

@RhScope
class MockRequestHandler (rhBuilder: RequestHandlerSubComponent.RhBuilder):AbstractRequestHandler(rhBuilder) {
    init{
        println("Mock Request handler instantiated")
    }


    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {


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
            ctx.fireChannelRead(MockCommand())
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        println("Invoked channel read complete")

    }
}