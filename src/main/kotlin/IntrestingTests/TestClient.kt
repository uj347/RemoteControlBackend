package IntrestingTests

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.netty.handler.logging.LoggingHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import remotecontrolbackend.ROOT_DIR
import remotecontrolbackend.command_invoker_part.command_hierarchy.BatCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.moshi.PathAdapter
import java.net.SocketAddress
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

fun main() {
val moshi =Moshi.Builder().add(PathAdapter()).build()
    val adapter= moshi.adapter<Set<Path>>(Types.newParameterizedType(Set::class.java,Path::class.java))
    val pathSet= setOf<Path>(Paths.get("jojo"),Paths.get("sss"))
    println(adapter.toJson(pathSet))
}

class TestClient {
    val batCommand = BatCommand("cmd /c start \"\" ping vk.com", "TESTBATCOMMAND")

    val mainComponent = DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get(ROOT_DIR))
        .setPort(34444)
        .isSSLEnabled(false)
        .isAuthEnabled(false)
        .buildMainComponent()

    val comInvSubcomponent = mainComponent
        .getComandInvokerSubcompBuilder()
        .build()

    val nettyComponent = mainComponent.getNettySubcomponentBuilder().buildNettySubcomponent()
    val sslContextProvider = nettyComponent.getSSLContextProvider()

    val commandSetAdapter: JsonAdapter<Set<SerializableCommand>> = comInvSubcomponent.getMoshi()
        .adapter(Types.newParameterizedType(Set::class.java, SerializableCommand::class.java))
    val moshi = Moshi.Builder().build()
    val jsonAdapter = moshi.adapter<Array<Array<String>>>(Types.arrayOf(Types.arrayOf(String::class.java)))


    fun launchTestClient() {
        val groop: NioEventLoopGroup = NioEventLoopGroup()
        val bootStrap = Bootstrap()
        val serializedBat = commandSetAdapter.toJson(setOf(batCommand))
        val contentWithSerializableCommand = Unpooled.wrappedBuffer(serializedBat.encodeToByteArray())
        val capsCommand = Unpooled.wrappedBuffer(
            jsonAdapter.toJson(arrayOf(arrayOf("keyPress", "20"))).toByteArray(StandardCharsets.UTF_8)
        )
        try {
            bootStrap
                .group(groop)
                .channel(NioSocketChannel::class.java)
                .handler(
                    object : ChannelInitializer<NioSocketChannel>() {
                        override fun initChannel(ch: NioSocketChannel?) {
                            ch?.let {
//                              it.pipeline().addFirst("SSLHANDLER",
//                              SslHandler(sslContextProvider.clientSslContext.newEngine(it.alloc()))
//                              )
                                it.pipeline().addLast(
                                    LoggingHandler(),
                                    HttpClientCodec(),

                                    object : ChannelOutboundHandlerAdapter() {
                                        override fun connect(
                                            ctx: ChannelHandlerContext?,
                                            remoteAddress: SocketAddress?,
                                            localAddress: SocketAddress?,
                                            promise: ChannelPromise?
                                        ) {
                                            super.connect(ctx, remoteAddress, localAddress, promise)
                                            println("client channel connected: $remoteAddress")
                                            promise?.addListener {
                                                val headers = DefaultHttpRequest(
                                                    HttpVersion.HTTP_1_1,
                                                    HttpMethod.POST, "/robot"
                                                ).also {
                                                    it.headers().add(HttpHeaderNames.HOST, "127.0.0.1:34747")
                                                    it.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
                                                    it.headers().add(HttpHeaderNames.AUTHORIZATION, "cococo")
                                                    it.headers().add(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON)
                                                    HttpUtil.setTransferEncodingChunked(it, true)
                                                }
                                                // println("context is: ${ctx?.channel()}")

                                                ctx?.writeAndFlush(headers)
                                                println("Headers written")

                                                groop.execute {
                                                    val scanner = Scanner(System.`in`)
                                                    while (true) {
                                                        val input = scanner.next()
                                                        if (input == "end") {
                                                            ctx?.close()
                                                            break
                                                        }
                                                        val capsBody = DefaultHttpContent(capsCommand)
                                                        capsBody.retain()
                                                        ctx?.writeAndFlush(capsBody)
                                                        println("written $capsBody")
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    object : SimpleChannelInboundHandler<FullHttpResponse>() {
                                        override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpResponse?) {
                                            msg?.let { println("Received msg from server : $msg") }
                                        }
                                    },

                                    )
                            }
                        }
                    })
            val bootstrapFuture = bootStrap.connect("127.0.0.1", 34747).sync()
            bootstrapFuture.channel().closeFuture().sync()
        } finally {
            groop.shutdownGracefully()
            println("Netty shutted down")
        }
    }
}