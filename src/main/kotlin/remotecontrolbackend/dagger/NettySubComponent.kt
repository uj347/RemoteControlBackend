package remotecontrolbackend.dagger

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import remotecontrolbackend.AuthComponent
import remotecontrolbackend.netty_part.NettyConnectionManager
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.auth_part.ConcreteAuthHandler
import remotecontrolbackend.netty_part.auth_part.MockAuthHandler
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler
import remotecontrolbackend.netty_part.request_handler_part.MockRequestHandler
import javax.inject.Named
import javax.inject.Scope

//TODO
@NettyScope
@Subcomponent(modules = [NettyModule::class, RhSubcomponentModule::class, AuthSubcomponentModule::class])
interface NettySubComponent {
    fun inject(nettyConnectionManager: NettyConnectionManager)

    @Subcomponent.Builder
    interface NettySubComponentBuilder {
        fun buildNettySubcomponent(): NettySubComponent
    }
}

@Module
interface NettyModule {
    companion object {
        @Provides
        fun provideAuthHandler(
            @Named("isTest") isTest: Boolean,
            authBuilder: AuthComponent.AuthBuilder
        ): AbstractAuthHandler {
            if (isTest) {
                return MockAuthHandler(authBuilder)
            } else {
                return ConcreteAuthHandler(authBuilder)
            }
        }

        @Provides
        fun provideRequestHandler(
            @Named("isTest") isTest: Boolean,
            rhBuilder: RequestHandlerSubComponent.RhBuilder
        ): AbstractRequestHandler {
            if (isTest) {
                return MockRequestHandler(rhBuilder)
            } else {
                //TODO За неимением настоящего Хэндлера пока здесь будет висеть это, нужно не забыть убрать
                return MockRequestHandler(rhBuilder)
            }
        }
    }
}


@Module(subcomponents = arrayOf(AuthComponent::class))
interface AuthSubcomponentModule {

}

@Module(subcomponents = [RequestHandlerSubComponent::class])
interface RhSubcomponentModule {

}


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class NettyScope