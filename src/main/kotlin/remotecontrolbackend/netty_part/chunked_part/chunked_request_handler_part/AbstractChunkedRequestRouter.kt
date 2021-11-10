package remotecontrolbackend.netty_part.chunked_part.chunked_request_handler_part

import io.netty.channel.ChannelHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.DefaultHttpRequest
import io.netty.handler.codec.http.HttpRequest
import remotecontrolbackend.dagger.NettyScope
import javax.inject.Inject

@NettyScope
@ChannelHandler.Sharable
abstract class AbstractChunkedRequestRouter:SimpleChannelInboundHandler<HttpRequest>(){

}
