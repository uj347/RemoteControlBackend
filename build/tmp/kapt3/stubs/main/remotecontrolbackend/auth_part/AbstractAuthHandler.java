package remotecontrolbackend.auth_part;

import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\r\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lremotecontrolbackend/auth_part/AbstractAuthHandler;", "Lio/netty/channel/SimpleChannelInboundHandler;", "Lio/netty/handler/codec/http/FullHttpRequest;", "authComponent", "Lremotecontrolbackend/AuthComponent;", "(Lremotecontrolbackend/AuthComponent;)V", "CoroutineTest"})
@io.netty.channel.ChannelHandler.Sharable
public abstract class AbstractAuthHandler extends io.netty.channel.SimpleChannelInboundHandler<io.netty.handler.codec.http.FullHttpRequest> {
    
    public AbstractAuthHandler(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.AuthComponent authComponent) {
        super();
    }
}