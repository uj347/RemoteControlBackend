
import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001:\u0001\u000fJ\b\u0010\u0002\u001a\u00020\u0003H\'J\b\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0007H&J\b\u0010\b\u001a\u00020\tH&J\b\u0010\n\u001a\u00020\u000bH&J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\tH&\u00a8\u0006\u0010"}, d2 = {"LMainComponent;", "", "getAppCoroutineContext", "Lkotlin/coroutines/CoroutineContext;", "getComandInvokerSubcompBuilder", "Lremotecontrolbackend/dagger/CommandInvokerSubcomponent$CommandInvokerBuilder;", "getCommandInvoker", "Lremotecontrolbackend/command_invoker_part/command_invoker/CommandInvoker;", "getLauncher", "Lremotecontrolbackend/MainLauncher;", "getNettySubcomponentBuilder", "Lremotecontrolbackend/dagger/NettySubComponent$NettySubComponentBuilder;", "inject", "", "mainClass", "MainBuilder", "CoroutineTest"})
@dagger.Component(modules = {MainModule.class, NettySubcomponentModule.class, DnsSdSubcomponentModule.class, CommandInvokerSubcomponentModule.class})
@javax.inject.Singleton
public abstract interface MainComponent {
    
    public abstract void inject(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.MainLauncher mainClass);
    
    @org.jetbrains.annotations.NotNull
    public abstract remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker getCommandInvoker();
    
    @org.jetbrains.annotations.NotNull
    public abstract remotecontrolbackend.dagger.CommandInvokerSubcomponent.CommandInvokerBuilder getComandInvokerSubcompBuilder();
    
    @org.jetbrains.annotations.NotNull
    public abstract remotecontrolbackend.dagger.NettySubComponent.NettySubComponentBuilder getNettySubcomponentBuilder();
    
    @org.jetbrains.annotations.NotNull
    public abstract remotecontrolbackend.MainLauncher getLauncher();
    
    @org.jetbrains.annotations.NotNull
    @javax.inject.Named(value = "AppCoroutineContext")
    public abstract kotlin.coroutines.CoroutineContext getAppCoroutineContext();
    
    @kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\u0012\u0010\u0004\u001a\u00020\u00002\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\'J\u0012\u0010\u0007\u001a\u00020\u00002\b\b\u0001\u0010\b\u001a\u00020\tH\'J\u0012\u0010\n\u001a\u00020\u00002\b\b\u0001\u0010\u000b\u001a\u00020\fH\'\u00a8\u0006\r"}, d2 = {"LMainComponent$MainBuilder;", "", "buildMainComponent", "LMainComponent;", "isTestRun", "isTest", "", "setPort", "portN", "", "setWorkDirectory", "invokerDirectory", "Ljava/nio/file/Path;", "CoroutineTest"})
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
        public abstract MainComponent.MainBuilder setWorkDirectory(@org.jetbrains.annotations.NotNull
        @javax.inject.Named(value = "invokerDir")
        java.nio.file.Path invokerDirectory);
        
        @org.jetbrains.annotations.NotNull
        @dagger.BindsInstance
        public abstract MainComponent.MainBuilder isTestRun(@javax.inject.Named(value = "isTest")
        boolean isTest);
    }
}