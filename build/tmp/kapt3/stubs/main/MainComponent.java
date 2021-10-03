
import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001:\u0001\u0006J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0007"}, d2 = {"LMainComponent;", "", "inject", "", "mainClass", "Lremotecontrolbackend/Main;", "MainBuilder", "CoroutineTest"})
@dagger.Component(modules = {TestMainModule.class, NettySubcomponentModule.class, DnsSdSubcomponentModule.class})
@javax.inject.Singleton
public abstract interface MainComponent {
    
    public abstract void inject(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.Main mainClass);
    
    @kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\u0012\u0010\u0004\u001a\u00020\u00002\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\'J\u0012\u0010\u0007\u001a\u00020\u00002\b\b\u0001\u0010\b\u001a\u00020\tH\'\u00a8\u0006\n"}, d2 = {"LMainComponent$MainBuilder;", "", "buildMainComponent", "LMainComponent;", "isTestRun", "isTest", "", "setPort", "portN", "", "CoroutineTest"})
    @dagger.Component.Builder
    public static abstract interface MainBuilder {
        
        @org.jetbrains.annotations.NotNull
        public abstract MainComponent buildMainComponent();
        
        @org.jetbrains.annotations.NotNull
        @dagger.BindsInstance
        public abstract MainComponent.MainBuilder setPort(@javax.inject.Named(value = "port")
        int portN);
        
        @org.jetbrains.annotations.NotNull
        @dagger.BindsInstance
        public abstract MainComponent.MainBuilder isTestRun(@javax.inject.Named(value = "isTest")
        boolean isTest);
    }
}