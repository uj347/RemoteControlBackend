package remotecontrolbackend.netty_part.full_request_part.full_request_router_part

import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.dagger.NettySubComponent.Companion.FULL_REQUEST_ROUTER_LITERAL
import remotecontrolbackend.netty_part.chunked_part.ChunkWorkModeHandler
import remotecontrolbackend.netty_part.full_request_part.FullRequestWorkModeHandler
import remotecontrolbackend.netty_part.utils.FullRequestChain

@FullRequestChain
@NettyScope
@Sharable
abstract class AbstractFullRequestRouter():SimpleChannelInboundHandler<FullHttpRequest>(){
companion object{
val handlerDescription:String=FULL_REQUEST_ROUTER_LITERAL
}

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
       //При добавлении роутера, на всякий случай подчистить пайплайн от потенциально уже имеющихся ВоркМодХэндлеров
        ctx?.let{
            ctx.pipeline().removeAll {it.value is ChunkWorkModeHandler||it.value is FullRequestWorkModeHandler }
        }
    }

    open val handlerDescription
        get()=Companion.handlerDescription
}