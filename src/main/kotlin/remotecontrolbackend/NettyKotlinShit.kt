package remotecontrolbackend

import DaggerMainComponent
import dagger.internal.DaggerCollections
import dagger.internal.DaggerGenerated
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpMessage
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.logging.LoggingHandler
import kotlinx.coroutines.*
import remotecontrolbackend.auth_part.AbstractAuthHandler
import remotecontrolbackend.auth_part.ConcreteAuthHandler
import remotecontrolbackend.request_handler_part.AbstractRequestHandler
import remotecontrolbackend.request_handler_part.MockRequestHandler
import javax.inject.Inject

fun main() {
    runBlocking {
        nettyScope.launch{ NettyKotlinShit().launch() }.join()

    }

}

const val PORT = 34747
val nettyScope = CoroutineScope(context = Dispatchers.IO + CoroutineExceptionHandler(
    { context, exc -> println("Exception $exc happened at $context") }
))

class NettyKotlinShit {

    init {
        DaggerMainComponent.create().inject(this)
    }

    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    val bootStrap = ServerBootstrap()


    @Inject
    lateinit var authHandler: AbstractAuthHandler
    @Inject
    lateinit var requestHandler: AbstractRequestHandler


    suspend fun launch() {

                try {
                    bootStrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel::class.java)
                        .handler(LoggingHandler())
                        .childHandler(
                            object : ChannelInitializer<SocketChannel>() {
                                override fun initChannel(ch: SocketChannel?) {
                                    ch?.let {
                                        it.pipeline().addLast(HttpServerCodec(),
                                            HttpObjectAggregator(Int.MAX_VALUE),
                                            object:ChannelOutboundHandlerAdapter(){
                                                override fun write(
                                                    ctx: ChannelHandlerContext?,
                                                    msg: Any?,
                                                    promise: ChannelPromise?
                                                ) {
                                                    println("Display proxy. Now passing trough: ${msg as HttpMessage}")
                                                    ctx?.write(msg)
                                                }
                                            },
                                                    authHandler,
                                            requestHandler,


                                        )
                                    }
                                }
                            })
                    val bootstrapFuture = bootStrap.bind(PORT).sync()
                    bootstrapFuture.channel().closeFuture().sync()
                } finally {
                    workerGroup.shutdownGracefully()
                    bossGroup.shutdownGracefully()
                }

            }
        }



