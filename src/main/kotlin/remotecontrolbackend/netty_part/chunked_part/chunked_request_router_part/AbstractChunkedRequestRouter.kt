package remotecontrolbackend.netty_part.chunked_part.chunked_request_router_part

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.HttpRequest
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.utils.SpecificChain

@SpecificChain(chainType = SpecificChain.ChainType.CHUNKED)
@NettyScope
@ChannelHandler.Sharable
abstract class AbstractChunkedRequestRouter:SimpleChannelInboundHandler<HttpRequest>(){

   companion object{
       val handlerDescription= NettySubComponent.CHUNKED_REQUEST_ROUTER_LITERAL
   }

}
