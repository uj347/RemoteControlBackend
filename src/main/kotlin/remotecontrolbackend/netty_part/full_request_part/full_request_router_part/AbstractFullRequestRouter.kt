package remotecontrolbackend.netty_part.full_request_part.full_request_router_part

import io.netty.channel.ChannelHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import remotecontrolbackend.dagger.NettyScope


@NettyScope
@ChannelHandler.Sharable
abstract class AbstractFullRequestRouter():SimpleChannelInboundHandler<FullHttpRequest>(){
companion object{

}
}