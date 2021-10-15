package remotecontrolbackend.dagger

import dagger.Subcomponent
import remotecontrolbackend.netty_part.command_handler_part.handler.AbstractCommandHandler
import remotecontrolbackend.netty_part.command_handler_part.handler.ConcreteCommandHandler
import remotecontrolbackend.netty_part.command_handler_part.handler.MockCommandHandler
import javax.inject.Scope
@ChScope
@Subcomponent
interface CommandHandlerSubcomponent {

    fun inject(mockCommandHandler: MockCommandHandler)
     fun inject(concreteCommandHandler: ConcreteCommandHandler)
    @Subcomponent.Builder
    interface  CommHandlerSCBuilder{
        fun build():CommandHandlerSubcomponent
    }
}


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ChScope

