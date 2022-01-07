
package com.uj.rcbackend

import APP_COROUTINE_CONTEXT_LITERAL
import DaggerMainComponent
import ROOT_DIR_LITERAL
import kotlinx.coroutines.*
import com.uj.rcbackend.commandinvokerpart.commandinvoker.CommandInvoker
import com.uj.rcbackend.configurationutilities.PropertiesEngine
import com.uj.rcbackend.configurationutilities.Property
import com.uj.rcbackend.configurationutilities.feedgenerators.DependentFeedGenerator
import com.uj.rcbackend.dnssdpart.DnsSdManager
import com.uj.rcbackend.fileservicepart.FileService
import com.uj.rcbackend.nettypart.NettyConnectionManager
import io.netty.channel.ChannelOutboundHandlerAdapter
import org.h2.security.auth.H2AuthConfig
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.exists

/**
 * Пропертисы в файлах *.properties, либо в аргументах в формате "name==value1&&value2&&value3......."
 * Так же необходимо задать проперти препроцессора, ну в данном случае - это единственный параметр "plup"
 * Пропертисы препроцессора по задумке читаются только из Аргументов, файлы игнорируются (Это настраивается)
 * */


const val ROOT_DIR="C:\\Ujtrash\\TestRun\\"
const val PORT = 34747
 public val appProperties=setOf(
    Property("port", defaultValues = setOf("0"), required = true, multivalued = false),
    Property("workdir",setOf("workdir","wd","dir"),
        multivalued = false, required = true,
        defaultValues = setOf(System.getProperty("user.dir")),
        valuePredicate = {v->
            println("In predicate")
            Paths.get(v).isAbsolute}),
    Property("ssl", multivalued = false,required=true, defaultValues = setOf("false")),
    Property("auth", required=true, defaultValues = setOf("false"), multivalued = false),
    Property("password", argPrefixes = setOf("pwd","password","pswd"),required = true, multivalued = false)
)
val propertiesPreprocessorDependencies= setOf(DependentFeedGenerator.propertiesLookUpPathsDependency)



fun main(args:Array<String>) {

   runBlocking {
       val propertiesEngine=PropertiesEngine.Factory(propertiesPreprocessorDependencies).newInstance(args.asList())
       val processedProperties=propertiesEngine.fire(appProperties.map{it.generateNotConfiguredClone()})

       processedProperties.map{p->p.propertyName to p}.toMap().let{
           val launcher=DaggerMainComponent.builder()
               .setPort(it.getValue("port").getSingleLong().toInt())
               .setWorkDirectory(Paths.get(it.getValue("workdir").getSingleString()))
               .isSSLEnabled(it.getValue("ssl").getSingleBooleanValue())
               .isAuthEnabled(it.getValue("auth").getSingleBooleanValue())
               .dbPassword(it.getValue("password").getSingleString())
               .buildMainComponent()
               .getLauncher()

           launcher.launch()
       }

//        val launcher=DaggerMainComponent.builder()
//            .setPort(PORT)
//            .setWorkDirectory(Paths.get(ROOT_DIR))
//            .isSSLEnabled(false)
//            .isAuthEnabled(false)
//            .dbPassword("1234")
//            .buildMainComponent()
//            .getLauncher()

//       launcher.launch()
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
            dnsSdManager.launchDnsSd(this)
            commandInvoker.launchCommandInvoker()
        }
    }
}
