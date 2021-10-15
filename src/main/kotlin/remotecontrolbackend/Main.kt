package remotecontrolbackend

import DaggerMainComponent
import kotlinx.coroutines.*
import remotecontrolbackend.dns_sd_part.DnsSdManager
import remotecontrolbackend.netty_part.NettyConnectionManager
import java.nio.file.Paths
import javax.inject.Inject


const val PORT = 34747
fun main() {
    runBlocking {
        supervisorScope {
            launch { Main().launch(this) }
        }

        awaitCancellation()
    }

}

class Main() {
    init {
        DaggerMainComponent.builder().setPort(PORT).setWorkDirectory(Paths.get("J:\\InvokerTest\\")).isTestRun(true).buildMainComponent().inject(this)
    }

    @Inject
    lateinit var nettyConnectionManager: NettyConnectionManager

    @Inject
    lateinit var dnsSdManager: DnsSdManager
    fun launch(coroutineScope: CoroutineScope) {
//TODO Это все очень сыро
        coroutineScope.launch(Dispatchers.IO) { nettyConnectionManager.launchNetty(this) }
        coroutineScope.launch(Dispatchers.IO) { dnsSdManager.launchDnsSd(this) }


    }
}

