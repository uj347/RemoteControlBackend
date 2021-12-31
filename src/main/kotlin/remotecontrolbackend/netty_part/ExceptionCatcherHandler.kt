package remotecontrolbackend.netty_part

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.utils.Exception404
import remotecontrolbackend.netty_part.utils.Exception500
import remotecontrolbackend.netty_part.utils.HttpException


@ChannelHandler.Sharable
class ExceptionCatcherHandler : ChannelInboundHandlerAdapter() {
    companion object{
        val handlerDescription = NettySubComponent.EXCEPTION_CATCHER_LITERAL
        val logger=LogManager.getLogger()
    }
        override fun exceptionCaught(
            ctx: ChannelHandlerContext?,
            cause: Throwable?
        ) {
           if(cause!=null&&ctx!=null) {
               logger.error(cause)
              when (cause){
                  is HttpException->{
                      when(cause){
                          is Exception404->{ctx.send404Response(cause.message)}
                          is Exception500->{ctx.send500Response(cause.message)}
                      }
                  }
                  else->{ctx.send500Response("Unidentified internal cause")}
              }
           }

        }
    }
