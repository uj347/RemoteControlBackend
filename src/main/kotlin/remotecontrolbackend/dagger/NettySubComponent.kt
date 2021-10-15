package remotecontrolbackend.dagger

import IS_TEST_LITERAL
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import remotecontrolbackend.AuthComponent
import remotecontrolbackend.netty_part.NettyConnectionManager
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.auth_part.ConcreteAuthHandler
import remotecontrolbackend.netty_part.auth_part.MockAuthHandler
import remotecontrolbackend.netty_part.command_handler_part.handler.AbstractCommandHandler
import remotecontrolbackend.netty_part.command_handler_part.handler.MockCommandHandler
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler
import remotecontrolbackend.netty_part.request_handler_part.MockRequestHandler
import javax.inject.Named
import javax.inject.Scope

//TODO
@NettyScope
@Subcomponent(modules = [NettyModule::class, RhSubcomponentModule::class, AuthSubcomponentModule::class,CommandHandlerSubcomponentModule::class])
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
            @Named(IS_TEST_LITERAL) isTest: Boolean,
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

        @Provides
        fun provideCommandHandler(
            @Named(IS_TEST_LITERAL) isTest: Boolean,
            commHandlerSCBuilder: CommandHandlerSubcomponent.CommHandlerSCBuilder
        ): AbstractCommandHandler {
            if (isTest) {
                return MockCommandHandler(commHandlerSCBuilder)
            } else {
                //TODO Пока не слепил конкрит будет мок
                return MockCommandHandler(commHandlerSCBuilder)
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

@Module(subcomponents = [CommandHandlerSubcomponent::class])
interface CommandHandlerSubcomponentModule {

}


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class NettyScope