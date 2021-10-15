package remotecontrolbackend.netty_part.command_handler_part.handler

import io.netty.channel.ChannelHandler
import remotecontrolbackend.dagger.ChScope
import remotecontrolbackend.dagger.CommandHandlerSubcomponent

@ChannelHandler.Sharable
@ChScope
 abstract class ConcreteCommandHandler(commandHandlerSCBuilder: CommandHandlerSubcomponent.CommHandlerSCBuilder)
    :AbstractCommandHandler(commandHandlerSCBuilder) {
    //TODO
}