package remotecontrolbackend.netty_part.command_handler_part

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.command_invoker_part.command_hierarchy.CommandReference
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.isCacheable
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.netty_part.send200Response
import remotecontrolbackend.netty_part.send500Response
import java.lang.RuntimeException
import javax.inject.Inject

/**
 * В этот хэндлер залетает команда, опционально она может содержать "strategy" параметр с возможными значениями
 *{@link CommandStrategy}
 * при отсутствии стратеджи в POST реквесте будет использована postfair
 */
@ChannelHandler.Sharable
class ConcreteCommandHandler
@Inject constructor(commandInvoker: CommandInvoker) : AbstractCommandHandler(commandInvoker) {


    companion object {
        val _logger = LogManager.getLogger()
    }

    //TODO Вот тут нужно бы потестить, слеплено на коленке
    override val logger = _logger
    val moshi = commandInvoker.commandRepo.moshi

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        super.handlerAdded(ctx)

        commandInvoker.launchCommandInvoker()
    }

    //TODO Нужно много тестить
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
        msg?.let {
            logger.debug("received msg: $msg")
            //Увеличиваем рефкаунт, потому что корутина уходит в паралель и функция возвращается до того, как корутина закончится,
            // и из-за этого в корутине факапается куча всего ссвязанного с рефкаунтом
            msg.retain()
            CoroutineScope(commandInvoker.invokerCoroutineContext + Dispatchers.IO).launch {
                kotlin.runCatching {
                    val receivedCommandsSet: Set<SerializableCommand>? = when (msg.method()) {
                        HttpMethod.GET -> null
                        else -> msg.extractCommands()
                    }
                    logger.debug("msg contains commands: $receivedCommandsSet")


                    when (msg.extractStrategy()) {
                        CommandStrategy.POSTFAIR -> {
                            receivedCommandsSet?.let {
                                for (command in it) {
                                    commandInvoker.postFairCommand(command)
                                }
                            }?:throw RuntimeException("Received null Command Set")

                        }
                        CommandStrategy.POSTNONFAIR -> {
                            receivedCommandsSet?.let {
                                for (command in it) {
                                    commandInvoker.postNonFairCommand(command)
                                }
                            }?:throw RuntimeException("Received null Command Set")
                        }
                        CommandStrategy.PUT -> {
                            receivedCommandsSet?.let {
                                //Не забывай, дорогая вафельница, путнется только последняя команда из запарсенных
                                //ТЫ ведь не натолько тугой чтобы пихнуть больше одной в путе
                                commandInvoker.putCommand(it.last())
                            }?:throw RuntimeException("Received null Command Set")
                        }
                        CommandStrategy.GET -> {
                            ctx?.formGetResponse()?.let {
                                ctx.writeAndFlush(it)
                            }
                        }
                        CommandStrategy.DELETE -> {
                            receivedCommandsSet?.forEach {
                                if (it !is CommandReference) {
                                    throw IllegalArgumentException("There most be only ComandReferences parsed in delete message")
                                }
                            }
                            receivedCommandsSet?.let {
                                for (command in it) {
                                    commandInvoker.commandRepo.removeCommand(command as CommandReference)
                                }
                            }?:throw RuntimeException("Received null Command Set")
//TODO
                        }
                        CommandStrategy.ONLYCACHE ->{
                            if(it.method().name()!=HttpMethod.PUT.name()&&
                                it.method().name()!=HttpMethod.POST.name()){
                                throw RuntimeException("Malformeed request: ONLYCACHE QS Parameter applicble only for POST and PUT")
                            }
                           receivedCommandsSet?.let{
                               receivedCommandsSet
                                   .filter { it.isCacheable() }
                                   .forEach{commandInvoker.commandRepo.addToRepo(it)}
                           }


                        }
                    }
                }.let {
                    it.onSuccess {
                        ctx.send200Response().let {
                            logger.debug("successfully written 200-response")
                        }
                    }
                    it.onFailure {
                        logger.error("error occured in deserialization: $it")
                        ctx.send500Response().let {
                            logger.debug("successfully written 500-response")
                        }
                    }
                }
            }
        }
    }
}









