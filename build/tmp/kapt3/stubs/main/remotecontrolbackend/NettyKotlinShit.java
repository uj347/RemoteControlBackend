package remotecontrolbackend;

import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0011\u0010\u0019\u001a\u00020\u001aH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001bR\u001e\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001e\u0010\u0011\u001a\u00020\u00128\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0017\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0010\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001c"}, d2 = {"Lremotecontrolbackend/NettyKotlinShit;", "", "()V", "authHandler", "Lremotecontrolbackend/auth_part/AbstractAuthHandler;", "getAuthHandler", "()Lremotecontrolbackend/auth_part/AbstractAuthHandler;", "setAuthHandler", "(Lremotecontrolbackend/auth_part/AbstractAuthHandler;)V", "bootStrap", "Lio/netty/bootstrap/ServerBootstrap;", "getBootStrap", "()Lio/netty/bootstrap/ServerBootstrap;", "bossGroup", "Lio/netty/channel/nio/NioEventLoopGroup;", "getBossGroup", "()Lio/netty/channel/nio/NioEventLoopGroup;", "requestHandler", "Lremotecontrolbackend/request_handler_part/AbstractRequestHandler;", "getRequestHandler", "()Lremotecontrolbackend/request_handler_part/AbstractRequestHandler;", "setRequestHandler", "(Lremotecontrolbackend/request_handler_part/AbstractRequestHandler;)V", "workerGroup", "getWorkerGroup", "launch", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "CoroutineTest"})
public final class NettyKotlinShit {
    @org.jetbrains.annotations.NotNull
    private final io.netty.channel.nio.NioEventLoopGroup bossGroup = null;
    @org.jetbrains.annotations.NotNull
    private final io.netty.channel.nio.NioEventLoopGroup workerGroup = null;
    @org.jetbrains.annotations.NotNull
    private final io.netty.bootstrap.ServerBootstrap bootStrap = null;
    @javax.inject.Inject
    public remotecontrolbackend.auth_part.AbstractAuthHandler authHandler;
    @javax.inject.Inject
    public remotecontrolbackend.request_handler_part.AbstractRequestHandler requestHandler;
    
    public NettyKotlinShit() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final io.netty.channel.nio.NioEventLoopGroup getBossGroup() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final io.netty.channel.nio.NioEventLoopGroup getWorkerGroup() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final io.netty.bootstrap.ServerBootstrap getBootStrap() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final remotecontrolbackend.auth_part.AbstractAuthHandler getAuthHandler() {
        return null;
    }
    
    public final void setAuthHandler(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.auth_part.AbstractAuthHandler p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final remotecontrolbackend.request_handler_part.AbstractRequestHandler getRequestHandler() {
        return null;
    }
    
    public final void setRequestHandler(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.request_handler_part.AbstractRequestHandler p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object launch(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation) {
        return null;
    }
}