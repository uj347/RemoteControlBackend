package remotecontrolbackend.netty_part.request_handler_part

import io.netty.channel.ChannelHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderValues.*
import io.netty.handler.codec.http.multipart.InterfaceHttpData
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.dagger.NettyScope
import java.lang.RuntimeException


@NettyScope
@ChannelHandler.Sharable
abstract class AbstractRequestHandler():SimpleChannelInboundHandler<FullHttpRequest>(){
companion object{

}
}