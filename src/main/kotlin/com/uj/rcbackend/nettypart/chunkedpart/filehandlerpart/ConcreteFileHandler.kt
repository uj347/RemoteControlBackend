package com.uj.rcbackend.nettypart.chunkedpart.filehandlerpart

import com.squareup.moshi.Moshi
import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultHttpRequest
import io.netty.util.ReferenceCounted
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import com.uj.rcbackend.dagger.NettyMainModule.Companion.NETTY_COROUTINE_CONTEXT_LITERAL
import com.uj.rcbackend.dagger.NettyScope
import com.uj.rcbackend.fileservicepart.FileService
import com.uj.rcbackend.moshi.PathAdapter
import com.uj.rcbackend.nettypart.chunkedpart.filehandlerpart.contenthandler.FileBodyHandler.Companion.FILE_BODY_HANDLER_LITERAL
import com.uj.rcbackend.nettypart.send404Response
import com.uj.rcbackend.nettypart.utils.SpecificChain
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

@SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
@Sharable
@NettyScope
class ConcreteFileHandler @Inject constructor(
    @Named(NETTY_COROUTINE_CONTEXT_LITERAL) nettyCoroutineContext: CoroutineContext,
    fileService: FileService
) : AbstractFileHandler(nettyCoroutineContext, fileService) {
    companion object {
        val logger = LogManager.getLogger()
    }

    val moshi = Moshi.Builder().add(PathAdapter()).build()

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        if (!fileService.initialized) {
            runBlocking {
                handlerScope.launch {
                    logger.debug("In FileHandler onAdd FileService initializing")
                    fileService.initializeFileService()
                }.join()

            }
        }
    }


    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        msg?.let {
            logger.debug("Read msg of type: ${msg::class.simpleName ?: "SHIIIIIIEEEEET THERE IS NULL!!!!!"}, surely not null: ${msg != null}")
            runBlocking {
                logger.debug("AfterLaunch block join")

                when (msg) {
                    is DefaultHttpRequest -> {
                        handlerScope.launch {
                            logger.debug("Starting processing of HTTP Header Part")
                            val dispatchToken = msg.dispatchRequest()

                            if (dispatchToken is NotConsistentDispatch) {
                                logger.debug("Spotted inconsistent dispatch, sending 404 response")
                                ctx.send404Response()
                                return@launch
                            }
                            if (dispatchToken.isBodyProcessingNeeded) {
                                logger.debug("Body processing needed appending Body handler")
                                ctx!!.appendBodyHandler(fileService, dispatchToken)
                                return@launch
                            } else {
                                logger.debug("There is no need in body processing, http body will be discarded")
                                ctx!!.processRequestOnly(fileService, moshi, dispatchToken)
                            }
                            logger.debug("Ended processing of HTTP Header Part")
                        }.join()
                    }
                    else -> {
                        handlerScope.launch {
                            logger.debug("In HTTP body msg processing part")
                            logger.debug("Passing trough msg of type: ${msg::class.java.simpleName}")
                            if (ctx!!.pipeline().any { (k, v) -> k == FILE_BODY_HANDLER_LITERAL }) {
                                logger.debug("Pipeline contains FILE BODY HANDLER, sending body to it.....")
                                ctx.fireChannelRead(msg)
                            } else {
                                logger.debug("There is no FILE BODY HANDLER, so msg is discarded")
                                if (msg is ReferenceCounted) {
                                    msg.release()
                                }
                            }
                        }.join()
                    }
                }

            }
        }


    }
}
//TODO ???? ?? ?????????? ?????? ???????????? ?????????????????? ????????....
//TODO ?????? ???? ?????????????????? ?????????????????? ???????????????? ?????????????? ???????????? ???????? ???????????????????? ???????? ??????????????????
//TODO ???????? ?????????????????? ?????????????????? ???????????????? - ?????????? ???????????? ?????????? ?????????????????? ?? ?????????????????????? ?????????????? ?????????????? (?? ?????????? ????????????)
