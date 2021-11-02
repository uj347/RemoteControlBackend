package IntrestingTests

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.ssl.SslContextBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import java.io.File

fun main(){

val certLines=File("J:\\sslMagick\\client\\clientcert.pem").readLines().filter { it.lowercase().contains("certificate") }.count()
    println("this is cert file: ${certLines>0} with count:$certLines")
}
class TestDecoder{
    val logger: Logger=LogManager.getLogger()
   fun logMyShit(){
       println("Current logger is: $logger")
       logger.debug("COCOC")

   }
}