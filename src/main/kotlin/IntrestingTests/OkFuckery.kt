package IntrestingTests

import kotlinx.coroutines.*
import org.apache.commons.compress.utils.IOUtils
import com.uj.rcbackend.fileservicepart.ZipStreamer
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

fun main(){

   runBlocking{
       val largeBooba= setOf(File("J:\\preparedForTestingSSL"))
       supervisorScope {
       val storedZipper=ZipStreamer(this.coroutineContext)
           val deflZipper=ZipStreamer(this.coroutineContext, compressionLevel = ZipStreamer.Companion.CompsressionLevel.DEFLATED)
           val deflPath=Paths.get("j:\\Ujtrash\\zipLvls\\defl.zip")
           val storedPath= Paths.get("j:\\Ujtrash\\zipLvls\\stored.zip")
       ZipConsumer(deflZipper.zipThisFiles(largeBooba),deflPath).start()
           ZipConsumer(storedZipper.zipThisFiles(largeBooba),storedPath).start()
       }

//       zipper.zipScope.coroutineContext.job.invokeOnCompletion { this.cancel() }
   }
}

class ZipConsumer(val pair:Pair<InputStream,Job>,val filePath:Path){



    fun start(){
        if(!filePath.toFile().exists()){
            filePath.parent.createDirectories()
            filePath.createFile()
        }
         val(inputZip,job)=pair

        job.start()
        var readed:AtomicLong= AtomicLong(0)
        var mReaded:Long=0L
        val readerAr=ByteArray(240000)
        FileOutputStream(filePath.toFile()).use {fos->
            IOUtils.copy(inputZip,fos)
        }
//        while (inputZip.read(readerAr).also {readed.getAndAdd(it.toLong())
//            }!=-1){
//            if (readed.get()/1000000>mReaded){
//                mReaded=readed.get()/1000000
//                println("Readed from zip $mReaded MBytes")
//            }
//            if(job.isCancelled){
//                println("Woopsy, all fucked up")
//            }
//        }
        println("Consuming is ended")

    }}