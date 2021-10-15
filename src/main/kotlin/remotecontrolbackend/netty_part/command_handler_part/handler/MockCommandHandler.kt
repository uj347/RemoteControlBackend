package remotecontrolbackend.netty_part.command_handler_part.handler

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import kotlinx.coroutines.*
import remotecontrolbackend.dagger.ChScope
import remotecontrolbackend.dagger.CommandHandlerSubcomponent
import remotecontrolbackend.command_invoker_part.command_hierarchy.Command
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import javax.inject.Inject

@ChannelHandler.Sharable
@ChScope
class MockCommandHandler (commandHandlerSCBuilder: CommandHandlerSubcomponent.CommHandlerSCBuilder):AbstractCommandHandler(commandHandlerSCBuilder){
   @Inject
    lateinit var commandInvoker: CommandInvoker

    val handlerScope:CoroutineScope= CoroutineScope(Dispatchers.IO)

   init {
     commandHandlerSCBuilder.build().inject(this)

   }
    override fun handlerAdded(ctx: ChannelHandlerContext?) {
       handlerScope.launch { commandInvoker.launchCommandInvoker(this) }
        super.handlerAdded(ctx)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        handlerScope.cancel()
        super.handlerRemoved(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Command?) {
       msg?.let{
           handlerScope.launch {  commandInvoker.postFairCommand(msg)}
       }
    }
}