package com.uj.rcbackend.IntrestingTests

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.ssl.SslContextBuilder
import java.io.File


fun main(){
    var sslContext= SslContextBuilder
        .forClient()
        .keyManager(
            File("J:\\sslMagick\\client\\clientcert.pem"),
            File("J:\\sslMagick\\client\\clientkey.pem")
        )
        .trustManager(File("J:\\sslMagick\\ca\\cacert.pem"))
        .build()

    val bootstrap: Bootstrap = Bootstrap()
    val group: NioEventLoopGroup = NioEventLoopGroup()
    bootstrap.group(group).channel(NioSocketChannel::class.java).handler(object:
        ChannelInitializer<NioSocketChannel>(){
        override fun initChannel(ch: NioSocketChannel?) {
            ch?.pipeline()?.addLast(
                sslContext.newHandler(ch.alloc()),
                StringDecoder(),
                object: ChannelInboundHandlerAdapter(){
                    override fun channelActive(ctx: ChannelHandlerContext?) {
                        ctx?.writeAndFlush(Unpooled.wrappedBuffer("Hello".encodeToByteArray()))

                    }
                }

            )
        }
    })
    val future=bootstrap.connect("127.0.0.1",34747)
    future.addListener { println("Client connected") }
    future.channel().closeFuture().sync()

}