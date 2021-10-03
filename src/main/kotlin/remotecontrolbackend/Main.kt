package remotecontrolbackend

import DaggerMainComponent
import kotlinx.coroutines.*
import remotecontrolbackend.dns_sd_part.DnsSdManager
import remotecontrolbackend.netty_part.NettyConnectionManager
import javax.inject.Inject


const val PORT = 34747
fun main() {
runBlocking {
   supervisorScope {
       launch { Main().launch(this) }
   }

awaitCancellation()
}
//    runBlocking {
//        supervisorScope {
//           launchNetty(PORT)
//        }
//        awaitCancellation()
//        println("Program is finished")
//    }
}

class Main() {
    init {
        DaggerMainComponent.builder().setPort(PORT).isTestRun(true).buildMainComponent().inject(this)
    }

    @Inject
    lateinit var nettyConnectionManager: NettyConnectionManager

    @Inject
    lateinit var dnsSdManager: DnsSdManager
    fun launch(coroutineScope:CoroutineScope) {

        coroutineScope.launch(Dispatchers.IO) { nettyConnectionManager.launchNetty() }
        coroutineScope.launch(Dispatchers.IO){ dnsSdManager.launchDnsSd() }


    }
}

