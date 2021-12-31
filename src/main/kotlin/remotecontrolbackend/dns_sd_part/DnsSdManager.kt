package remotecontrolbackend.dns_sd_part


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import remotecontrolbackend.dagger.DnsSdScope
import remotecontrolbackend.dagger.DnsSdSubComponent
import java.net.InetAddress
import javax.inject.Named
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

@DnsSdScope
class DnsSdManager (val dnsSdSubComponent: DnsSdSubComponent.DnsSdSubComponentBuilder,
                 @Named("port")  var servicePort:Int) {

    init {
        dnsSdSubComponent.buildDnsSdSubComp().inject(this)
    }
    var dnsSdJob: Job?=null
    val serviceName:String="NoNameUjService"
    val serviceType: String="_http._tcp.local."
    var serviceAdress:InetAddress=InetAddress.getLocalHost()
    val jmDns:JmDNS=JmDNS.create(serviceAdress,serviceName)

    val dnsSdInfo:ServiceInfo= ServiceInfo.create(serviceType,serviceName,servicePort,"Remote Control Service")
    //todo переделать в контекст-аксептинг
     fun launchDnsSd(coroutineScope: CoroutineScope){

        dnsSdJob=coroutineScope.launch {
            println("Before registering service in DNS SD")
            jmDns.registerService(dnsSdInfo)
            println("${dnsSdInfo.name} service was registered in DNS SD")
        }
    }

    suspend fun stopDnsSd(){
        jmDns.unregisterAllServices()
        jmDns.close()
        dnsSdJob?.cancel()
        println("DNS SD services was removed")
    }
}