package IntrestingTests

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http.FullHttpRequest
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand

fun main(){
TestDecoder().logMyShit()
}
class TestDecoder{
    val logger: Logger=LogManager.getLogger()
   fun logMyShit(){
       println("Current logger is: $logger")
       logger.debug("COCOC")
   }
}