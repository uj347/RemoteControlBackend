package com.uj.rcbackend.commandinvokerpart.commandhierarchy.mocks


import kotlinx.coroutines.delay
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.SerializableCommand

//@JsonClass(generateAdapter = true)

//TODO Проверить и это тоже
class MockCommandV2(entityMap:Map<String,String>): SerializableCommand(entityMap) {
constructor():this(mapOf(
    OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME to MockCommandV2::class.java.simpleName,
OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION to "MockCommandV2 #$counter"))

    constructor(description:String):this(mapOf(
        OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME to MockCommandV2::class.java.simpleName,
        OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION to description))

    override val description: String?
        get() = entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION)


//    companion object{
//        var counter:Int=0
//    }
    val comN:Int
    init{
       comN= counter++
        println("Creating MockV2 Command #$comN")
    }
    override suspend fun execute(infoToken: Map<String, Any>) {
        println("MockV2 $comN started with infoToken:\n$infoToken")
        delay(500)

        println("MockV2 $comN finished")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MockCommandV2

        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        return description?.hashCode() ?: 0
    }

}