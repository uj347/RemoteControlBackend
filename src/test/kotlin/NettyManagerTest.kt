import com.squareup.moshi.Moshi
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import remotecontrolbackend.command_invoker_part.command_hierarchy.BatCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommand
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.command_handler_part.handler.AbstractCommandHandler
import remotecontrolbackend.netty_part.command_handler_part.handler.MockCommandHandler
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.test.assertEquals

class NettyManagerTest {

    val nettySubcomponent:NettySubComponent = DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get("j:\\testo"))
        .setPort(34444)
        .isTestRun(false)
        .buildMainComponent()
        .getNettySubcomponentBuilder()
        .buildNettySubcomponent()

    val authHandler: AbstractAuthHandler=nettySubcomponent.getAuthHandler()
    val  requestHandler: AbstractRequestHandler=nettySubcomponent.getRequestHandler()
    val  commandHandler: AbstractCommandHandler=nettySubcomponent.getCommandHandler()
    val batCommand = BatCommand("cmd /c start \"\" ping vk.com", "TESTBATCOMMAND")
    val mockCommand=MockCommand("TEST_MOCK_COMMAND")

    @Before
    fun cleanUp(){
        val comRepoDir=commandHandler.commandInvoker.commandRepo.repoDirectory
       if(comRepoDir.exists()) {
           FileUtils.cleanDirectory(comRepoDir.toFile())
       }
    }

@Test
    fun checkEmbeddedChannelWorksAsExpected() {
    val testString1="TSTSTRING"
    val testString2="TESTSTR2"
    val testHandler1:ChannelInboundHandlerAdapter=object :ChannelInboundHandlerAdapter(){
          override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
              if(msg is String){
                  ctx?.fireChannelRead(msg)
              }
          }
      }
    val testHandler2:ChannelInboundHandlerAdapter=object :ChannelInboundHandlerAdapter(){
        override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
            if(msg is String){
                (msg as String).let {
                    ctx?.fireChannelRead(it +testString2 )
                }

            }
        }
    }

    val testChannel:EmbeddedChannel=EmbeddedChannel(testHandler1,testHandler2)

    assert(testChannel.writeInbound(testString1))
    assertEquals(testString1+testString2,testChannel.readInbound())
    }


    @Test
    fun checkRequestCommandChainWorks(){
        val commandHttpRequest=DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT,"/command",true )
            .also { it.headers()?.add(HttpHeaderNames.HOST, "www.booba.com" ) }

        val noCommandHttpRequest=DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/",true )
            .also { it.headers()?.add(HttpHeaderNames.HOST, "www.booba.com" ) }

        val testChannel:EmbeddedChannel=EmbeddedChannel(requestHandler)

        testChannel.writeInbound(commandHttpRequest)
        assertEquals((testChannel.readInbound() as String),MockCommandHandler.PASS_TROUGH_STRING)

        testChannel.writeInbound(noCommandHttpRequest)
        assert((testChannel.readOutbound() as Any is FullHttpResponse))
    }




    @Test
    fun testingCommandHandler(){
        val commandInvoker=commandHandler.commandInvoker
        val commandRepo=commandInvoker.commandRepo
        val moshi=commandRepo.moshi

        Thread.sleep(600)


        val testChannel=EmbeddedChannel(commandHandler)
        val serizalizedBatCommand=batCommand.getByteBuf(moshi)
        val serializedMockCommand=mockCommand.getByteBuf(moshi)
        assert(commandRepo.pointerMap!!.isEmpty())

        val testMockCommandRequestWithoutQS=DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
            HttpMethod.POST,
            "/command",
            serializedMockCommand).also {
            it.headers().add(HttpHeaderNames.HOST,"127.0.0.1:34747")
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, serializedMockCommand.readableBytes().toLong())
            it.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE)
            it.headers().add(HttpHeaderNames.AUTHORIZATION,"cococo")
            it.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
    }

        val testBatCommandRequestWithoutQS=DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
            HttpMethod.POST,
            "/command",
            serizalizedBatCommand).also {
            it.headers().add(HttpHeaderNames.HOST,"127.0.0.1:34747")
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, serizalizedBatCommand.readableBytes().toLong())
            it.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE)
            it.headers().add(HttpHeaderNames.AUTHORIZATION,"cococo")
            it.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        }
        println("testCommadRequsetWithoutQS is $testMockCommandRequestWithoutQS")
        testChannel.writeOneInbound(testMockCommandRequestWithoutQS)
        testChannel.writeOneInbound(testMockCommandRequestWithoutQS)
        testChannel.writeOneInbound(testMockCommandRequestWithoutQS)

       Thread.sleep(200)
            assert((testChannel.readOutbound() as FullHttpResponse).status()==HttpResponseStatus.OK)
            assertEquals(commandRepo.pointerMap!!.size,1,"failed because pointemap size is " +
                    "${commandRepo.pointerMap!!.size}, " +
                    "pointermap:${commandRepo.pointerMap!!.keys}")

        testChannel.writeOneInbound(testBatCommandRequestWithoutQS)
        testChannel.writeOneInbound(testBatCommandRequestWithoutQS)
        Thread.sleep(500)
        assertEquals(2, commandRepo.pointerMap!!.size,"failed because pointemap size is " +
                "${commandRepo.pointerMap!!.size}, " +
                "pointermap:${commandRepo.pointerMap!!.keys}")

        val serializableCommandDirectory=commandRepo.serializedCommandsDir


        runBlocking {
            delay(250)
            val serializedCommandsInDir=Files.newDirectoryStream(serializableCommandDirectory).count()
        assertEquals(2,serializedCommandsInDir)

        }
    }


    @Test
    fun testGetCommandRequest(){
        val commandInvoker=commandHandler.commandInvoker
        val commandRepo=commandInvoker.commandRepo
        val moshi=commandRepo.moshi
        Thread.sleep(250)
        val testChannel=EmbeddedChannel(commandHandler)
        Thread.sleep(450)
        runBlocking {
            commandRepo.addToRepo(mockCommand)
        }
        Thread.sleep(450)
        assert(!commandRepo.pointerMap!!.isEmpty())

        val testGetRequest=DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
            HttpMethod.GET,
            "/command").also {
            it.headers().add(HttpHeaderNames.HOST,"127.0.0.1:34747")
            it.headers().add(HttpHeaderNames.CONTENT_LENGTH, 0)
            it.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE)
            it.headers().add(HttpHeaderNames.AUTHORIZATION,"cococo")
        }
        testChannel.writeInbound(testGetRequest)
        Thread.sleep(650)
        assert( (testChannel.readOutbound() as FullHttpResponse).content().readableBytes()>0  )
    }
}




fun SerializableCommand.getByteBuf(moshi: Moshi):ByteBuf{
    val serializableCommandToMapAdapter=moshi.adapter(SerializableCommand::class.java)
    val buffToReturn=Unpooled.buffer()
    buffToReturn.writeCharSequence(serializableCommandToMapAdapter.toJson(this),Charsets.UTF_8)
    return buffToReturn
}


