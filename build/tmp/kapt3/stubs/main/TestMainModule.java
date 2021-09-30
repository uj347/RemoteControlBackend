
import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0004\u001a\u00020\bH\'\u00a8\u0006\t"}, d2 = {"LTestMainModule;", "", "bindsMockAuthHandler", "Lremotecontrolbackend/auth_part/AbstractAuthHandler;", "handler", "Lremotecontrolbackend/auth_part/MockAuthHandler;", "bindsMockRequestHandler", "Lremotecontrolbackend/request_handler_part/AbstractRequestHandler;", "Lremotecontrolbackend/request_handler_part/MockRequestHandler;", "CoroutineTest"})
@dagger.Module
public abstract interface TestMainModule {
    
    @org.jetbrains.annotations.NotNull
    @dagger.Binds
    public abstract remotecontrolbackend.request_handler_part.AbstractRequestHandler bindsMockRequestHandler(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.request_handler_part.MockRequestHandler handler);
    
    @org.jetbrains.annotations.NotNull
    @dagger.Binds
    public abstract remotecontrolbackend.auth_part.AbstractAuthHandler bindsMockAuthHandler(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.auth_part.MockAuthHandler handler);
}