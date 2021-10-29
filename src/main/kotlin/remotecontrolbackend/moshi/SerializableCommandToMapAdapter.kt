package remotecontrolbackend.moshi

import com.squareup.moshi.*
import remotecontrolbackend.command_invoker_part.command_hierarchy.OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.dagger.CommandInvokerModule.Companion.COMMAND_CLASS_MAP_LITERAL
import javax.inject.Inject
import javax.inject.Named

class SerializableCommandToMapAdapter@Inject constructor(
    @Named(COMMAND_CLASS_MAP_LITERAL)
    val comClassMap:@JvmSuppressWildcards Map<String,Class<out Any>>) {
//TODO Попробовать переделать с ридером
    @ToJson
    fun serCommandToStringStringMap(serializableCommand: SerializableCommand):Map<String,String>{
          return serializableCommand.entityMap
    }
//TODO Вот это стремное говно точно нужно будет потно проверить
    @FromJson
    fun stringToSerCommand(entityMap:Map<String,String>):SerializableCommand{
        val commandType=comClassMap.get(entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME))
        return (commandType!!.getConstructor(Map::class.java).newInstance(entityMap)) as SerializableCommand
    }
}