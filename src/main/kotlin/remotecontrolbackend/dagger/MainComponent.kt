import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import remotecontrolbackend.Main

import remotecontrolbackend.dagger.DnsSdSubComponent
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.dns_sd_part.DnsSdManager
import remotecontrolbackend.netty_part.NettyConnectionManager
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(TestMainModule::class,NettySubcomponentModule::class, DnsSdSubcomponentModule::class))
interface MainComponent {
    fun inject(mainClass: Main)

    @Component.Builder
    interface MainBuilder{
        fun buildMainComponent():MainComponent
        @BindsInstance
        fun setPort(@Named("port") portN:Int):MainBuilder

        @BindsInstance
        fun isTestRun(@Named("isTest") isTest:Boolean):MainBuilder

    }

}


@Module
interface ConcreteMainModule {

}

@Module
interface TestMainModule {

}





@Module(subcomponents = [DnsSdSubComponent::class])
interface DnsSdSubcomponentModule{
companion object{
    @Provides
    fun provideDnsSdManager(dnsSdSubComponentBuilder: DnsSdSubComponent.DnsSdSubComponentBuilder,
                             @Named("port")portN: Int):DnsSdManager{
        return DnsSdManager(dnsSdSubComponentBuilder,portN)
    }
}
        }


    @Module(subcomponents = [NettySubComponent::class])
    interface NettySubcomponentModule{
companion object {
    @Provides
    fun provideNettyManager(nettySubComponentBuilder: NettySubComponent.NettySubComponentBuilder,
    @Named("port")portN: Int):NettyConnectionManager{
        return NettyConnectionManager(nettySubComponentBuilder,portN)
    }
//    @Provides
//    fun provideAuthHandler(
//        nettySubComponentBuilder: NettySubComponent.NettySubComponentBuilder
//                          ):AbstractAuthHandler{
//    return nettySubComponentBuilder.buildNettySubcomponent().getAuthHandler()
//    }
//
//    @Provides
//    fun provideRhHandler(
//        nettySubComponentBuilder: NettySubComponent.NettySubComponentBuilder
//    ):AbstractRequestHandler{
//        return nettySubComponentBuilder.buildNettySubcomponent().getRequestHandler()
//    }


    }
    }


