package remotecontrolbackend.auth_part;

import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\u0010\t\u001a\u0004\u0018\u00010\nH\u0014J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e\u00a8\u0006\u000f"}, d2 = {"Lremotecontrolbackend/auth_part/MockAuthHandler;", "Lremotecontrolbackend/auth_part/AbstractAuthHandler;", "authComponent", "Lremotecontrolbackend/AuthComponent;", "(Lremotecontrolbackend/AuthComponent;)V", "channelRead0", "", "ctx", "Lio/netty/channel/ChannelHandlerContext;", "msg", "Lio/netty/handler/codec/http/FullHttpRequest;", "checkAuth", "", "string", "", "CoroutineTest"})
@remotecontrolbackend.AuthScope
public final class MockAuthHandler extends remotecontrolbackend.auth_part.AbstractAuthHandler {
    
    public MockAuthHandler(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.AuthComponent authComponent) {
        super(null);
    }
    
    public final boolean checkAuth(@org.jetbrains.annotations.NotNull
    java.lang.String string) {
        return false;
    }
    
    @java.lang.Override
    protected void channelRead0(@org.jetbrains.annotations.Nullable
    io.netty.channel.ChannelHandlerContext ctx, @org.jetbrains.annotations.Nullable
    io.netty.handler.codec.http.FullHttpRequest msg) {
    }
}