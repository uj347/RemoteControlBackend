package remotecontrolbackend.command_invoker_part.command_repo

import com.squareup.moshi.JsonClass
import remotecontrolbackend.command_invoker_part.command_hierarchy.Command
import remotecontrolbackend.command_invoker_part.command_hierarchy.OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION
import remotecontrolbackend.command_invoker_part.command_hierarchy.OBLIGATORY_ENTITY_KEY_COMMAND_TYPE
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import java.lang.RuntimeException

@JsonClass(generateAdapter = true)
/**Инстансы нужно создавать только черех ютилити метод createReference */


class CommandReference constructor(
     entityMap:Map<String,String>
) : Comparable<CommandReference>, SerializableCommand(
    entityMap
//
) {
    constructor( commandClassName: String,
                 commandDescription: String? = "EmptyDescription"):this(
        mapOf(OBLIGATORY_ENTITY_KEY_COMMAND_TYPE to commandClassName,
    OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION to commandDescription!!)
                        )
val commandClassName:String
get(){
    return entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_TYPE)!!
}
val commandDescription:String?
    get(){
        return entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION)
    }

    companion object {
        fun createReference(command: Command): CommandReference {
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
        val commandRepo = (infoToken.get(CommandInvoker.INVOKER_INSTANCE_LITERAL) as CommandInvoker).commandRepo
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