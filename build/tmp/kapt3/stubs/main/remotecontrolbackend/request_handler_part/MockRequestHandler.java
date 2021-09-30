package remotecontrolbackend.request_handler_part;

import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u0016J\u001c\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\n2\b\u0010\u0005\u001a\u0004\u0018\u00010\u000bH\u0014J\u0012\u0010\f\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\nH\u0016\u00a8\u0006\r"}, d2 = {"Lremotecontrolbackend/request_handler_part/MockRequestHandler;", "Lremotecontrolbackend/request_handler_part/AbstractRequestHandler;", "()V", "acceptInboundMessage", "", "msg", "", "channelRead0", "", "ctx", "Lio/netty/channel/ChannelHandlerContext;", "Lio/netty/handler/codec/http/FullHttpRequest;", "channelReadComplete", "CoroutineTest"})
public final class MockRequestHandler extends remotecontrolbackend.request_handler_part.AbstractRequestHandler {
    
    @javax.inject.Inject
    public MockRequestHandler() {
        super();
    }
    
    @java.lang.Override
    public boolean acceptInboundMessage(@org.jetbrains.annotations.Nullable
    java.lang.Object msg) {
        return false;
    }
    
    @java.lang.Override
    protected void channelRead0(@org.jetbrains.annotations.Nullable
    io.netty.channel.ChannelHandlerContext ctx, @org.jetbrains.annotations.Nullable
    io.netty.handler.codec.http.FullHttpRequest msg) {
    }
    
    @java.lang.Override
    public void channelReadComplete(@org.jetbrains.annotations.Nullable
    io.netty.channel.ChannelHandlerContext ctx) {
    }
}