package remotecontrolbackend.netty_part.chunked_part

import io.netty.channel.ChannelInboundHandlerAdapter
import remotecontrolbackend.netty_part.utils.SpecificChain

@SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
abstract class ChunkWorkModeHandler:ChannelInboundHandlerAdapter(){
    abstract val handlerQuery:String
    abstract val handlerDescription:String
}