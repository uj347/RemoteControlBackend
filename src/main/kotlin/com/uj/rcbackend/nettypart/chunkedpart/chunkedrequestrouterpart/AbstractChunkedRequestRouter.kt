package com.uj.rcbackend.nettypart.chunkedpart.chunkedrequestrouterpart

import io.netty.channel.ChannelHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.HttpRequest
import com.uj.rcbackend.dagger.NettyScope
import com.uj.rcbackend.dagger.NettySubComponent
import com.uj.rcbackend.nettypart.utils.SpecificChain

@SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
@NettyScope
@ChannelHandler.Sharable
abstract class AbstractChunkedRequestRouter:SimpleChannelInboundHandler<HttpRequest>(){

   companion object{
       val handlerDescription= NettySubComponent.CHUNKED_REQUEST_ROUTER_LITERAL
   }

}
