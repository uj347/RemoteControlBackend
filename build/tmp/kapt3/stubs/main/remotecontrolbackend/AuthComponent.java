package remotecontrolbackend;

import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001:\u0001\u0006J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0007"}, d2 = {"Lremotecontrolbackend/AuthComponent;", "", "inject", "", "authHandler", "Lremotecontrolbackend/netty_part/auth_part/ConcreteAuthHandler;", "AuthBuilder", "CoroutineTest"})
@dagger.Subcomponent(modules = {remotecontrolbackend.TestAuthModule.class})
@AuthScope
public abstract interface AuthComponent {
    
    public abstract void inject(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.netty_part.auth_part.ConcreteAuthHandler authHandler);
    
    @kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&\u00a8\u0006\u0004"}, d2 = {"Lremotecontrolbackend/AuthComponent$AuthBuilder;", "", "build", "Lremotecontrolbackend/AuthComponent;", "CoroutineTest"})
    @dagger.Subcomponent.Builder
    public static abstract interface AuthBuilder {
        
        @org.jetbrains.annotations.NotNull
        public abstract remotecontrolbackend.AuthComponent build();
    }
}