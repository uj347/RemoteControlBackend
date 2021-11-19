package remotecontrolbackend.netty_part.full_request_part.full_request_router_part

import io.netty.channel.ChannelHandler.*
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent.Companion.FULL_REQUEST_ROUTER_LITERAL
import remotecontrolbackend.netty_part.utils.FullRequestChain

@FullRequestChain
@NettyScope
@Sharable
abstract class AbstractFullRequestRouter():SimpleChannelInboundHandler<FullHttpRequest>(){
companion object{
val handlerDescription:String=FULL_REQUEST_ROUTER_LITERAL
}
    open val handlerDescription
        get()=Companion.handlerDescription
}