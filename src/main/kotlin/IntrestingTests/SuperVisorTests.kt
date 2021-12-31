package IntrestingTests

import kotlinx.coroutines.*
import java.lang.RuntimeException

fun main(){
    val superScope= CoroutineScope(Dispatchers.IO+ SupervisorJob())
    runBlocking {
    superScope.launch (CoroutineExceptionHandler {
        cont,ex->
        println("$ex happend in $cont")
    }
    ){ throw RuntimeException("SukaBLLyat") }
    val infiniteJob=superScope.launch{ while(true){
        delay(3000)
        println("I'm still alive and supeScope alive: ${superScope.isActive}")
    }
    }
        delay(5000)
        infiniteJob.cancel()
        println("Main is runing: ${superScope.isActive}")
    superScope.coroutineContext.job.cancelAndJoin()
        println("Main is runing: ${superScope.isActive}")
    }


}

class SuperVisorTests