import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

import remotecontrolbackend.MainLauncher
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.dagger.CommandInvokerSubcomponent

import remotecontrolbackend.dagger.DnsSdSubComponent
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.dns_sd_part.DnsSdManager

import remotecontrolbackend.netty_part.NettyConnectionManager

import java.nio.file.Path
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

const val PORT_LITERAL = "port"
const val INVOKER_DIR_LITERAL = "invokerDir"
const val IS_TEST_LITERAL = "isTest"
const val APP_COROUTINE_CONTEXT_LITERAL="AppCoroutineContext"

@Singleton
@Component(modules = [MainModule::class, NettySubcomponentModule::class, DnsSdSubcomponentModule::class, CommandInvokerSubcomponentModule::class])
interface MainComponent {
    fun inject(mainClass: MainLauncher)
   fun getCommandInvoker():CommandInvoker
    fun getComandInvokerSubcompBuilder(): CommandInvokerSubcomponent.CommandInvokerBuilder
    fun getNettySubcomponentBuilder(): NettySubComponent.NettySubComponentBuilder
    fun getLauncher(): MainLauncher

    @Named(APP_COROUTINE_CONTEXT_LITERAL)
    fun getAppCoroutineContext(): CoroutineContext

    @Component.Builder
    interface MainBuilder {
        fun buildMainComponent(): MainComponent

        @BindsInstance
        fun setPort(@Named(PORT_LITERAL) portN: Int): MainBuilder

        @BindsInstance
        fun setWorkDirectory(@Named(INVOKER_DIR_LITERAL) invokerDirectory: Path): MainBuilder

        @BindsInstance
        fun isTestRun(@Named(IS_TEST_LITERAL) isTest: Boolean): MainBuilder




    }

}


@Module
interface MainModule {
    companion object {
        @Singleton
        @Named(APP_COROUTINE_CONTEXT_LITERAL)
        @Provides
        fun provideAppCoroutineScope(): CoroutineContext {
            return Dispatchers.Default + SupervisorJob()
        }
    }
}


@Module(subcomponents = [DnsSdSubComponent::class])
interface DnsSdSubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideDnsSdManager(
            dnsSdSubComponentBuilder: DnsSdSubComponent.DnsSdSubComponentBuilder,
            @Named(PORT_LITERAL) portN: Int
        ): DnsSdManager {
            return DnsSdManager(dnsSdSubComponentBuilder, portN)
        }
    }
}


@Module(subcomponents = [NettySubComponent::class])
interface NettySubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideNettyManager(
            nettySubComponentBuilder: NettySubComponent.NettySubComponentBuilder,
            @Named(PORT_LITERAL) portN: Int
        ): NettyConnectionManager {
            return NettyConnectionManager(nettySubComponentBuilder, portN)
        }
    }


}


@Module(subcomponents = [CommandInvokerSubcomponent::class])
interface CommandInvokerSubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideCommandInvoker(
            @Named(INVOKER_DIR_LITERAL)
            invokerDirectory: Path,
            commandInvokerSubcomponentBuilder: CommandInvokerSubcomponent.CommandInvokerBuilder
        ): CommandInvoker {
           return CommandInvoker(invokerDirectory, commandInvokerSubcomponentBuilder)
        }
    }
}