import dagger.Binds
import remotecontrolbackend.AuthComponent
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.internal.SetFactory
import remotecontrolbackend.AuthScope
import remotecontrolbackend.NettyKotlinShit
import remotecontrolbackend.auth_part.AbstractAuthHandler
import remotecontrolbackend.auth_part.ConcreteAuthHandler
import remotecontrolbackend.auth_part.MockAuthHandler
import remotecontrolbackend.request_handler_part.AbstractRequestHandler
import remotecontrolbackend.request_handler_part.MockRequestHandler
import javax.inject.Singleton

@Component(modules = arrayOf(TestMainModule::class, AuthSubcomponentModule::class))
abstract class MainComponent {
    abstract fun inject(mainClass: NettyKotlinShit)

}


@Module
interface RealMainModule {
    @Binds
    fun bindsRealAuthHandler(handler: ConcreteAuthHandler): AbstractAuthHandler

}

@Module
interface TestMainModule {
    @Binds
    abstract fun bindsMockRequestHandler(handler: MockRequestHandler): AbstractRequestHandler

    //ToDo Will be implemented later for now it's real
    @Binds
    abstract fun bindsMockAuthHandler(handler: MockAuthHandler): AbstractAuthHandler
}

@Module(subcomponents = arrayOf(AuthComponent::class))
interface AuthSubcomponentModule {
    companion object {
        @Provides
        fun provideAuthComponent(authComponentBuilder:AuthComponent.AuthBuilder):AuthComponent{
            return authComponentBuilder.build()
        }
        @Provides
        fun provideConcreteAuthHandler(authComponent: AuthComponent): ConcreteAuthHandler {
            return ConcreteAuthHandler(authComponent)
        }
        @Provides
        fun provideMockAuthHandler(authComponent: AuthComponent): MockAuthHandler {
            return MockAuthHandler(authComponent)
        }
    }
}

