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
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import remotecontrolbackend.PORT
import remotecontrolbackend.dagger.NettyModule.Companion.NETTY_COROUTINE_CONTEXT_LITERAL
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.command_handler_part.handler.AbstractCommandHandler
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler
import java.util.logging.LogManager
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext


@NettyScope
class NettyConnectionManager(
    val nettySubComponentBuilder: NettySubComponent.NettySubComponentBuilder,
    @Named(PORT_LITERAL) val port: Int
) {
    init {
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
        nettySubComponentBuilder.buildNettySubcomponent().inject(this)
    }
val logger:Logger=LoggerFactory.getLogger(this::class.java)
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    val bootStrap = ServerBootstrap()
    var nettyJob:Job?=null

@Inject
@Named(NETTY_COROUTINE_CONTEXT_LITERAL)
lateinit var nettyCoroutineContext: CoroutineContext

    @Inject
    lateinit var authHandler: AbstractAuthHandler

    @Inject
    lateinit var requestHandler: AbstractRequestHandler



    fun launchNetty() {
        val nettyScope= CoroutineScope(nettyCoroutineContext)
   nettyJob=nettyScope.launch{
        println("Starting Netty in ${this.coroutineContext}")
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
                                        override fun handlerAdded(ctx: ChannelHandlerContext?) {
                                            super.handlerAdded(ctx)
                                            println("pipeline listener added")
                                        }

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

                                    requestHandler


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



