package IntrestingTests

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.netty.handler.logging.LoggingHandler
import remotecontrolbackend.command_invoker_part.command_hierarchy.BatCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.moshi.SerializableCommandToMapAdapter
import java.net.SocketAddress
import java.nio.charset.Charset
import java.nio.file.Paths
fun main(){
    val testClient=TestClient()
    testClient.launchTestClient()
}
class TestClient {
    val batCommand = BatCommand("cmd /c start \"\" ping vk.com", "TESTBATCOMMAND")
    val comInvSubcomponent=DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get("j:\\testClietnt\\"))
        .setPort(34444)
        .isTestRun(true)
        .buildMainComponent()
        .getComandInvokerSubcompBuilder()
        .build()

    val SerializableCommandToMapAdapter=comInvSubcomponent.getMoshi().adapter(SerializableCommand::class.java)


  fun launchTestClient() {
      val groop:NioEventLoopGroup= NioEventLoopGroup()
      val bootStrap=Bootstrap()
      val serializedBat=SerializableCommandToMapAdapter.toJson(batCommand)
      val contentWithSerializableCommand= Unpooled.wrappedBuffer(serializedBat.encodeToByteArray())
      try {
          bootStrap
              .group(groop)
              .channel(NioSocketChannel::class.java)
              .handler(
                  object : ChannelInitializer<NioSocketChannel>() {
                      override fun initChannel(ch: NioSocketChannel?) {
                          ch?.let {
                              it.pipeline().addLast(
                                  object:ChannelOutboundHandlerAdapter(){
                                      override fun write(
                                          ctx: ChannelHandlerContext?,
                                          msg: Any?,
                                          promise: ChannelPromise?
                                      ) {
                                          println("FROM MONITOR: Client sending :$msg")
                                          super.write(ctx, msg, promise)
                                      }


                                  },

                                          HttpClientCodec(),
                                  HttpObjectAggregator(Int.MAX_VALUE),
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
                                              val msgToSend=DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                  HttpMethod.POST,"/command",contentWithSerializableCommand).also {
                                                  it.headers().add(HttpHeaderNames.HOST,"127.0.0.1:34747")
                                                  it.headers().add(HttpHeaderNames.CONTENT_LENGTH,contentWithSerializableCommand.readableBytes().toLong())
                                                  it.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE)
                                                  it.headers().add(HttpHeaderNames.AUTHORIZATION,"cococo")
                                                  it.headers().add(HttpHeaderNames.CONTENT_TYPE,APPLICATION_JSON)
                                              }
                                              // println("context is: ${ctx?.channel()}")

                                              ctx?.writeAndFlush(msgToSend)

                                          }
                                      }
                                                                           },
                                  object:SimpleChannelInboundHandler<FullHttpResponse> (){
                                      override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpResponse?) {
                                          msg?.let { println("Received msg from server : $msg") }
                                      }
                                  },

                              )
                          }
                      }
                  })
          val bootstrapFuture = bootStrap.connect("127.0.0.1",34747).sync()
          bootstrapFuture.channel().closeFuture().sync()
      } finally {
          groop.shutdownGracefully()
          println("Netty shutted down")
      }
  }
}