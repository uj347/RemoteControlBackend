package remotecontrolbackend.netty_part.chunked_part

import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.SimpleChannelInboundHandler
import remotecontrolbackend.dagger.NettyScope


abstract class ChunkWorkModeHandler:ChannelInboundHandlerAdapter(){
    abstract val handlerDescription:String
}