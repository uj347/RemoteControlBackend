
import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\'\u00a8\u0006\u0006"}, d2 = {"LRealMainModule;", "", "bindsRealAuthHandler", "Lremotecontrolbackend/auth_part/AbstractAuthHandler;", "handler", "Lremotecontrolbackend/auth_part/ConcreteAuthHandler;", "CoroutineTest"})
@dagger.Module
public abstract interface RealMainModule {
    
    @org.jetbrains.annotations.NotNull
    @dagger.Binds
    public abstract remotecontrolbackend.auth_part.AbstractAuthHandler bindsRealAuthHandler(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.auth_part.ConcreteAuthHandler handler);
}