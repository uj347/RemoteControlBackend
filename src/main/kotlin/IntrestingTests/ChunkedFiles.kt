package IntrestingTests

import io.netty.channel.*
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedStream
import io.netty.handler.stream.ChunkedWriteHandler
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.utils.IOUtils
import com.uj.rcbackend.ROOT_DIR
import java.io.*
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists


class DummyHandler :ChannelInboundHandlerAdapter()

class RemoveDummyHandler :ChannelInboundHandlerAdapter(){
    override fun channelActive(ctx: ChannelHandlerContext?) {
        ctx?.let{
            println("Remove dummy in action")
            val pipeIterator=it.pipeline().iterator()
            while(pipeIterator.hasNext()){
                val next=pipeIterator.next()
                if(next.value is DummyHandler){
                    pipeIterator.remove()
                    println("Dummy removed")
                }
            }
        }
        super.channelActive(ctx)
    }
}

class Zipper : SimpleChannelInboundHandler<Path>() {

    fun startZippin(filePath: Path): ChunkedStream {
        val pipeIn = PipedInputStream(12000)
        val pipeOut = PipedOutputStream(pipeIn)
        val chunkedStream = ChunkedStream(pipeIn, 12000)
            Thread{
            println("Before zip stream construction in executor block in Thread: ${Thread.currentThread().name}")
            ZipArchiveOutputStream(pipeOut).use { zipStream ->
//TODO
                zipStream.putArchiveEntry(ZipArchiveEntry(File(ROOT_DIR+"testbooba.flac"), "testBooba.flac"))
                IOUtils.copy(
                    BufferedInputStream(FileInputStream(ROOT_DIR)),
                    zipStream
                )
                zipStream.closeArchiveEntry()
                zipStream.finish()

                println("End of zip block")
            }
        }.start()
        return chunkedStream
    }


    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Path?) {

        msg?.let { fileToWrite ->
            ctx?.let { context ->
//Possibly replace with Thread()
                context.write(HttpChunkedInput(startZippin(fileToWrite)))
                println("chunked stream written from Thread ${Thread.currentThread().name}")
                context.flush()
            }
        }
//    chunkedFuture,.await()
    }


}


class TestFileHandler : ChannelInboundHandlerAdapter() {
    var zipCompleted: ChannelPromise? = null

    override fun channelActive(ctx: ChannelHandlerContext?) {

        println("In testFileHAndler Channel registered")
//            j:\\testbooba.flac

        val headers = DefaultHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.POST,
            "/files/zalupa/boroda/pisya.boo"
        ).also {
            it.headers().add(HttpHeaderNames.HOST, "localhost:34747")
            it.headers().add(HttpHeaderNames.CONTENT_TYPE, "audio/flac")
            it.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
        }

        ctx?.writeAndFlush(headers)
        println("Rq is written")
        ctx?.fireChannelRead(Paths.get(ROOT_DIR))
        println("Path written")
    }

}





class FileAccumHandler(val fileToCreate: Path = Paths.get(ROOT_DIR)) :
    ChannelOutboundHandlerAdapter() {

    private val chunksReaded: AtomicInteger = AtomicInteger(0)

    val done: AtomicBoolean = AtomicBoolean(false)

    private var inited: AtomicBoolean = AtomicBoolean(false)
    private var raf: RandomAccessFile? = null
    private val lastPosition
        get() = raf?.filePointer


    fun init() {
        if (!inited.get()) {
            if (fileToCreate.exists()) {
                fileToCreate.deleteExisting()
            }
            fileToCreate.createFile()
            raf = RandomAccessFile(fileToCreate.toFile(), "rw")
            inited.set(true)
        }
    }

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        init()
        super.handlerAdded(ctx)
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        msg?.let { message ->
            when (message) {
                is DefaultHttpContent -> {
                    raf!!.let {
                        chunksReaded.getAndIncrement()
                        //TODO
                        val nioBuf = message.content().nioBuffer()
                        message.release()
                        while (nioBuf.hasRemaining()) {
                            it.channel.write(nioBuf)
                        }
                        nioBuf.clear()
                    }
                }
                is LastHttpContent -> {
                    println("Last chunk readed")

                    done.set(true)
                    println("Final report:\nChunks readed:${chunksReaded.get()}\nRAF size is :${raf!!.length()}")
                   ctx?.writeAndFlush("Most be ready")
                    println("After most be ready")
                    raf!!.close()
                    println("After RAF closed" )
                    ctx?.channel()!!.close()
                }

                else -> {
                    "smth else happend"
                }

            }
        }
    }
    }

fun main() {
    val testChannel = EmbeddedChannel()
    val testPipeline = testChannel.pipeline()
    testPipeline.addFirst(DummyHandler(),RemoveDummyHandler())
    testPipeline.addLast(FileAccumHandler())
    testPipeline.addLast(
        TestFileHandler(),
        ChunkedWriteHandler()
    )

    testPipeline.addLast(
//        DefaultEventExecutorGroup(2) ,
        Zipper()
    ).fireChannelActive()
    var notDone=true
    while(notDone){
        testChannel.closeFuture().addListener { notDone=false}
        if(testChannel.readOutbound<Any>()=="Most be ready"){
            println("Done")
            break
        }

    }



    //testChannel.closeFuture().await()
}



