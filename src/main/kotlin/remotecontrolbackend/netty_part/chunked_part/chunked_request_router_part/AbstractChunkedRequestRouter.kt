package remotecontrolbackend.netty_part.chunked_part.chunked_request_router_part

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.HttpRequest
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.netty_part.chunked_part.ChunkWorkModeHandler
import remotecontrolbackend.netty_part.full_request_part.FullRequestWorkModeHandler
import remotecontrolbackend.netty_part.utils.ChunkedChain

@ChunkedChain
@NettyScope
@ChannelHandler.Sharable
abstract class AbstractChunkedRequestRouter:SimpleChannelInboundHandler<HttpRequest>(){

   companion object{
       val handlerDescription= NettySubComponent.CHUNKED_REQUEST_ROUTER_LITERAL
   }

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        //При добавлении роутера, на всякий случай подчистить пайплайн от потенциально уже имеющихся ВоркМодХэндлеров
        ctx?.let{
            ctx.pipeline().removeAll {it.value is ChunkWorkModeHandler ||it.value is FullRequestWorkModeHandler }
        }
    }
}
