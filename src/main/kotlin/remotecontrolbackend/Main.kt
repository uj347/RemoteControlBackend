package remotecontrolbackend

import APP_COROUTINE_CONTEXT_LITERAL
import DaggerMainComponent
import ROOT_DIR_LITERAL
import kotlinx.coroutines.*
import org.apache.logging.log4j.core.lookup.EnvironmentLookup
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.dns_sd_part.DnsSdManager
import remotecontrolbackend.file_service_part.FileService
import remotecontrolbackend.netty_part.NettyConnectionManager
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.exists

const val ROOT_DIR="j:\\Ujtrash\\TestRun\\"
const val PORT = 34747
fun main() {

   runBlocking {

        val launcher=DaggerMainComponent.builder()
            .setPort(PORT)
            .setWorkDirectory(Paths.get(ROOT_DIR))
            .isSSLEnabled(false)
            .isAuthEnabled(false)
            .dbPassword("1234")
            .buildMainComponent()
            .getLauncher()

       launcher.launch()
    awaitCancellation()
   }

}
@Singleton
class MainLauncher @Inject constructor (@Named(APP_COROUTINE_CONTEXT_LITERAL)coroutineContext: CoroutineContext) {
    companion object{
        fun killAppWithMessage(killMsg:String?=null){
            val defaultMsg="UNKNOWN REASON"
            println("App terminated because of: ${killMsg?:defaultMsg}")
            System.exit(-1)
        }
    }
@Inject
lateinit var commandInvoker: CommandInvoker

    @Inject
    lateinit var nettyConnectionManager: NettyConnectionManager

    @Named(ROOT_DIR_LITERAL)
    @Inject
    lateinit var rootDir:Path

    @Inject
    lateinit var dnsSdManager: DnsSdManager
    @Inject
    lateinit var fileService: FileService

    @Named(APP_COROUTINE_CONTEXT_LITERAL)
    @Inject
    lateinit var appCoroutineContext:CoroutineContext

    fun launch() {

        if(!rootDir.exists()){
            kotlin.runCatching {
                Files.createDirectories(rootDir)
            }.onFailure { killAppWithMessage("Unable to create root directory") }
        }

        val appScope= CoroutineScope(appCoroutineContext)
         nettyConnectionManager.launchNetty()
        appScope.launch {
            fileService.initializeFileService()
        }
        commandInvoker.launchCommandInvoker()
        appScope.launch(Dispatchers.IO) { dnsSdManager.launchDnsSd(this) }


    }
}

