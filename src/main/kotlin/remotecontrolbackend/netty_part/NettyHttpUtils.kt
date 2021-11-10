package remotecontrolbackend.netty_part

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion


fun ChannelHandlerContext?.send501Response():ChannelFuture{
    return this!!.writeAndFlush(
        DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.NOT_IMPLEMENTED
        ).also {
            it.headers().add(HttpHeaderNames.CONNECTION,"close")
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, "0")
        }
    )

}

fun ChannelHandlerContext?.send404Response():ChannelFuture{
    return this!!.writeAndFlush(
        DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.NOT_FOUND
        ).also {
            it.headers().add(HttpHeaderNames.CONNECTION,"close")
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, "0")
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

fun ChannelHandlerContext?.send500Response():ChannelFuture{
   return this!!.writeAndFlush(
        DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.INTERNAL_SERVER_ERROR
        ).also {
            it.headers().add(HttpHeaderNames.CONNECTION,"close")
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, "0")
        }
    )

}