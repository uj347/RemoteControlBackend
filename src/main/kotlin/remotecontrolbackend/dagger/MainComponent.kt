import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

import remotecontrolbackend.MainLauncher
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.dagger.*

import remotecontrolbackend.dns_sd_part.DnsSdManager
import remotecontrolbackend.file_service_part.FileService
import remotecontrolbackend.file_service_part.path_list_provider_part.IPathListProvider

import remotecontrolbackend.netty_part.NettyConnectionManager
import remotecontrolbackend.robot.RobotManager

import java.nio.file.Path
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

const val PORT_LITERAL = "port"
const val WORK_DIR_LITERAL = "workDir"
const val IS_TEST_LITERAL = "isTest"
const val APP_COROUTINE_CONTEXT_LITERAL = "AppCoroutineContext"
const val SSL_ENABLED_LITERAL = "SSLEnabled"
const val AUTH_ENABLED_LITERAL = "AuthEnabled"


@Singleton
@Component(
    modules = [MainModule::class, NettySubcomponentModule::class, DnsSdSubcomponentModule::class,
        CommandInvokerSubcomponentModule::class, CommandInvokerSubcomponentModule::class,
        RobotManagerSubcomponentModule::class, FileServiceSubcomponentModule::class]
)
interface MainComponent {
    fun inject(mainClass: MainLauncher)
    fun getCommandInvoker(): CommandInvoker
    fun getComandInvokerSubcompBuilder(): CommandInvokerSubcomponent.CommandInvokerBuilder
    fun getNettySubcomponentBuilder(): NettySubComponent.NettySubComponentBuilder
    fun getLauncher(): MainLauncher
    fun getRobotManager(): RobotManager
    fun getFileServiceSubcomponentBuilder():FileServiceSubcomponent.Builder
    fun getFileService():FileService


    @Named(APP_COROUTINE_CONTEXT_LITERAL)
    fun getAppCoroutineContext(): CoroutineContext

    @Component.Builder
    interface MainBuilder {
        fun buildMainComponent(): MainComponent

        @BindsInstance()
        fun setPathProvider(filePathsProvider: IPathListProvider?): MainBuilder

        @BindsInstance
        fun setPort(@Named(PORT_LITERAL) portN: Int): MainBuilder

        @BindsInstance
        fun setWorkDirectory(@Named(WORK_DIR_LITERAL) invokerDirectory: Path): MainBuilder

        @BindsInstance
        fun isTestRun(@Named(IS_TEST_LITERAL) isTest: Boolean): MainBuilder

        @BindsInstance
        fun isSSLEnabled(@Named(SSL_ENABLED_LITERAL) isSSLEnabled: Boolean): MainBuilder

        @BindsInstance
        fun isAuthEnabled(@Named(AUTH_ENABLED_LITERAL) isAuthEnabled: Boolean): MainBuilder


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
            @Named(PORT_LITERAL) portN: Int,
            @Named(SSL_ENABLED_LITERAL) isSSLEnabled: Boolean,
            @Named(AUTH_ENABLED_LITERAL) isAuthEnabled: Boolean
        ): NettyConnectionManager {
            return NettyConnectionManager(nettySubComponentBuilder, portN, isSSLEnabled, isAuthEnabled)
        }
    }


}


@Module(subcomponents = [CommandInvokerSubcomponent::class])
interface CommandInvokerSubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideCommandInvoker(
            @Named(WORK_DIR_LITERAL)
            invokerDirectory: Path,
            commandInvokerSubcomponentBuilder: CommandInvokerSubcomponent.CommandInvokerBuilder
        ): CommandInvoker {
            return CommandInvoker(invokerDirectory, commandInvokerSubcomponentBuilder)
        }
    }


}

@Module(subcomponents = [RobotManagerSubcomponent::class])
interface RobotManagerSubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideRobotManager(
            subcomponentBuilder: RobotManagerSubcomponent.RobotManagerBuilder
        ): RobotManager {
            return RobotManager(subcomponentBuilder)
        }
    }
}


@Module(subcomponents = [FileServiceSubcomponent::class])
interface FileServiceSubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideFileService(
            fileServiceBuilder: FileServiceSubcomponent.Builder,
            pathProvider:IPathListProvider?
        ): FileService {
            return FileService(fileServiceBuilder,pathProvider)
        }
    }
}