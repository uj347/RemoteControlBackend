package remotecontrolbackend.netty_part

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.EmptyByteBuf
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import java.nio.charset.StandardCharsets

//TODO
fun ChannelHandlerContext?.send501Response(msg:String?=null):ChannelFuture{
   var contentLength=0
    val msgBuf:ByteBuf=this!!.alloc().buffer()

    msg?.let{message->
        msgBuf.writeCharSequence(message, StandardCharsets.UTF_8)
            contentLength = msgBuf.readableBytes()
    }
    return this.writeAndFlush(
        DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.NOT_IMPLEMENTED,
            msgBuf
        ).also {
            it.headers().add(HttpHeaderNames.CONNECTION,"close")
            it.headers().add(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.TEXT_PLAIN.concat("; charset=UTF-8"))
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, contentLength)
        }
    )

}

fun ChannelHandlerContext?.send404Response(msg:String?=""):ChannelFuture{
    var contentLength=0
    val msgBuf:ByteBuf=this!!.alloc().buffer()

    msg?.let{message->
        msgBuf.writeCharSequence(message, StandardCharsets.UTF_8)
        contentLength = msgBuf.readableBytes()
    }
    return this.writeAndFlush(
        DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.NOT_FOUND,
            msgBuf
        ).also {
            it.headers().add(HttpHeaderNames.CONNECTION,"close")
            it.headers().add(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.TEXT_PLAIN.concat("; charset=UTF-8"))
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, contentLength)
        }
    )
}
fun ChannelHandlerContext?.send200Response():ChannelFuture{
    return this!!.writeAndFlush(
        DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK
        ).also {
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, "0")
        })
}

fun ChannelHandlerContext?.send500Response(msg:String?=""):ChannelFuture{
    var contentLength=0
    val msgBuf:ByteBuf=this!!.alloc().buffer()

    msg?.let{message->
        msgBuf.writeCharSequence(message, StandardCharsets.UTF_8)
        contentLength = msgBuf.readableBytes()
    }
   return this.writeAndFlush(
        DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.INTERNAL_SERVER_ERROR,
            msgBuf
        ).also {
            it.headers().add(HttpHeaderNames.CONNECTION,"close")
            it.headers().add(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.TEXT_PLAIN.concat("; charset=UTF-8"))
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, contentLength)
        }
    )

}

