package remotecontrolbackend.netty_part.command_handler_part

import com.squareup.moshi.Types
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.LoggerConfig
import remotecontrolbackend.command_invoker_part.command_hierarchy.CommandReference
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.reflect.Type
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.jvm.Throws

@ChannelHandler.Sharable
abstract class AbstractCommandHandler(val commandInvoker: CommandInvoker) :
    SimpleChannelInboundHandler<FullHttpRequest>() {
    companion object {
        private val _logger = LogManager.getLogger()
    }

    abstract val logger: Logger

    @Throws(IllegalArgumentException::class)
    fun FullHttpRequest.extractStrategy(): CommandStrategy {
        _logger.debug("Extracting Strategy from request: $this, that has METHOD: ${this.method()}")
        //TODO Пидумать возврат списка возмжных комманд при гет реквесте
        if (this.method() != HttpMethod.PUT &&
            this.method() != HttpMethod.POST &&
            this.method() != HttpMethod.GET &&
            this.method() != HttpMethod.DELETE
        ) {
            throw IllegalArgumentException("Request most be one of PUT,POST or GET")

        }
        if (this.method() == HttpMethod.PUT) {
            return CommandStrategy.PUT
        }
        if (this.method() == HttpMethod.GET) {
            return CommandStrategy.GET
        }
        if (this.method() == HttpMethod.DELETE) {
            return CommandStrategy.DELETE
        }
//Вернуть фейр иили нон фейр пост, по умолчанию фэйр при отсутствиипараметров
        val qsDecoder = QueryStringDecoder(this.uri())
        val receivedStrategyString: String = when (qsDecoder.parameters().contains(COMAND_STRATEGY_LITERAL)) {
            true -> qsDecoder.parameters().get(COMAND_STRATEGY_LITERAL)!!.get(0)
            false -> return CommandStrategy.POSTFAIR
        }
        return CommandStrategy.valueOf(receivedStrategyString)

    }


    fun CommandInvoker.getCachedCommandReferences(): Set<out CommandReference> {
        return this.commandRepo.pointerMap?.keys ?: emptySet<CommandReference>()
    }

    //TODO Testing needed
    fun ChannelHandlerContext.formGetResponse(): FullHttpResponse {
        val moshi = commandInvoker.commandRepo.moshi
        val setType: Type = Types.newParameterizedType(Set::class.java, SerializableCommand::class.java)
        val adapter = moshi.adapter<Set<SerializableCommand>>(setType)
        val stringRepresentationOfSet = adapter.toJson(commandInvoker.getCachedCommandReferences())
        _logger.debug("In formGetResponse: stringRepresantation of set is : $stringRepresentationOfSet")
        val serializedComRefSet: ByteBuf = ByteBufUtil.encodeString(this.alloc(), CharBuffer.wrap(stringRepresentationOfSet),StandardCharsets.UTF_8)
        _logger.debug("Prduced serialized set of ComRef: $serializedComRefSet")

        val response: DefaultFullHttpResponse =
            DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, serializedComRefSet)
                .also {
                    it.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    it.headers().add(HttpHeaderNames.CONTENT_LENGTH, serializedComRefSet.readableBytes())
                    it.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
                }
        _logger.debug("Produced GET response: $response")
        return response
    }
//TODO Сделаь так чтобы возвращало список комманд
    fun FullHttpRequest.extractCommands(): Set<SerializableCommand> {
        if (HttpUtil.getMimeType(this)!= HttpHeaderValues.APPLICATION_JSON.toString() ||
            HttpUtil.getContentLength(this,0L) == 0L
        ) {
            throw RuntimeException("Command requests most contain JSON")
        }
        commandInvoker.commandInvokerSubcomponent
            .getMoshi()
            .adapter<Set<SerializableCommand>>(Types.newParameterizedType(Set::class.java,SerializableCommand::class.java))
            .let {
               var result: Set<SerializableCommand>?=null

               return it.fromJson(this.content().toString(StandardCharsets.UTF_8)).also {
                   _logger.debug("deserialized commands from incoming request: $it")
               }?:throw RuntimeException("Content can't be deserialized")
            }
}
    }

const val COMAND_STRATEGY_LITERAL = "strategy"

enum class CommandStrategy {
    POSTFAIR, POSTNONFAIR, PUT, GET, DELETE
}