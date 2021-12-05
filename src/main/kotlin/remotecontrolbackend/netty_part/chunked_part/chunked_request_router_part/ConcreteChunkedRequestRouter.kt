package remotecontrolbackend.netty_part.chunked_part.chunked_request_router_part

import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.chunked_part.ChunkWorkModeHandler
import remotecontrolbackend.netty_part.send501Response
import remotecontrolbackend.netty_part.utils.ChunkedChain
import javax.inject.Inject
//TODO
@ChunkedChain
@Sharable
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
                    ctx?.let{context->
                        //Проверить не модифирован ли пайплайн уже и удалить чанкед Хэндлеры, даже если они подходят,
                        //вдруг у меня в будующем хватит ума сделать стэйтфул хэндлеры.
//                       PS- Кажись сделал это уже в абстракт хэндлере
//                       val pipelineIter= context.pipeline().iterator()
//                        while(pipelineIter.hasNext()){
//                            val nextEntry=pipelineIter.next()
//                            if(nextEntry.value::class in chunkedRequestHandlers.values.map { it::class }){
//                                logger.debug("Removing already present Chunked handler from pipeline")
//                                pipelineIter.remove()
//                            }
//                        }

                        val targetHandler=chunkedRequestHandlers.get(workMode)!!
                        logger.debug("Modifying pipeline, adding handler: ${targetHandler.handlerDescription}")
                        context.pipeline().addAfter(NettySubComponent.CHUNKED_REQUEST_ROUTER_LITERAL,targetHandler.handlerDescription ,targetHandler)
                        logger.debug("Firing read to the next handler")
                        context.fireChannelRead(httpMsg)

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