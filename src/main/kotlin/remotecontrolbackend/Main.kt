package remotecontrolbackend

import APP_COROUTINE_CONTEXT_LITERAL
import DaggerMainComponent
import kotlinx.coroutines.*
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.dns_sd_part.DnsSdManager
import remotecontrolbackend.file_service_part.FileService
import remotecontrolbackend.netty_part.NettyConnectionManager
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

const val ROOT_DIR="j:\\Ujtrash\\Test\\"
const val PORT = 34747
fun main() {
   runBlocking {

        val launcher=DaggerMainComponent.builder()
            .setPort(PORT)
            .setWorkDirectory(Paths.get(ROOT_DIR))
            .isTestRun(false)
            .isSSLEnabled(false)
            .isAuthEnabled(false)
            .buildMainComponent()
            .getLauncher()

       launcher.launch()
//           Main(mainComponent).launch()
    awaitCancellation()
   }

}
@Singleton
class MainLauncher @Inject constructor (@Named(APP_COROUTINE_CONTEXT_LITERAL)coroutineContext: CoroutineContext) {
@Inject
lateinit var commandInvoker: CommandInvoker

    @Inject
    lateinit var nettyConnectionManager: NettyConnectionManager

    @Inject
    lateinit var dnsSdManager: DnsSdManager
    @Inject
    lateinit var fileService: FileService

    @Named(APP_COROUTINE_CONTEXT_LITERAL)
    @Inject
    lateinit var appCoroutineContext:CoroutineContext

    fun launch() {
//TODO Это все очень сыро

        val appScope= CoroutineScope(appCoroutineContext)
         nettyConnectionManager.launchNetty()
        appScope.launch {
            fileService.initializeFileService()
        }
        commandInvoker.launchCommandInvoker()
        appScope.launch(Dispatchers.IO) { dnsSdManager.launchDnsSd(this) }


    }
}

