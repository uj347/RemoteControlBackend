package com.uj.rcbackend.fileservicepart

import kotlinx.coroutines.*
import org.apache.commons.compress.archivers.zip.Zip64Mode
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.utils.IOUtils
import com.uj.rcbackend.dagger.FileServiceModule.Companion.FILESERVICE_COROUTINE_CONTEXT_LITERAL
import java.io.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext


class ZipStreamer (private val context:CoroutineContext, val pipeBuffSize:Int=8128, val compressionLevel:CompsressionLevel=CompsressionLevel.STORED) {
   @Inject constructor(@Named(FILESERVICE_COROUTINE_CONTEXT_LITERAL)context:CoroutineContext):this(context,8128,CompsressionLevel.STORED)



private val scope:CoroutineScope
init{
    scope= CoroutineScope(context+ SupervisorJob(context.job))
}


    fun zipThisFiles(files: Collection<File>):Pair<InputStream,Job>{
        val pipeOut=PipedOutputStream()
        val zipJob=scope.launch(
            Dispatchers.IO,
            start = CoroutineStart.LAZY
        ) {

            ZipArchiveOutputStream(pipeOut).use { zipStream->
             val compsressionInt:Int=when(compressionLevel){
                 CompsressionLevel.STORED->{ZipArchiveOutputStream.STORED}
                 CompsressionLevel.DEFAULT->{ZipArchiveOutputStream.DEFAULT_COMPRESSION}
                 CompsressionLevel.DEFLATED->{ZipArchiveOutputStream.DEFLATED}
             }
                zipStream.setUseZip64(Zip64Mode.Always)
                zipStream.setLevel(compsressionInt)
                for(file in files) {
                    toZip(zipStream, file, file.name)
                }
                zipStream.flush()
                zipStream.finish()
            }
            pipeOut.close()
        }
        return PipedInputStream(pipeOut, pipeBuffSize) to zipJob
     }

    companion object{
        enum class CompsressionLevel{
            STORED,
            DEFLATED,
            DEFAULT
        }


        private fun toZip(zipStream: ZipArchiveOutputStream, file: File, fileName: String) {
            val zipEntry = ZipArchiveEntry(file, fileName)
            zipStream.putArchiveEntry(zipEntry)
            if (file.isDirectory) {
                zipStream.closeArchiveEntry()
                file.listFiles()?.forEach {
                    toZip(zipStream, it, fileName +"\\"+it.name)
                }
            } else {
                IOUtils.copy(file, zipStream)
                zipStream.flush()
                zipStream.closeArchiveEntry()
            }

        }
    }

}