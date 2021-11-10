package remotecontrolbackend.netty_part.chunked_part.chunked_request_handler_part

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.chunked_part.ChunkWorkModeHandler
import remotecontrolbackend.netty_part.chunked_part.robot_handler_part.ConcreteRobotHandler
import remotecontrolbackend.netty_part.full_request_part.FullRequestWorkModeHandler
import remotecontrolbackend.netty_part.full_request_part.full_request_router_part.ConcreteFullRequestRouter
import remotecontrolbackend.netty_part.send501Response
import javax.inject.Inject
//TODO
@ChannelHandler.Sharable
@NettyScope
class ConcreteChunkedRequestRouter @Inject constructor():AbstractChunkedRequestRouter() {
   companion object{
       val logger=LogManager.getLogger()
   }

    @Inject
  lateinit var chunkedRequestHandlers:Map<String,@JvmSuppressWildcards ChunkWorkModeHandler>

  override fun handlerAdded(ctx: ChannelHandlerContext?) {
        super.handlerAdded(ctx)
      logger.debug("ConcreteChunkedRequestRouter added to pipeline")
    }



    override fun channelRead0(ctx: ChannelHandlerContext?, msg: HttpRequest?) {
        logger.debug("In channelRead0")
        if (HttpUtil.is100ContinueExpected(msg)) {
            logger.debug("Received 100Continue")
            ctx?.writeAndFlush(DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE))
        }
        msg?.let {
            val httpMsg=it
            val queryStringDecoder = QueryStringDecoder(it.uri())
            val workMode=queryStringDecoder.path().lowercase().substring(1)
            logger.debug("Received message with workMode: $workMode")
            when (workMode) {
                in chunkedRequestHandlers -> {
                    ctx?.let{
                        val targetHandler=chunkedRequestHandlers.get(workMode)!!
                        logger.debug("Modifying pipeline, adding handler: ${targetHandler.handlerDescription}")
                        it.pipeline().addAfter(NettySubComponent.CHUNKED_REQUEST_ROUTER_LITERAL,targetHandler.handlerDescription ,targetHandler)
                        logger.debug("Firing read to the next handler")
                        it.fireChannelRead(httpMsg)

                    }

                }


                else -> {
                    logger.debug("Unsupported work mode received, sending 501 msg")
                    ctx.send501Response()
                    ctx?.close()
                }
            }
        }
    }
}