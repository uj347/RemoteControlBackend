package IntrestingTests

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.apache.commons.io.file.PathUtils
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationObserver
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.ConcurrentSkipListSet

fun main(){
val set=ConcurrentSkipListSet<String>()
set.addAll(listOf( "one", "two", "three") )
set.filter { it=="two" }.forEach {set.remove(it)}
    println("Good---set is ${set.toString()}")
}



fun main1(){

    val watchaDir= Paths.get("c:\\Ujtrash")
    val observer=FileAlterationObserver(watchaDir.toFile())

    val listener=object:FileAlterationListenerAdaptor(){
        override fun onFileCreate(file: File?) {
            file!!.prtMsg("FILE_CREATED")
        }

        override fun onDirectoryChange(directory: File?) {
            directory!!.prtMsg("DIR_CHANGED")
        }

        override fun onDirectoryCreate(directory: File?) {
            directory!!.prtMsg("DIR_CREATED")
        }

        override fun onFileChange(file: File?) {
            file!!.prtMsg("FILE_CHANGED")
        }
    }
    observer.addListener(listener)
    println("Listener added")

    runBlocking {
        observer.initialize()
        println("Observer initialized")
        val fileWatcherJob=launch(Dispatchers.IO){
            while(true){
                println("BREAKPOINT")
                delay(2000)
                observer.checkAndNotify()
            }
        }
        fileWatcherJob.join()

    }
}

fun File.prtMsg(string:String){
    println("${this.name} is $string")
}