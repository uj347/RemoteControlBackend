package remotecontrolbackend.auth_part;

import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0014J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\n\u00a8\u0006\u0015"}, d2 = {"Lremotecontrolbackend/auth_part/ConcreteAuthHandler;", "Lremotecontrolbackend/auth_part/AbstractAuthHandler;", "authComponent", "Lremotecontrolbackend/AuthComponent;", "(Lremotecontrolbackend/AuthComponent;)V", "userRepo", "Lremotecontrolbackend/UserRepo;", "getUserRepo", "()Lremotecontrolbackend/UserRepo;", "setUserRepo", "(Lremotecontrolbackend/UserRepo;)V", "channelRead0", "", "ctx", "Lio/netty/channel/ChannelHandlerContext;", "msg", "Lio/netty/handler/codec/http/FullHttpRequest;", "checkAuth", "", "authHeaderContent", "", "CoroutineTest"})
@remotecontrolbackend.AuthScope
public final class ConcreteAuthHandler extends remotecontrolbackend.auth_part.AbstractAuthHandler {
    @javax.inject.Inject
    public remotecontrolbackend.UserRepo userRepo;
    
    public ConcreteAuthHandler(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.AuthComponent authComponent) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull
    public final remotecontrolbackend.UserRepo getUserRepo() {
        return null;
    }
    
    public final void setUserRepo(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.UserRepo p0) {
    }
    
    @java.lang.Override
    protected void channelRead0(@org.jetbrains.annotations.Nullable
    io.netty.channel.ChannelHandlerContext ctx, @org.jetbrains.annotations.Nullable
    io.netty.handler.codec.http.FullHttpRequest msg) {
    }
    
    public final boolean checkAuth(@org.jetbrains.annotations.NotNull
    java.lang.String authHeaderContent) {
        return false;
    }
}