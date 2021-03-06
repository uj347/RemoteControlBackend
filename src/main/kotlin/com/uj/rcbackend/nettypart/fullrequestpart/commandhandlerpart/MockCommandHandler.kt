package com.uj.rcbackend.nettypart.fullrequestpart.commandhandlerpart

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.commandinvokerpart.commandinvoker.CommandInvoker
import com.uj.rcbackend.nettypart.utils.SpecificChain
import java.nio.charset.Charset
import javax.inject.Inject

@SpecificChain(chainType = SpecificChain.ChainType.FULLREQUEST)
@Sharable
class MockCommandHandler @Inject constructor(commandInvoker: CommandInvoker): AbstractCommandHandler(commandInvoker){

    companion object {
        val PASS_TROUGH_STRING="Command passed trough the command handler"
        val logger= LogManager.getLogger()
    }


    override val logger: Logger= Companion.logger


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


     }
    }
}