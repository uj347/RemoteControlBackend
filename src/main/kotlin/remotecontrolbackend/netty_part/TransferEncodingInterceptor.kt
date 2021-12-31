package remotecontrolbackend.netty_part

import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent.Companion.TRANSFER_ENCODING_INTERCEPTOR_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.CHUNKED_REQUEST_ROUTER_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.HTTP_AGGREGATOR_LITERAL
import remotecontrolbackend.dagger.NettySubComponent.Companion.HTTP_CODEC_LITERAL
import remotecontrolbackend.netty_part.chunked_part.chunked_request_router_part.AbstractChunkedRequestRouter
import remotecontrolbackend.netty_part.full_request_part.full_request_router_part.AbstractFullRequestRouter
import remotecontrolbackend.netty_part.utils.SpecificChain
import javax.inject.Inject
import kotlin.reflect.full.hasAnnotation


@NettyScope
@Sharable
class TransferEncodingInterceptor @Inject constructor() : ChannelInboundHandlerAdapter() {


    @Inject
    lateinit var chunnkedRequestRouter: AbstractChunkedRequestRouter

    @Inject
    lateinit var fullRequestRouter: AbstractFullRequestRouter


    companion object {
        val logger = LogManager.getLogger()
        val handlerDescription = TRANSFER_ENCODING_INTERCEPTOR_LITERAL
    }

    val handlerDescription
        get() = Companion.handlerDescription


    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        msg?.let { message ->
            if (message is DefaultHttpRequest) {
                logger.debug("Interceptor received request: $message")
                if (HttpUtil.isTransferEncodingChunked(message)) {
                    logger.debug("Encoding of msg is CHUNKED, interceptor is reconfiguring pipeline")
                    ctx?.sanitizeFromChainSpecificHandlers()
                    ctx?.customizeToChunked()
                    logger.debug("Firing read on the chunked-modified pipeline")
                    ctx?.fireChannelRead(message)
                    } else {
                    logger.debug("Encoding of msg is NOT-CHUNKED, interceptor is reconfiguring pipeline")
                    ctx?.let { context->
                       context.sanitizeFromChainSpecificHandlers()
                        context.customizeToFullRequest()
                        logger.debug("Firing channeel read in FullRequest-configured pipeline")
                        context.fireChannelRead(message)
                    }
                }
            } else {
                logger.debug("Passing NON HTTP-REQUEST message trough without modification of pipeline, " +
                        "\n msg type is ${message::class.simpleName?:"NULL!!!!!!"}" +
                        "\n msg is EmptyLastContent: ${message is LastHttpContent}")
                ctx?.fireChannelRead(message)
            }
        }
    }

    fun ChannelHandlerContext.sanitizeFromChainSpecificHandlers(){
        val pipeIter =pipeline().iterator()
        while (pipeIter.hasNext()){
            val nextHandler=pipeIter.next().value
            if(nextHandler::class.hasAnnotation<SpecificChain>()){
                logger.debug("Pipeline sanitation, removing handler: [${nextHandler::class.simpleName}]")
            pipeline().remove(nextHandler)
            }
        }
    }

    fun ChannelHandlerContext.customizeToFullRequest(){
        logger.debug("Customizing pipeline to Full request mode")

        pipeline().addAfter(
            TRANSFER_ENCODING_INTERCEPTOR_LITERAL,
            AnnotatedHttpAgregator.handlerDescription,
            AnnotatedHttpAgregator(Int.MAX_VALUE)
            )

        pipeline().addAfter(
                AnnotatedHttpAgregator.handlerDescription,
                fullRequestRouter.handlerDescription,
                fullRequestRouter
            )



        logger.debug("Full-request customization of the pipeline finished\nCurrent pipeine is [${pipeline()}]")

    }

    fun ChannelHandlerContext.customizeToChunked(){
       logger.debug("Customizing pipeline to Chunked mode")
        pipeline().addAfter(
            TRANSFER_ENCODING_INTERCEPTOR_LITERAL,
            CHUNKED_REQUEST_ROUTER_LITERAL,
            chunnkedRequestRouter
        )
    }
}

@SpecificChain(SpecificChain.ChainType.FULLREQUEST)
class AnnotatedHttpAgregator(maxContentLength:Int,
                             closeOnExpectationFailed:Boolean=false)
    :HttpObjectAggregator(maxContentLength,closeOnExpectationFailed){
        companion object{
        val handlerDescription= HTTP_AGGREGATOR_LITERAL
        }
    }
