package com.uj.rcbackend.nettypart.chunkedpart

import io.netty.channel.ChannelInboundHandlerAdapter
import com.uj.rcbackend.nettypart.utils.SpecificChain

@SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
abstract class ChunkWorkModeHandler:ChannelInboundHandlerAdapter(){
    abstract val handlerQuery:String
    abstract val handlerDescription:String
}