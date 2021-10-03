package remotecontrolbackend.dns_sd_part


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
    val serviceName:String="NoNameUjService"
    val serviceType: String="_http._tcp.local."
    var serviceAdress:InetAddress=InetAddress.getLocalHost()
    val jmDns:JmDNS=JmDNS.create(serviceAdress,serviceName)

    val dnsSdInfo:ServiceInfo= ServiceInfo.create(serviceType,serviceName,servicePort,"Remote Control Service")

    suspend fun launchDnsSd(){
        println("Before registering service in DNS SD")
jmDns.registerService(dnsSdInfo)
        println("${dnsSdInfo.name} service was registered in DNS SD")
    }

    suspend fun stopDnsSd(){
        jmDns.unregisterAllServices()
        println("DNS SD services was removed")
    }
}