package remotecontrolbackend.netty_part.full_request_part

import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.netty_part.utils.SpecificChain
import remotecontrolbackend.netty_part.utils.SpecificChain.ChainType

@SpecificChain(chainType = ChainType.FULLREQUEST)
@NettyScope
abstract class FullRequestWorkModeHandler:SimpleChannelInboundHandler<FullHttpRequest> (){
    abstract val handlerQuery:String

}