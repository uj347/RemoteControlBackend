import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelPipeline
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.junit.Before
import org.junit.Test
import com.uj.rcbackend.moshi.PathAdapter
import com.uj.rcbackend.nettypart.chunkedpart.filehandlerpart.ConcreteFileHandler
import com.uj.rcbackend.nettypart.chunkedpart.filehandlerpart.DispatchResult
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.exists
import kotlin.test.assertEquals

class FileHandlerTest {
    val testFilePath=Paths.get(TEST_DIRECTORY).resolve("tst.mp3")
    val dropBoxPath=Paths.get(TEST_DIRECTORY).resolve("FileService\\Dropbox")
    val testFileProvider={ setOf(testFilePath) }
val mainComponent=DaggerMainComponent
    .builder()
    .setWorkDirectory(Paths.get(TEST_DIRECTORY))
    .setPort(34444)
    .setPathProvider(testFileProvider)
    .dbPassword(TEST_PSWD)
    .isSSLEnabled(false)
    .isAuthEnabled(false)
    .buildMainComponent()
    val nettyConnectionManager=mainComponent.getNettyConnectionManager()
    val fileService=mainComponent.getFileService()
    val nettySubcomponent=mainComponent.getNettySubcomponentBuilder().buildNettySubcomponent()
    val moshi= Moshi.Builder().add(PathAdapter()).build()
    val moshiPathAdapter = moshi.adapter<Set<Path>>(Types.newParameterizedType(Set::class.java, Path::class.java))


    var testChannel:EmbeddedChannel=EmbeddedChannel()
    val testPipeline:ChannelPipeline
            get()=testChannel.pipeline()
    val chunkedInterceptor = nettySubcomponent.getChunkedInterceptor()


        @Before
        fun before() {
            runBlocking {
                fileService.reInitializeFileService()
                testChannel= EmbeddedChannel()
                testPipeline
                    .addLast(HttpRequestDecoder())
                    .addLast(chunkedInterceptor.handlerDescription, chunkedInterceptor)
                assert(fileService.initialized)
                assert(testFilePath in fileService.getAllPaths())
            }
        }




    @Test
    fun checkAvailablePathsWorks(){
        runBlocking{

            println("Current test pipeline is ${testPipeline.names()}")
            val testRequest =
                DefaultHttpRequest(
                    HttpVersion.HTTP_1_1,
                    HttpMethod.GET,
                    "/file/${DispatchResult.AVAILABLEFILES_LITERAL}"
                ).also {
                    it.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
                }

            testChannel.writeInbound(testRequest)
            assert(testPipeline.get(ConcreteFileHandler::class.java) != null)
            val receivedAnswer = testChannel.readOutbound<FullHttpResponse>().content().toString(StandardCharsets.UTF_8)

            assert(receivedAnswer.isNotBlank())
        }
    }

    @Test
    fun fileUploadingWorks() {
       runBlocking {
            println("Current test pipeline is ${testPipeline.names()}")
            val stringJsonPath = moshiPathAdapter.toJson(setOf(testFilePath))
            val testRequest =
                DefaultHttpRequest(
                    HttpVersion.HTTP_1_1,
                    HttpMethod.GET,
                    "/file/${DispatchResult.BODY_LISTED_FILES_LITERAL}"
                ).also {
                    it.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
                }
           val testContent=DefaultHttpContent(Unpooled.wrappedBuffer(stringJsonPath.encodeToByteArray()))
            val msgCounter: AtomicLong = AtomicLong(0)
           val testFileSize:Long=FileUtils.sizeOf(testFilePath.toFile())
           val size:AtomicLong=AtomicLong(0)
            testChannel.writeInbound(testRequest)
           testChannel.writeInbound(testContent)
           testChannel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT)

           while(null!=testChannel.readOutbound<Any?>().also {
               if(it!=null&&it is HttpContent){
                   it.content().readableBytes().let{
                       size.addAndGet(it.toLong())
                   }
               }
               }
           ){
                msgCounter.incrementAndGet()
            }
            delay(500)
            println("counter is ${msgCounter.get()}")
           println("Received size is: ${size.get()}")
           assertEquals(testFileSize,size.get())
            assert(msgCounter.get() > 0)

        }
    }

    @Test
    fun filePostWorks(){
        runBlocking {
            FileUtils.listFiles(dropBoxPath.toFile(),TrueFileFilter.TRUE,null).forEach{
                FileUtils.delete(it)
            }
            println("Current test pipeline is ${testPipeline.names()}")
            val testRequest =
                DefaultHttpRequest(
                    HttpVersion.HTTP_1_1,
                    HttpMethod.POST,
                    "/file/test.mp3"
                ).also {
                    it.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
                }
            val chunkedHttp=HttpChunkedInput(ChunkedStream(FileInputStream(testFilePath.toFile())))
            testChannel.writeInbound(testRequest)
            while(!chunkedHttp.isEndOfInput){
                chunkedHttp.readChunk(Unpooled.buffer().alloc()).let { testChannel.writeInbound(it) }
            }
            testChannel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT)
            delay(2550)
            val resultingInputTestFile=dropBoxPath.resolve("test.mp3")
            assertEquals(true,resultingInputTestFile.exists())
            assertEquals(FileUtils.sizeOf(testFilePath.toFile()),FileUtils.sizeOf(resultingInputTestFile.toFile()))

        }
    }

}



