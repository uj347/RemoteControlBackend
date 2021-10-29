package remotecontrolbackend.command_invoker_part.command_hierarchy

import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import java.lang.RuntimeException


/**Инстансы нужно создавать только черех ютилити метод createReference */

const val OBLIGATORY_ENTITY_KEY_COMMAND_REFERENT_TYPE="REFERENT_TYPE"


class CommandReference constructor(
    entityMap: Map<String, String>
) : Comparable<CommandReference>, SerializableCommand(
    entityMap
//
) {
    constructor(
        commandClassName: String,
        commandDescription: String? = "EmptyDescription"
    ) : this(
        mapOf(
            OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME to CommandReference::class.java.simpleName,
            OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION to commandDescription!!,
            OBLIGATORY_ENTITY_KEY_COMMAND_REFERENT_TYPE  to commandClassName
        )
    )

    init{if (!entityMap.containsKey(OBLIGATORY_ENTITY_KEY_COMMAND_REFERENT_TYPE)) {
        throw RuntimeException("All references most contain REFERENT TYPE in the entity map")
    }}

    val commandClassName: String
        get() {
            return entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_REFERENT_TYPE)!!
        }
    val commandDescription: String?
        get() {
            return entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION)
        }

    companion object {
        fun createReference(command: SerializableCommand): CommandReference {
            return CommandReference(command::class.qualifiedName.toString(), command.description)
        }

        fun restoreFromString(string: String): CommandReference {
            val processedSplitted = string.removePrefix("Repocommand:").split("||")
            if (processedSplitted.size != 2) throw RuntimeException("Non-applicable string!")
            return CommandReference(processedSplitted[0], processedSplitted[1])

        }
    }

    //TODO Вытащитьь команду из репо и запусттить
    override suspend fun execute(infoToken: Map<String, Any>) {
        val commandRepo = (infoToken.get(INVOKER_INSTANCE_LITERAL) as CommandInvoker).commandRepo
        commandRepo.getCommand(this)?.execute(infoToken)
            ?: throw RuntimeException("There is no command for reference: $this")
    }

    override val description: String? = "Reference for {$commandClassName||$commandDescription}"


    override fun toString(): String {
        return "Repocommand:$commandClassName||$commandDescription"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandReference

        if (commandClassName != other.commandClassName) return false
        if (commandDescription != other.commandDescription) return false

        return true
    }

    override fun hashCode(): Int {
        var result = commandClassName.hashCode()
        result = 31 * result + (commandDescription?.hashCode() ?: 0)
        return result
    }

    override fun compareTo(other: CommandReference): Int {
        return this.toString().compareTo(other.toString())
    }
}