package remotecontrolbackend.netty_part

import AUTH_ENABLED_LITERAL
import PORT_LITERAL
import SSL_ENABLED_LITERAL
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.SslHandler
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettyModule.Companion.NETTY_COROUTINE_CONTEXT_LITERAL
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.NettySslContextProvider.Companion.logger
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext


@NettyScope
class NettyConnectionManager(
    val nettySubComponentBuilder: NettySubComponent.NettySubComponentBuilder,
    @Named(PORT_LITERAL) val port: Int,
    @Named(SSL_ENABLED_LITERAL) val isSSLEnabled: Boolean,
    @Named(AUTH_ENABLED_LITERAL) val isAuthEnabled: Boolean
) {
    init {
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
        nettySubComponentBuilder.buildNettySubcomponent().inject(this)
        val logger = LogManager.getLogger()

    }

    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    val bootStrap = ServerBootstrap()
    var nettyJob: Job? = null

    @Inject
    @Named(NETTY_COROUTINE_CONTEXT_LITERAL)
    lateinit var nettyCoroutineContext: CoroutineContext


    @Inject
    lateinit var authHandler: AbstractAuthHandler

    @Inject
    lateinit var requestHandler: AbstractRequestHandler

    @Inject
    lateinit var sslContextProvider: NettySslContextProvider


    fun launchNetty() {
        val nettyScope = CoroutineScope(nettyCoroutineContext)
        nettyJob = nettyScope.launch {
            println("Starting Netty in ${this.coroutineContext}")
            try {
                bootStrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .handler(LoggingHandler())
                    .childHandler(
                        object : ChannelInitializer<SocketChannel>() {
                            override fun initChannel(ch: SocketChannel?) {
                                ch?.let {
                                    if (isSSLEnabled) {
                                        it.pipeline().addFirst(
                                            "SSL_HANDLER",
                                            SslHandler(sslContextProvider.serverSslContext.newEngine(it.alloc()))
                                        )
                                    }
                                    it.pipeline().addLast(
                                        HttpServerCodec(),
                                        HttpObjectAggregator(Int.MAX_VALUE)
                                    )

                                    if (isAuthEnabled) {
                                        it.pipeline().addLast(
                                            "AUTH_HANDLER",
                                            authHandler
                                        )
                                    }
                                    it.pipeline().addLast(requestHandler)
                                    it.pipeline().addLast(
                                        "ExceptionCatcher",
                                        object:ChannelInboundHandlerAdapter(){
                                            override fun exceptionCaught(
                                                ctx: ChannelHandlerContext?,
                                                cause: Throwable?
                                            ) {
                                               cause?.let { logger.error(it)}
                                                ctx?.channel()?.close()
                                            }
                                        }
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

    suspend fun stopNetty() {
        nettyJob?.cancel()
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()

    }
}



