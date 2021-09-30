package remotecontrolbackend.request_handler_part

import io.netty.channel.ChannelHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest


@ChannelHandler.Sharable
abstract class AbstractRequestHandler():SimpleChannelInboundHandler<FullHttpRequest>(){

}