package remotecontrolbackend.command_invoker_part.command_hierarchy


import com.squareup.moshi.JsonClass
import kotlinx.coroutines.delay
import java.nio.file.Files
import java.nio.file.Path
//@JsonClass(generateAdapter = true)

//TODO Проверить и это тоже
class MockCommand(entityMap:Map<String,String>): SerializableCommand(entityMap) {
constructor():this(mapOf(OBLIGATORY_ENTITY_KEY_COMMAND_TYPE to MockCommand.javaClass.canonicalName,
OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION to "MockCommand #$counter"))

    constructor(description:String):this(mapOf(OBLIGATORY_ENTITY_KEY_COMMAND_TYPE to MockCommand.javaClass.canonicalName,
        OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION to description))

    override val description: String?
        get() = entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION)


    companion object{

        @JvmField
        var counter:Int=0
    }
    val comN:Int
    init{
       comN= counter++
        println("Creating Mock Command #$comN")
    }
    override suspend fun execute(infoToken: Map<String, Any>) {
        println("Command $comN started with infoToken:\n$infoToken")
        delay(500)
        println("Command $comN finished")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MockCommand

        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        return description?.hashCode() ?: 0
    }

}