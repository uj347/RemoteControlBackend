package com.uj.rcbackend.nettypart.fullrequestpart.fullrequestrouterpart

import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*

import com.uj.rcbackend.dagger.NettyScope
import com.uj.rcbackend.nettypart.fullrequestpart.commandhandlerpart.AbstractCommandHandler
import com.uj.rcbackend.nettypart.utils.SpecificChain
import javax.inject.Inject
@SpecificChain(chainType = SpecificChain.ChainType.FULLREQUEST)
@Sharable
@NettyScope

class MockFullRequestRouter @Inject constructor():AbstractFullRequestRouter() {
    init{
        println("Mock Request handler instantiated")
    }

    @Inject
    lateinit var commandHandler: AbstractCommandHandler


    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
if(HttpUtil.is100ContinueExpected(msg)){
    ctx?.write(DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.CONTINUE))
}

        val uri=msg?.uri()
        val queryStringDecoder=QueryStringDecoder(uri)
        if(queryStringDecoder.path().equals("/command",true)) {
            if (msg!=null){
                println("Recieved command request in mock request handler")
                ctx?.pipeline()?.addLast(commandHandler)
                msg.retain()
                ctx?.fireChannelRead(msg)
            }
        }else{
            println("received nocommand request in mock request handler")
            ctx?.writeAndFlush(
                DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK)
            )
        }
        }


    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        println("Invoked channel read complete")

    }
}