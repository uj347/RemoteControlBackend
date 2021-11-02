package remotecontrolbackend.dagger

import APP_COROUTINE_CONTEXT_LITERAL
import IS_TEST_LITERAL
import WORK_DIR_LITERAL
import dagger.*
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import kotlinx.coroutines.Dispatchers
import remotecontrolbackend.UserRepo
import remotecontrolbackend.dagger.NettyModule.Companion.NETTY_COROUTINE_CONTEXT_LITERAL
import remotecontrolbackend.netty_part.NettyConnectionManager
import remotecontrolbackend.netty_part.NettySslContextProvider
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.auth_part.MockAuthHandler
import remotecontrolbackend.netty_part.auth_part.MockUserRepo
import remotecontrolbackend.netty_part.command_handler_part.AbstractCommandHandler
import remotecontrolbackend.netty_part.command_handler_part.ConcreteCommandHandler
import remotecontrolbackend.netty_part.command_handler_part.MockCommandHandler
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler
import remotecontrolbackend.netty_part.request_handler_part.ConcreteRequestHandler
import remotecontrolbackend.netty_part.request_handler_part.MockRequestHandler
import java.nio.file.Path
import javax.inject.Named
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext

//TODO



@NettyScope
@Subcomponent(modules = [NettyModule::class, NettySSLModule::class])
interface NettySubComponent {
    fun getRequestHandler(): AbstractRequestHandler
    fun getAuthHandler(): AbstractAuthHandler
    fun getCommandHandler(): AbstractCommandHandler

    @Named(NETTY_COROUTINE_CONTEXT_LITERAL)
    fun getNettyCoroutineContext():CoroutineContext

    fun inject(nettyConnectionManager: NettyConnectionManager)
    fun getSSLContextProvider():NettySslContextProvider

    @Subcomponent.Builder
    interface NettySubComponentBuilder {
        fun buildNettySubcomponent(): NettySubComponent
    }
}
@Module
interface NettySSLModule{

    @NettyScope
    @Named(SSL_PATHS_MAP_LITERAL)
    @StringKey(SSL_DIRECTORY_LITERAL)
    @IntoMap
    @Binds
    abstract fun provideSslDirectoryPathIntoMap(@Named(SSL_DIRECTORY_LITERAL) sslDir: Path):Path

    companion object{
        const val SERVER_SSL_PATH_LITERAL="SERVER_SSL_PATH"
        const val CLIENT_SSL_PATH_LITERAL="CLIENT_SSL_PATH"
        const val SSL_DIRECTORY_LITERAL="SSL_DIRECTORY_PATH"
        const val SSL_CA_CERT_PATH_LITERAL="SSL_CA_CERT_PATH"
        const val SSL_PATHS_MAP_LITERAL="SSL_MAP"
        //TODO Прикруутить проброс пути из мейн компонента


        @NettyScope
        @Named(SSL_DIRECTORY_LITERAL)
        @Provides
        fun provideSslDirectoryPath(@Named(WORK_DIR_LITERAL) workDir: Path):Path{
            return workDir.resolve("SSL")
        }

        @NettyScope
        @StringKey(SSL_CA_CERT_PATH_LITERAL)
        @Named(SSL_PATHS_MAP_LITERAL)
        @Provides
        @IntoMap
        fun provideCaCertPath(
            @Named(SSL_DIRECTORY_LITERAL) sslDir: Path
        ):Path{
        return sslDir.resolve("CACert.pem")

        }

        @NettyScope
        @StringKey(SERVER_SSL_PATH_LITERAL)
        @Named(SSL_PATHS_MAP_LITERAL)
        @Provides
        @IntoMap
        fun provideServerSSLPath(
            @Named(SSL_DIRECTORY_LITERAL) sslDir: Path
        ):Path{
            return sslDir.resolve("SERVER")
        }

        @NettyScope
        @StringKey(CLIENT_SSL_PATH_LITERAL)
        @Named(SSL_PATHS_MAP_LITERAL)
        @IntoMap
        @Provides
        fun provideClientSSLPath(
            @Named(SSL_DIRECTORY_LITERAL) sslDir: Path
        ):Path{
            return sslDir.resolve("CLIENT")
        }
    }
}

@Module
interface NettyModule {
    companion object {
        const val TEST = "TEST"
        const val CONCRETE = "CONCRETE"
        const val NETTY_COROUTINE_CONTEXT_LITERAL="NettyCoroutineContext"


        @NettyScope
        @Named(NETTY_COROUTINE_CONTEXT_LITERAL)
        @Provides
        fun provideNettyCoroutineScope(
            @Named(APP_COROUTINE_CONTEXT_LITERAL)
            appCoroutineContext: CoroutineContext
        ): CoroutineContext {

            return appCoroutineContext+Dispatchers.IO
        }

        @NettyScope
        @Provides
        fun provideAuthH(
            map: Map<String, @JvmSuppressWildcards AbstractAuthHandler>,
            @Named(IS_TEST_LITERAL) isTest: Boolean
        ): AbstractAuthHandler {
            when (isTest) {
                true -> return map.get(TEST)!!
                false -> return map.get(CONCRETE)!!
            }
        }

        @NettyScope
        @Provides
        fun provideCommandH(
            map: Map<String,@JvmSuppressWildcards AbstractCommandHandler>,
            @Named(IS_TEST_LITERAL) isTest: Boolean
        ): AbstractCommandHandler {
            when (isTest) {
                true -> return map.get(TEST)!!
                false -> return map.get(CONCRETE)!!
            }
        }

        @Provides
        @NettyScope
        fun provideRequestH(
            map: Map<String, @JvmSuppressWildcards AbstractRequestHandler>,
            @Named(IS_TEST_LITERAL) isTest: Boolean
        ): AbstractRequestHandler {
            when (isTest) {
                true -> return map.get(TEST)!!
                false -> return map.get(CONCRETE)!!
            }
        }

        @Provides
        @NettyScope
        fun provideUserRepo(
            map: Map<String, @JvmSuppressWildcards UserRepo>,
            @Named(IS_TEST_LITERAL) isTest: Boolean
        ): UserRepo {
            when (isTest) {
                true -> return map.get(TEST)!!
                false -> return map.get(CONCRETE)!!
            }
        }
    }


    @NettyScope
    @Binds
    @IntoMap
    @StringKey(TEST)
     fun bindMockRequestH(mock: MockRequestHandler): AbstractRequestHandler

    @NettyScope
    @Binds
    @IntoMap
    @StringKey(CONCRETE)
    fun bindConcreteRequestH(concreteRequestHandler: ConcreteRequestHandler): AbstractRequestHandler


    @NettyScope
    @Binds
    @IntoMap
    @StringKey(TEST)
    fun bindMockAuthH(mockAuthHandler: MockAuthHandler): AbstractAuthHandler

    @NettyScope
    @Binds
    @IntoMap
    @StringKey(CONCRETE)
    //TODO Осторожно только моки реализованны на данный момент

    fun bindConcreteAuthH(concreteAuthHandler: MockAuthHandler): AbstractAuthHandler


    @NettyScope
    @Binds
    @IntoMap
    @StringKey(TEST)
    fun bindMockCommandH(mockCommandHandler: MockCommandHandler): AbstractCommandHandler


    @NettyScope
    @Binds
    @IntoMap
    @StringKey(CONCRETE)
    //TODO Осторожно только моки реализованны на данный момент

    fun bindConcreteCommandH(concreteCommandHandler: ConcreteCommandHandler): AbstractCommandHandler


    @NettyScope
    @Binds
    @IntoMap
    @StringKey(CONCRETE)
    //TODO Осторожно только моки реализованны на данный момент
    fun bindConcreteUserRepo(mockUserRepo: MockUserRepo): UserRepo

    @NettyScope
    @Binds
    @IntoMap
    @StringKey(TEST)
    //TODO Осторожно только моки реализованны на данный момент
    fun bindMockUserRepo(mockUserRepo: MockUserRepo): UserRepo
//companion object{
//        @NettyScope
//        @Provides
//        fun provideAuthHandler(
//            @Named(IS_TEST_LITERAL) isTest: Boolean
//        ): AbstractAuthHandler {
//            if (isTest) {
//                return MockAuthHandler()
//            } else {
//                return ConcreteAuthHandler()
//            }
//        }
//        @NettyScope
//        @Provides
//        fun provideRequestHandler(
//            @Named("isTest") isTest: Boolean
//        ): AbstractRequestHandler {
//            if (isTest) {
//                return MockRequestHandler()
//            } else {
//                //TODO За неимением настоящего Хэндлера пока здесь будет висеть это, нужно не забыть убрать
//                return MockRequestHandler()
//            }
//        }
//
//        @NettyScope
//        @Provides
//        fun provideCommandHandler(
//            @Named(IS_TEST_LITERAL) isTest: Boolean,
//
//        ): AbstractCommandHandler {
//            if (isTest) {
//                return MockCommandHandler()
//            } else {
//                //TODO Пока не слепил конкрит - будет мок
//                return MockCommandHandler()
//            }
//        }
}


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class NettyScope