package remotecontrolbackend.netty_part.command_handler_part.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import kotlinx.coroutines.*
import okhttp3.MediaType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import remotecontrolbackend.command_invoker_part.command_hierarchy.Command
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import java.awt.PageAttributes
import java.nio.charset.Charset
import javax.inject.Inject

@ChannelHandler.Sharable

class MockCommandHandler @Inject constructor(commandInvoker: CommandInvoker):AbstractCommandHandler(commandInvoker){

    companion object {
        val PASS_TROUGH_STRING="Command passed trough the command handler"
       const val LOGGER_NAME="MockCommandHandlerLogger"
        val _logger= LogManager.getLogger(COMMAND_HANDLER_LOGGER_SUPERCLASS+"."+ MockCommandHandler.LOGGER_NAME)
    }

    override val logger: Logger= _logger


    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        super.handlerAdded(ctx)
        println("MockCommand handler added")
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
     msg?.let{
         val isKeepAlive=HttpUtil.isKeepAlive(msg)

         println("Command reckognized")
         ctx?.writeAndFlush(DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK)
             .also { it.headers().add(HttpHeaderNames.CONNECTION,"close")

                 val commandMsg:ByteBuf=ctx.alloc().buffer().also{
                    it.writeCharSequence(
                         PASS_TROUGH_STRING,
                         Charset.forName("UTF-8")
                     )
                 }
                 it.headers().add(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.TEXT_PLAIN)
                 if(isKeepAlive) {
                     it.headers().add(HttpHeaderNames.CONTENT_LENGTH, commandMsg.readableBytes())
                 }
                 it.content().writeBytes(commandMsg)

             })
         ctx?.fireChannelRead(PASS_TROUGH_STRING)
//         ctx?.writeAndFlush()

//         if (HttpUtil.getMimeType(msg)!="application/json"){
//             ctx?.writeAndFlush(DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST))
//         }

     }
    }
}