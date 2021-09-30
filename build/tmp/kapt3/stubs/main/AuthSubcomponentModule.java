
import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\bg\u0018\u0000 \u00022\u00020\u0001:\u0001\u0002\u00a8\u0006\u0003"}, d2 = {"LAuthSubcomponentModule;", "", "Companion", "CoroutineTest"})
@dagger.Module(subcomponents = {remotecontrolbackend.AuthComponent.class})
public abstract interface AuthSubcomponentModule {
    @org.jetbrains.annotations.NotNull
    public static final AuthSubcomponentModule.Companion Companion = null;
    
    @kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\t\u001a\u00020\u0004H\u0007\u00a8\u0006\f"}, d2 = {"LAuthSubcomponentModule$Companion;", "", "()V", "provideAuthComponent", "Lremotecontrolbackend/AuthComponent;", "authComponentBuilder", "Lremotecontrolbackend/AuthComponent$AuthBuilder;", "provideConcreteAuthHandler", "Lremotecontrolbackend/auth_part/ConcreteAuthHandler;", "authComponent", "provideMockAuthHandler", "Lremotecontrolbackend/auth_part/MockAuthHandler;", "CoroutineTest"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        @dagger.Provides
        public final remotecontrolbackend.AuthComponent provideAuthComponent(@org.jetbrains.annotations.NotNull
        remotecontrolbackend.AuthComponent.AuthBuilder authComponentBuilder) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        @dagger.Provides
        public final remotecontrolbackend.auth_part.ConcreteAuthHandler provideConcreteAuthHandler(@org.jetbrains.annotations.NotNull
        remotecontrolbackend.AuthComponent authComponent) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        @dagger.Provides
        public final remotecontrolbackend.auth_part.MockAuthHandler provideMockAuthHandler(@org.jetbrains.annotations.NotNull
        remotecontrolbackend.AuthComponent authComponent) {
            return null;
        }
    }
}