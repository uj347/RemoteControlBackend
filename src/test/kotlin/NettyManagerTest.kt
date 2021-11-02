import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.netty.buffer.ByteBuf
import io.netty.buffer.EmptyByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import remotecontrolbackend.command_invoker_part.command_hierarchy.BatCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.Command
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommand
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.command_handler_part.AbstractCommandHandler
import remotecontrolbackend.netty_part.command_handler_part.CommandStrategy
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler
import java.awt.desktop.OpenURIEvent
import java.lang.reflect.Type
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.test.assertEquals

class NettyManagerTest {

    val nettySubcomponent: NettySubComponent = DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get("j:\\testo"))
        .setPort(34444)
        .isTestRun(false)
        .isSSLEnabled(false)
        .isAuthEnabled(false)
        .buildMainComponent()
        .getNettySubcomponentBuilder()
        .buildNettySubcomponent()


    val authHandler: AbstractAuthHandler = nettySubcomponent.getAuthHandler()
    val requestHandler: AbstractRequestHandler = nettySubcomponent.getRequestHandler()
    val commandHandler: AbstractCommandHandler = nettySubcomponent.getCommandHandler()
    val batCommand = BatCommand("cmd /c start \"\" ping vk.com", "TESTBATCOMMAND")
    val mockCommand = MockCommand("TEST_MOCK_COMMAND")

    val commandInvoker = commandHandler.commandInvoker
    val commandRepo = commandInvoker.commandRepo
    val moshi = commandRepo.moshi

    val setType: Type = Types.newParameterizedType(Set::class.java, SerializableCommand::class.java)
    val serializedCommandSetAdapter = moshi.adapter<Set<SerializableCommand>>(setType)

    @Before
    fun prepCleanUp() {
        val comRepoDir = commandHandler.commandInvoker.commandRepo.repoDirectory
        if (comRepoDir.exists()) {
            FileUtils.cleanDirectory(comRepoDir.toFile())
        }
        Thread.sleep(400)
        commandInvoker.launchCommandInvoker()
    }

    @After
    fun finCleanUp() {
        runBlocking {
            commandRepo.cleanRepoPointermap()
        }
    }


    @Test
    fun checkRequestCommandChainWorks() {
        val commandInvoker = commandHandler.commandInvoker
        val commandRepo = commandInvoker.commandRepo
        val moshi = commandRepo.moshi
        val testChannel: EmbeddedChannel = EmbeddedChannel(requestHandler)


        val mockBuff = testChannel.alloc().buffer(15)
        repeat(15, { mockBuff.writeByte(1) })

        val commandHttpRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/command", mockBuff)
            .also {
                it.headers()?.add(HttpHeaderNames.HOST, "www.booba.com")
                it.headers()?.add(HttpHeaderNames.CONTENT_LENGTH, "15")
                it.headers()?.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            }

        val noCommandHttpRequest = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/", true)
            .also { it.headers()?.add(HttpHeaderNames.HOST, "www.booba.com") }




        testChannel.writeInbound(noCommandHttpRequest)
        assert((testChannel.readOutbound() as Any is FullHttpResponse))


    }


    @Test
    fun testingCommandHandler() {


        Thread.sleep(600)
        val testChannel = EmbeddedChannel(commandHandler)
        val serizalizedBatCommandSet = setOf(batCommand).getByteBuf(moshi)
        val serializedMockCommandSet = setOf(mockCommand).getByteBuf(moshi)
        assert(commandRepo.pointerMap!!.isEmpty())
        val testMockCommandRequestWithoutQS = moshi.generateCommandFullRequest(
            HttpMethod.POST,
            null,
            false,
            serializedMockCommandSet
        )
        val testBatCommandRequestWithoutQS = moshi.generateCommandFullRequest(
            HttpMethod.POST,
            null,
            false,
            serizalizedBatCommandSet
        )

        println("testCommadRequsetWithoutQS is $testMockCommandRequestWithoutQS")

        //testMockCommandRequestWithoutQS.retain()
        // println("FIRST Current refcout is "+testMockCommandRequestWithoutQS.refCnt())
        testChannel.writeOneInbound(testMockCommandRequestWithoutQS)
        // testMockCommandRequestWithoutQS.retain()
        //println("SECOND Current refcout is "+testMockCommandRequestWithoutQS.refCnt())

        testChannel.writeOneInbound(testMockCommandRequestWithoutQS)

        Thread.sleep(600)
        assert((testChannel.readOutbound() as FullHttpResponse).status() == HttpResponseStatus.OK)
        assertEquals(
            commandRepo.pointerMap!!.size, 1, "failed because pointemap size is " +
                    "${commandRepo.pointerMap!!.size}, " +
                    "pointermap:${commandRepo.pointerMap!!.keys}"
        )

        //testBatCommandRequestWithoutQS.retain()
        testChannel.writeOneInbound(testBatCommandRequestWithoutQS)
        //testBatCommandRequestWithoutQS.retain()
        testChannel.writeOneInbound(testBatCommandRequestWithoutQS)

        Thread.sleep(1000)
        assertEquals(
            2, commandRepo.pointerMap!!.size, "failed because pointemap size is " +
                    "${commandRepo.pointerMap!!.size}, " +
                    "pointermap:${commandRepo.pointerMap!!.keys}"
        )

        val serializableCommandDirectory = commandRepo.serializedCommandsDir


        runBlocking {
            delay(250)
            val serializedCommandsInDir = Files.newDirectoryStream(serializableCommandDirectory).count()
            assertEquals(2, serializedCommandsInDir)

        }
    }


    @Test
    fun testGetCommandRequest() {


        Thread.sleep(250)
        val testChannel = EmbeddedChannel(commandHandler)
        Thread.sleep(450)
        runBlocking {
            commandRepo.addToRepo(mockCommand)
        }
        Thread.sleep(450)
        assert(!commandRepo.pointerMap!!.isEmpty())
        val testGetRequest = moshi.generateCommandFullRequest(
            HttpMethod.GET,
            null,
            false,
            null
        )
        testChannel.writeInbound(testGetRequest)
        Thread.sleep(650)
        val receivedMsg = (testChannel.readOutbound() as FullHttpResponse)
            .also { assert(it.content().readableBytes() > 0) }
        val receivedCommandSet =
            serializedCommandSetAdapter.fromJson(receivedMsg.content().toString(StandardCharsets.UTF_8))
        assertEquals(1, receivedCommandSet!!.size)

    }


    @Test
    fun checkDeleteRequestWorks() {
        val testChannel = EmbeddedChannel(commandHandler)
        val serizalizedBatCommandSet = setOf(batCommand).getByteBuf(moshi)
        val serializedMockCommandSet = setOf(mockCommand).getByteBuf(moshi)
        val testGetRequest = moshi.generateCommandFullRequest(
            HttpMethod.GET,
            null,
            false,
            null
        )
        runBlocking {
            commandRepo.addToRepo(batCommand)
            delay(600)
            assertEquals(1, commandRepo.pointerMap!!.size)
            testChannel.writeInbound(testGetRequest)
            delay(800)
            val receivedMsg: FullHttpResponse = testChannel.readOutbound() as FullHttpResponse
            val receivedCommands =
                serializedCommandSetAdapter.fromJson(receivedMsg.content().toString(StandardCharsets.UTF_8))!!
                    .getByteBuf(moshi)
            receivedCommands.retain()
            delay(259)

            val testDeleteRequest = moshi.generateCommandFullRequest(
                HttpMethod.DELETE,
                null,
                false,
                receivedCommands
            )

            testChannel.writeInbound(testDeleteRequest)
            delay(1000)
            assertEquals(0, commandRepo.pointerMap!!.size)

        }
    }

    @Test
    fun checkOnlyCacheWorks() {
        runBlocking{
            val testChannel = EmbeddedChannel(commandHandler)
            assertEquals(0, commandRepo.pointerMap!!.size)
            val cacheOnlyRequest = moshi.generateCommandFullRequest(HttpMethod.POST,CommandStrategy.ONLYCACHE,false,
                setOf(batCommand))

            testChannel.writeInbound(cacheOnlyRequest)
            delay(750)
            assertEquals(1,commandRepo.pointerMap!!.size)

            val fckdCacheOnlyRequest = moshi.generateCommandFullRequest(HttpMethod.POST,CommandStrategy.ONLYCACHE,true,
                setOf(mockCommand))
            testChannel.writeInbound(fckdCacheOnlyRequest)
            delay(750)
            assertEquals(2,commandRepo.pointerMap!!.size)
        }
    }
}


fun Set<SerializableCommand>.getByteBuf(moshi: Moshi): ByteBuf {
    val serializableCommandToMapAdapter = moshi.adapter<Set<SerializableCommand>>(
        Types.newParameterizedType(
            Set::class.java,
            SerializableCommand::class.java
        )
    )
    val buffToReturn = Unpooled.buffer()
    buffToReturn.writeCharSequence(serializableCommandToMapAdapter.toJson(this), Charsets.UTF_8)
    return buffToReturn
}


fun Moshi.generateCommandFullRequest(
    method: HttpMethod,
    strategy: CommandStrategy?,
    fuckStrategyUp: Boolean,
    commandSet: Set<SerializableCommand>?
): FullHttpRequest {

    val content: ByteBuf = commandSet?.getByteBuf(this) ?: Unpooled.EMPTY_BUFFER

    return this.generateCommandFullRequest(method, strategy, fuckStrategyUp, content)
}

fun Moshi.generateCommandFullRequest(
    method: HttpMethod,
    strategy: CommandStrategy?,
    fuckStrategyUp: Boolean,
    serializedCommandSet: ByteBuf
): FullHttpRequest {
    val goodStrategyMap: Map<CommandStrategy, String> = mapOf(
        CommandStrategy.GET to CommandStrategy.GET.name,
        CommandStrategy.PUT to CommandStrategy.PUT.name,
        CommandStrategy.POSTFAIR to CommandStrategy.POSTFAIR.name,
        CommandStrategy.POSTNONFAIR to CommandStrategy.POSTNONFAIR.name,
        CommandStrategy.ONLYCACHE to CommandStrategy.ONLYCACHE.name,
        CommandStrategy.DELETE to CommandStrategy.DELETE.name
    )
    val goodStrategyString = "STRATEGY"

    val fuckedUpStrategyMap: Map<CommandStrategy, String> = mapOf(
        CommandStrategy.GET to CommandStrategy.GET.name.lowercase(),
        CommandStrategy.PUT to CommandStrategy.PUT.name.lowercase(),
        CommandStrategy.POSTFAIR to CommandStrategy.POSTFAIR.name.lowercase(),
        CommandStrategy.POSTNONFAIR to CommandStrategy.POSTNONFAIR.name.lowercase(),
        CommandStrategy.ONLYCACHE to CommandStrategy.ONLYCACHE.name.lowercase(),
        CommandStrategy.DELETE to CommandStrategy.DELETE.name.lowercase()
    )
    val fuckedUpStrategyString = "StRaTegY"


    val qsEncoder = QueryStringEncoder("/Command")
    strategy?.let {
        if (it != CommandStrategy.GET &&
            it != CommandStrategy.DELETE &&
            it != CommandStrategy.PUT
        ) {
            when (fuckStrategyUp) {
                true -> {
                    qsEncoder.addParam(fuckedUpStrategyString, fuckedUpStrategyMap.get(it))
                }
                false -> {
                    qsEncoder.addParam(goodStrategyString, goodStrategyMap.get(it))
                }
            }
        }
    }

    return DefaultFullHttpRequest(
        HttpVersion.HTTP_1_1,
        method,
        qsEncoder.toString(),
        serializedCommandSet
    ).also {
        it.headers().add(HttpHeaderNames.HOST, "127.0.0.1:34747")
        it.headers().add(HttpHeaderNames.CONTENT_LENGTH, serializedCommandSet.readableBytes())
        it.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
        it.headers().add(HttpHeaderNames.AUTHORIZATION, "cococo")
        it.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)


    }
}



