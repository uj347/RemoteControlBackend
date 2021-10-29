package remotecontrolbackend.netty_part.command_handler_part.handler

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.netty_part.send200Response
import remotecontrolbackend.netty_part.send500Response
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
        const val LOGGER_NAME = "ConcreteCommandHandlerLogger"
        val _logger = LogManager.getLogger(COMMAND_HANDLER_LOGGER_SUPERCLASS + "." + LOGGER_NAME)
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
            val receivedCommand:SerializableCommand? =when(msg.method()){
                HttpMethod.GET->null
                else->msg.extractCommand()
            }

            logger.debug("msg contains command: $receivedCommand")


            CoroutineScope(commandInvoker.invokerCoroutineContext+ Dispatchers.IO).launch {
                kotlin.runCatching {
                    when (msg.extractStrategy()) {
                        CommandStrategy.POSTFAIR -> {
                            receivedCommand?.let{ commandInvoker.postFairCommand(it) }
                        }
                        CommandStrategy.POSTNONFAIR -> {
                            receivedCommand?.let{ commandInvoker.postNonFairCommand(it)}
                        }
                        CommandStrategy.PUT -> {
                            receivedCommand?.let{ commandInvoker.putCommand(it)}
                        }
                        CommandStrategy.GET -> {
                            ctx?.formGetResponse()?.let {
                                ctx.writeAndFlush(it)
                            }
                        }                            //Похоже нужно будет соордить метод для экстракции все референсов из репо, сериализации их и отправки в теле HHTTP response
                    }
                }.let {
                    it.onSuccess {
                        ctx.send200Response().let {
                            logger.debug("successfully written 200-response")
                        }
                    }
                    it.onFailure {
                        logger.debug("error occured in derealization: $it")
                        ctx.send500Response().let {
                            logger.debug("successfully written 500-response")
                        }
                    }
                }
            }
        }
    }
}









