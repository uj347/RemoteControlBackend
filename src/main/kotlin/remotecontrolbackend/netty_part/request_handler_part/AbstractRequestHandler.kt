package remotecontrolbackend.netty_part.request_handler_part

import io.netty.channel.ChannelHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import remotecontrolbackend.dagger.RequestHandlerSubComponent
import javax.inject.Inject


@ChannelHandler.Sharable
abstract class AbstractRequestHandler(rhBuilder: RequestHandlerSubComponent.RhBuilder):SimpleChannelInboundHandler<FullHttpRequest>(){

}