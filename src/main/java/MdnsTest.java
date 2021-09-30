import javax.jmdns.*;
import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class MdnsTest {
    static void registerNewService (JmDNS jmDNSInst,String serviceName) throws IOException {
        ServiceInfo newInfo=ServiceInfo.create("_http._tcp.local.",serviceName,34747,"generated");
        jmDNSInst.registerService(newInfo);
    }

    public static void main(String[] args) {
       try {

            JmDNS jmDNS = JmDNS.create(InetAddress.getLocalHost(),"name");
            jmDNS.addServiceListener("_http._tcp.local.", new ServiceListener() {
                @Override
                public void serviceAdded(ServiceEvent event) {
                   System.out.println("added service: " + event.getName());

                }

                @Override
                public void serviceRemoved(ServiceEvent event) {


                    System.out.println("removed service: " + event.getName());


                }

                @Override
                public void serviceResolved(ServiceEvent event) {

                     System.out.println("resolved service: " + event.getName());


                }
            });
           for (int i = 0; i <5 ; i++) {
           Thread.sleep(3000);
               registerNewService(jmDNS,"BullshitService# "+i);

           }
//           ServiceInfo info = ServiceInfo.create("_http._tcp.local.", "Bullshitservice", 34747, "This is my test service");
//            jmDNS.registerService(info);



           //Arrays.stream(jmDNS.list("_http._tcp.local.")).forEach(System.out::println);

           // jmDNS.unregisterAllServices();

while (true){
    Thread.sleep(15000);
    System.out.println("Still runnin");
}
//
        }catch (Exception e){
           System.out.println("Exception occured:" + e.getStackTrace());
       }
    }
}
