package com.uj.rcbackend.nettypart.fullrequestpart

import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import com.uj.rcbackend.dagger.NettyScope
import com.uj.rcbackend.nettypart.utils.SpecificChain
import com.uj.rcbackend.nettypart.utils.SpecificChain.ChainType

@SpecificChain(chainType = ChainType.FULLREQUEST)
@NettyScope
abstract class FullRequestWorkModeHandler:SimpleChannelInboundHandler<FullHttpRequest> (){
    abstract val handlerQuery:String

}