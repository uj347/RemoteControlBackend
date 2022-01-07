package com.uj.rcbackend.IntrestingTests

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.logging.LoggingHandler
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory

fun main(){

    InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
    var connectFuture: ChannelFuture?

    val sBootstrap: ServerBootstrap = ServerBootstrap()
    val group: NioEventLoopGroup = NioEventLoopGroup()
    sBootstrap.group(group).channel(NioServerSocketChannel::class.java)
//        .handler(LoggingHandler())
        .childHandler(object: ChannelInitializer<NioSocketChannel>(){
            override fun initChannel(ch: NioSocketChannel?) {
                ch?.pipeline()?.addLast(
                    LoggingHandler(),
                    StringDecoder(),
                    object: SimpleChannelInboundHandler<String>(){
                        override fun channelRegistered(ctx: ChannelHandlerContext?) {
                            println("Connected to: ${ctx?.channel()?.remoteAddress()}")
                        }

                        override fun channelRead0(ctx: ChannelHandlerContext?, msg: String?) {
                            println("Received: $msg")
                        }
                    }

                )
            }
        })
    val future=sBootstrap.bind(34747).also { it.addListener { println("server bound to port: 34747") } }
    future.channel().closeFuture().sync()

}