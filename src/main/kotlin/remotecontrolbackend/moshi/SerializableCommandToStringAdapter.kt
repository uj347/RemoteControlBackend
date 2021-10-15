package remotecontrolbackend.moshi

import com.squareup.moshi.*
import remotecontrolbackend.command_invoker_part.command_hierarchy.OBLIGATORY_ENTITY_KEY_COMMAND_TYPE
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand

class SerializableCommandToMapAdapter() {
//TODO Попробовать переделать с ридеером
    @ToJson
    fun serCommandToStringStringMap(serializableCommand: SerializableCommand):Map<String,String>{
          return serializableCommand.entityMap
    }
//TODO Вот это стремное говно точно нужно будет потно проверить
    @FromJson
    fun stringToSerCommand(entityMap:Map<String,String>):SerializableCommand{
        val commandType=Class.forName(entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_TYPE))
        return (commandType.getConstructor(Map::class.java).newInstance(entityMap)) as SerializableCommand
    }
}