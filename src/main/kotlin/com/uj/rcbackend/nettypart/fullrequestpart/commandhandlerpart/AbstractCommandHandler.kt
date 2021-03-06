package com.uj.rcbackend.nettypart.fullrequestpart.commandhandlerpart

import com.squareup.moshi.Types
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandler.*
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.CommandReference
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.SerializableCommand
import com.uj.rcbackend.commandinvokerpart.commandinvoker.CommandInvoker
import com.uj.rcbackend.dagger.NettySubComponent.Companion.COMMAND_HANDLER_LITERAL
import com.uj.rcbackend.nettypart.fullrequestpart.FullRequestWorkModeHandler
import com.uj.rcbackend.nettypart.utils.SpecificChain
import java.lang.RuntimeException
import java.lang.reflect.Type
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import kotlin.jvm.Throws
@SpecificChain(chainType = SpecificChain.ChainType.FULLREQUEST)
@Sharable
abstract class AbstractCommandHandler(val commandInvoker: CommandInvoker) :
    FullRequestWorkModeHandler() {
    companion object {
        private val logger = LogManager.getLogger()
        const val COMMAND_QUERY="command"


    }
    override val handlerQuery= COMMAND_QUERY
    val handlerDescription= COMMAND_HANDLER_LITERAL
    abstract val logger: Logger

    @Throws(IllegalArgumentException::class)
    protected fun FullHttpRequest.extractStrategy(): CommandStrategy {
        logger.debug("Extracting Strategy from request: $this, that has METHOD: ${this.method()}")

        if (this.method() != HttpMethod.PUT &&
            this.method() != HttpMethod.POST &&
            this.method() != HttpMethod.GET &&
            this.method() != HttpMethod.DELETE
        ) {
            throw IllegalArgumentException("Request most be one of PUT,POST,DELETE or GET")

        }
        val qsDecoder = QueryStringDecoder(this.uri())
        val normalizedParameters = qsDecoder.parameters().getNormalizedQuerryStringParameters()

        if (normalizedParameters.contains(COMAND_STRATEGY_LITERAL)) {
            normalizedParameters.get(COMAND_STRATEGY_LITERAL)!!.get(0).let {
                if (it == CommandStrategy.ONLYCACHE.name) {
                    return CommandStrategy.ONLYCACHE
                }
            }
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
//?????????????? ???????? ???????? ?????? ???????? ????????, ???? ?????????????????? ???????? ?????? ????????????????????????????????????????
        val receivedStrategyString: String = when (normalizedParameters.contains(COMAND_STRATEGY_LITERAL)) {
            true -> normalizedParameters.get(COMAND_STRATEGY_LITERAL)!!.get(0)
            false -> return CommandStrategy.POSTFAIR
        }
        return CommandStrategy.valueOf(receivedStrategyString)

    }


    suspend fun CommandInvoker.getCachedCommandReferences(): Collection<out CommandReference> {
        return this.commandRepo.getAllReferences()
    }


    suspend protected fun ChannelHandlerContext.formGetResponse(): FullHttpResponse {
        val moshi = commandInvoker.moshi
        val setType: Type = Types.newParameterizedType(Set::class.java, SerializableCommand::class.java)
        val adapter = moshi.adapter<Collection<SerializableCommand>>(setType)
        val stringRepresentationOfSet = adapter.toJson(commandInvoker.getCachedCommandReferences())
        logger.debug("In formGetResponse: stringRepresantation of set is : $stringRepresentationOfSet")
        val serializedComRefSet: ByteBuf =
            ByteBufUtil.encodeString(this.alloc(), CharBuffer.wrap(stringRepresentationOfSet), StandardCharsets.UTF_8)
        logger.debug("Prduced serialized set of ComRef: $serializedComRefSet")

        val response: DefaultFullHttpResponse =
            DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, serializedComRefSet)
                .also {
                    it.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    it.headers().add(HttpHeaderNames.CONTENT_LENGTH, serializedComRefSet.readableBytes())
                    it.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
                }
        logger.debug("Produced GET response: $response")
        return response
    }


    protected fun FullHttpRequest.extractCommands(): Set<SerializableCommand> {
        if (HttpUtil.getMimeType(this) != HttpHeaderValues.APPLICATION_JSON.toString() ||
            HttpUtil.getContentLength(this, 0L) == 0L
        ) {
            throw RuntimeException("Command requests most contain JSON")
        }
        commandInvoker.commandInvokerSubcomponent
            .getMoshi()
            .adapter<Set<SerializableCommand>>(
                Types.newParameterizedType(
                    Set::class.java,
                    SerializableCommand::class.java
                )
            )
            .let {
                var result: Set<SerializableCommand>? = null

                return it.fromJson(this.content().toString(StandardCharsets.UTF_8)).also {
                    logger.debug("deserialized commands from incoming request: $it")
                } ?: throw RuntimeException("Content can't be deserialized")
            }
    }

    //TODO TESTING NEEDED
    protected fun Map<String, List<String>>.getNormalizedQuerryStringParameters(): Map<String, List<String>> {
        val mutableMap = HashMap<String, List<String>>(this)
        this.entries.forEach {
            val valueList = ArrayList<String>()
            it.value.forEach {
                valueList.add(it.uppercase())
            }
            mutableMap.put(
                it.key.uppercase(),
                valueList
            )
        }
        return mutableMap

    }


}

const val COMAND_STRATEGY_LITERAL = "STRATEGY"

enum class CommandStrategy {
    POSTFAIR, POSTNONFAIR, PUT, GET, DELETE, ONLYCACHE
}