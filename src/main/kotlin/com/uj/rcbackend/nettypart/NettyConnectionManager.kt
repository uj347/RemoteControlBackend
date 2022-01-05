package com.uj.rcbackend.nettypart

import AUTH_ENABLED_LITERAL
import PORT_LITERAL
import SSL_ENABLED_LITERAL
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.SslHandler
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import com.uj.rcbackend.dagger.NettyMainModule.Companion.NETTY_COROUTINE_CONTEXT_LITERAL
import com.uj.rcbackend.dagger.NettyScope
import com.uj.rcbackend.dagger.NettySubComponent
import com.uj.rcbackend.dagger.NettySubComponent.Companion.AUTH_HANDLER_LITERAL
import com.uj.rcbackend.dagger.NettySubComponent.Companion.TRANSFER_ENCODING_INTERCEPTOR_LITERAL
import com.uj.rcbackend.dagger.NettySubComponent.Companion.HTTP_CODEC_LITERAL
import com.uj.rcbackend.dagger.NettySubComponent.Companion.FULL_REQUEST_ROUTER_LITERAL
import com.uj.rcbackend.dagger.NettySubComponent.Companion.SSL_HANDLER_LITERAL
import com.uj.rcbackend.nettypart.authpart.AbstractAuthHandler
import com.uj.rcbackend.nettypart.fullrequestpart.fullrequestrouterpart.AbstractFullRequestRouter
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
    lateinit var requestHandler: AbstractFullRequestRouter

    @Inject
    lateinit var sslContextProvider: NettySslContextProvider

    @Inject
    lateinit var chunkedInterceptor: TransferEncodingInterceptor


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

                                    it.pipeline().addLast(LoggingHandler())

                                    if (isSSLEnabled) {
                                        it.pipeline().addFirst(
                                            SSL_HANDLER_LITERAL,
                                            SslHandler(sslContextProvider.serverSslContext.newEngine(it.alloc()))
                                        )
                                    }
                                    it.pipeline().addLast(
                                        HTTP_CODEC_LITERAL,
                                        HttpServerCodec()
                                    )

                                    it.pipeline().addLast(LoggingHandler())


                                    if (isAuthEnabled) {
                                        it.pipeline().addLast(
                                            AUTH_HANDLER_LITERAL,
                                            authHandler
                                        )
                                    }

                                    it.pipeline().addLast(
                                        TRANSFER_ENCODING_INTERCEPTOR_LITERAL,
                                        chunkedInterceptor

                                    )


                                    it.pipeline().addLast(
                                        FULL_REQUEST_ROUTER_LITERAL,
                                        requestHandler
                                    )

                                    it.pipeline().addLast(
                                       ExceptionCatcherHandler.handlerDescription,
                                        ExceptionCatcherHandler()
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



