package com.uj.rcbackend.IntrestingTests

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationObserver
import java.io.File
import java.nio.file.Paths

fun main(){
    println("Digits in number 100 : ${digitsInNumber(100)}")
    println("Digits in number 1 : ${digitsInNumber(1)}")
    println("Digits in number 1000 :${digitsInNumber(1000)}")
}
fun digitsInNumber(numb: Int,holder:Int=0):Int{
    val preResult:Int=numb/10
    if(preResult>=10){
        return digitsInNumber(preResult,holder+1)
    }else{
        if(preResult==0){
            return 1+holder
        }else{
            return 2+holder
        }
    }
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