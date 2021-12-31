package remotecontrolbackend.dagger

import APP_COROUTINE_CONTEXT_LITERAL
import ROOT_DIR_LITERAL
import dagger.*
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import remotecontrolbackend.UserRepo
import remotecontrolbackend.dagger.NettyMainModule.Companion.NETTY_COROUTINE_CONTEXT_LITERAL
import remotecontrolbackend.netty_part.NettyConnectionManager
import remotecontrolbackend.netty_part.NettySslContextProvider
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.auth_part.MockAuthHandler
import remotecontrolbackend.netty_part.auth_part.MockUserRepo
import remotecontrolbackend.netty_part.chunked_part.ChunkWorkModeHandler
import remotecontrolbackend.netty_part.TransferEncodingInterceptor
import remotecontrolbackend.netty_part.chunked_part.chunked_request_router_part.AbstractChunkedRequestRouter
import remotecontrolbackend.netty_part.chunked_part.chunked_request_router_part.ConcreteChunkedRequestRouter
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.AbstractFileHandler
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.ConcreteFileHandler
import remotecontrolbackend.netty_part.chunked_part.robot_handler_part.AbstractRobotHandler
import remotecontrolbackend.netty_part.chunked_part.robot_handler_part.ConcreteRobotHandler
import remotecontrolbackend.netty_part.full_request_part.FullRequestWorkModeHandler
import remotecontrolbackend.netty_part.full_request_part.command_handler_part.AbstractCommandHandler
import remotecontrolbackend.netty_part.full_request_part.command_handler_part.ConcreteCommandHandler
import remotecontrolbackend.netty_part.full_request_part.full_request_router_part.AbstractFullRequestRouter
import remotecontrolbackend.netty_part.full_request_part.full_request_router_part.ConcreteFullRequestRouter
import java.nio.file.Path
import javax.inject.Named
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext

//TODO


@NettyScope
@Subcomponent(modules = [NettyMainModule::class, NettySSLModule::class,
    NettyFullRequestModesModule::class, NettyAuthModule::class,
    NettyChunkedRequestModesModule::class])

interface NettySubComponent {
    companion object {
        const val COMMAND_HANDLER_LITERAL = "COMMAND_HANDLER"
        const val FULL_REQUEST_ROUTER_LITERAL = "FULL_REQUEST_ROUTER"
        const val CHUNKED_REQUEST_ROUTER_LITERAL = "CHUNKED_REQUEST_ROUTER"
        const val TRANSFER_ENCODING_INTERCEPTOR_LITERAL = "TRANSFER_ENCODING_INTERCEPTOR"
        const val ROBOT_HANDLER_LITERAL = "ROBOT_HANDLER"
        const val FILE_HANDLER_LITERAL="FILE_HANDLER"
        const val SSL_HANDLER_LITERAL = "SSL_HANDLER"
        const val AUTH_HANDLER_LITERAL = "AUTH_HANDLER"
        const val EXCEPTION_CATCHER_LITERAL = "EXCEPTION_CATCHER"
        const val HTTP_AGGREGATOR_LITERAL = "HTTP_AGGREGATOR"
        const val HTTP_CODEC_LITERAL = "HTTP_CODEC"

    }

    fun getRobotHandler(): AbstractRobotHandler
    fun getChunkedRequestRouter(): AbstractChunkedRequestRouter
    fun getFullRequestRouter(): AbstractFullRequestRouter
    fun getAuthHandler(): AbstractAuthHandler
    fun getCommandHandler(): AbstractCommandHandler
    fun getChunkedInterceptor(): TransferEncodingInterceptor


    @Named(NETTY_COROUTINE_CONTEXT_LITERAL)
    fun getNettyCoroutineContext(): CoroutineContext

    fun inject(nettyConnectionManager: NettyConnectionManager)
    fun getSSLContextProvider(): NettySslContextProvider

    @Subcomponent.Builder
    interface NettySubComponentBuilder {
        fun buildNettySubcomponent(): NettySubComponent
    }
}

@Module
interface NettySSLModule {

    @NettyScope
    @Named(SSL_PATHS_MAP_LITERAL)
    @StringKey(SSL_DIRECTORY_LITERAL)
    @IntoMap
    @Binds
    fun provideSslDirectoryPathIntoMap(@Named(SSL_DIRECTORY_LITERAL) sslDir: Path): Path

    companion object {
        const val SERVER_SSL_PATH_LITERAL = "SERVER_SSL_PATH"
        const val CLIENT_SSL_PATH_LITERAL = "CLIENT_SSL_PATH"
        const val SSL_DIRECTORY_LITERAL = "SSL_DIRECTORY_PATH"
        const val SSL_CA_CERT_PATH_LITERAL = "SSL_CA_CERT_PATH"
        const val SSL_PATHS_MAP_LITERAL = "SSL_MAP"
        //TODO Прикруутить проброс пути из мейн компонента


        @NettyScope
        @Named(SSL_DIRECTORY_LITERAL)
        @Provides
        fun provideSslDirectoryPath(@Named(ROOT_DIR_LITERAL) workDir: Path): Path {
            return workDir.resolve("SSL")
        }

        @NettyScope
        @StringKey(SSL_CA_CERT_PATH_LITERAL)
        @Named(SSL_PATHS_MAP_LITERAL)
        @Provides
        @IntoMap
        fun provideCaCertPath(
            @Named(SSL_DIRECTORY_LITERAL) sslDir: Path
        ): Path {
            return sslDir.resolve("CACert.pem")

        }

        @NettyScope
        @StringKey(SERVER_SSL_PATH_LITERAL)
        @Named(SSL_PATHS_MAP_LITERAL)
        @Provides
        @IntoMap
        fun provideServerSSLPath(
            @Named(SSL_DIRECTORY_LITERAL) sslDir: Path
        ): Path {
            return sslDir.resolve("SERVER")
        }

        @NettyScope
        @StringKey(CLIENT_SSL_PATH_LITERAL)
        @Named(SSL_PATHS_MAP_LITERAL)
        @IntoMap
        @Provides
        fun provideClientSSLPath(
            @Named(SSL_DIRECTORY_LITERAL) sslDir: Path
        ): Path {
            return sslDir.resolve("CLIENT")
        }
    }
}

@Module
interface NettyMainModule {
    companion object {
        const val NETTY_COROUTINE_CONTEXT_LITERAL = "NettyCoroutineContext"


        @NettyScope
        @Named(NETTY_COROUTINE_CONTEXT_LITERAL)
        @Provides
        fun provideNettyCoroutineScope(
            @Named(APP_COROUTINE_CONTEXT_LITERAL)
            appCoroutineContext: CoroutineContext
        ): CoroutineContext {

            return appCoroutineContext + Dispatchers.IO+ SupervisorJob(appCoroutineContext.job)
        }


    }
}

@Module
interface NettyAuthModule {
    companion object {}
    @NettyScope
    @Binds
    fun bindConcreteAuthH(concreteAuthHandler: MockAuthHandler): AbstractAuthHandler


    @NettyScope
    @Binds
    fun bindConcreteUserRepo(mockUserRepo: MockUserRepo): UserRepo


}


@Module
interface NettyFullRequestModesModule {

    companion object {}

    @NettyScope
    @Binds
    @IntoMap
    @StringKey(AbstractCommandHandler.COMMAND_QUERY)
    fun bindCommandHandlerToFullModeMap(commandHandler: AbstractCommandHandler): FullRequestWorkModeHandler


    @NettyScope
    @Binds
    fun bindConcreteCommandHandlerIntoMap(concreteCommandHandler: ConcreteCommandHandler): AbstractCommandHandler


    @NettyScope
    @Binds
    fun bindConcreteFullRequestRouterIntoMap(concreteRequestHandler: ConcreteFullRequestRouter): AbstractFullRequestRouter
}

@Module
interface NettyChunkedRequestModesModule {
    companion object { }
    @NettyScope
    @Binds
    fun bindConcreteChunkedRouter(concreteChunkedRouter: ConcreteChunkedRequestRouter): AbstractChunkedRequestRouter

    @NettyScope
    @Binds
    fun bindConcreteRobotHandler(concreteRobotHandler: ConcreteRobotHandler): AbstractRobotHandler

    @NettyScope
    @Binds
    @IntoMap
    @StringKey(AbstractRobotHandler.ROBOT_QUERY)
    fun bindRobotModeHandlerIntoMap(robotHandler: AbstractRobotHandler): ChunkWorkModeHandler


    @NettyScope
    @Binds
    fun bindConcreteFileHandlerIntoMap(concreteFileHandler: ConcreteFileHandler): AbstractFileHandler

    @NettyScope
    @Binds
    @IntoMap
    @StringKey(AbstractFileHandler.FILE_QUERY)
    fun bindFileModeHandlerIntoMap(fileHandler: AbstractFileHandler): ChunkWorkModeHandler



}


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class NettyScope