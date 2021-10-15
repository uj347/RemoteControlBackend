package remotecontrolbackend.netty_part

import DaggerMainComponent
import PORT_LITERAL
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
import remotecontrolbackend.PORT
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.command_handler_part.handler.AbstractCommandHandler
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler
import javax.inject.Inject
import javax.inject.Named


@NettyScope
class NettyConnectionManager(
    val nettySubComponentBuilder: NettySubComponent.NettySubComponentBuilder,
    @Named(PORT_LITERAL) val port: Int
) {
    init {
        nettySubComponentBuilder.buildNettySubcomponent().inject(this)
    }

    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    val bootStrap = ServerBootstrap()
    var nettyJob:Job?=null


    @Inject
    lateinit var authHandler: AbstractAuthHandler

    @Inject
    lateinit var requestHandler: AbstractRequestHandler

    @Inject
    lateinit var commandHandler: AbstractCommandHandler

//todo переделать в контекмт-ааксептинг
    fun launchNetty(coroutineScope: CoroutineScope) {
   nettyJob=coroutineScope.launch(){
        println("Starting Netty")
        try {
            bootStrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .handler(LoggingHandler())
                .childHandler(
                    object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel?) {
                            ch?.let {
                                it.pipeline().addLast(
                                    HttpServerCodec(),
                                    HttpObjectAggregator(Int.MAX_VALUE),
                                    object : ChannelOutboundHandlerAdapter() {
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
                                    commandHandler


                                    )
                            }
                        }
                    })
            val bootstrapFuture = bootStrap.bind(port).sync()
            bootstrapFuture.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
            println("Netty shutted down")


        }
    }
    }
    suspend fun stopNetty(){
        nettyJob?.cancel()
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()

    }
}



