package com.uj.rcbackend.nettypart.fullrequestpart.fullrequestrouterpart

import io.netty.channel.ChannelHandler.*
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import com.uj.rcbackend.dagger.NettyScope
import com.uj.rcbackend.dagger.NettySubComponent.Companion.FULL_REQUEST_ROUTER_LITERAL
import com.uj.rcbackend.nettypart.utils.SpecificChain

@SpecificChain(chainType = SpecificChain.ChainType.FULLREQUEST)
@NettyScope
@Sharable
abstract class AbstractFullRequestRouter :SimpleChannelInboundHandler<FullHttpRequest>(){
companion object{
val handlerDescription:String=FULL_REQUEST_ROUTER_LITERAL
}


    open val handlerDescription
        get()=Companion.handlerDescription
}