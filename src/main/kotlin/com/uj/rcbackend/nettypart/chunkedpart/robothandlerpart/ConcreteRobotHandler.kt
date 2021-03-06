package com.uj.rcbackend.nettypart.chunkedpart.robothandlerpart

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.LastHttpContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import com.uj.rcbackend.dagger.NettyMainModule.Companion.NETTY_COROUTINE_CONTEXT_LITERAL
import com.uj.rcbackend.dagger.NettyScope
import com.uj.rcbackend.nettypart.send200Response
import com.uj.rcbackend.nettypart.utils.SpecificChain
import com.uj.rcbackend.robot.RobotCommandPack
import com.uj.rcbackend.robot.RobotManager
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

//TODO Переделать его в ЧэнелАдаптер
@SpecificChain(SpecificChain.ChainType.CHUNKED)
@NettyScope
@Sharable
class ConcreteRobotHandler @Inject constructor() : AbstractRobotHandler() {

    companion object {
        val logger = LogManager.getLogger()
    }

    @Inject
    lateinit var robotManager: RobotManager

    @Named(NETTY_COROUTINE_CONTEXT_LITERAL)
    @Inject
    lateinit var nettyContext: CoroutineContext



    val moshi = Moshi.Builder().build()
    val arrayOfStringArraysAdapter = moshi.adapter<Array<Array<String>>>(
        Types.arrayOf(Types.arrayOf(String::class.java))
    )


    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        super.handlerAdded(ctx)
        robotManager.reinitializeRobotActor()
        logger.debug("In handler added, channel is active:${ctx?.channel()?.isOpen}")
    }


    //TODO Переделать
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        logger.debug("In channel read with")
        msg?.let {
            if (it is HttpContent) {
                logger.debug("Received message is HttpContent")
                kotlin.runCatching {

                    val recievedCommands =
                        arrayOfStringArraysAdapter.fromJson(it.content().toString(StandardCharsets.UTF_8))
                    if (recievedCommands != null) {
                        logger.debug("Received commands: $recievedCommands")
                        var promise = CompletableFuture<Unit>()
                        for (commandArr in recievedCommands) {
                            runBlocking {
                                logger.debug("performing processing of robot command: ${commandArr.get(0)}")
                                CoroutineScope(nettyContext).launch {
                                    robotManager.robotActor.send(RobotCommandPack(commandArr, promise))
                                }.join()
                                logger.debug("RoboCommand ${commandArr[0]} executed")
                                promise= CompletableFuture()
                            }
                        }

                    }

                }.onFailure { logger.error(it) }
                logger.debug("Releasing used content")
                it.release()
            }
            if (it is LastHttpContent) {
                it.release()
                ctx.send200Response().addListener {
                    ctx?.close()
                }
            }
        }
    }
}