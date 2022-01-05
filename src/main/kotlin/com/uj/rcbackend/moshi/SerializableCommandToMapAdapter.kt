package com.uj.rcbackend.moshi

import com.squareup.moshi.*
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.SerializableCommand
import com.uj.rcbackend.dagger.CommandInvokerModule.Companion.COMMAND_CLASS_MAP_LITERAL
import javax.inject.Inject
import javax.inject.Named

class SerializableCommandToMapAdapter@Inject constructor(
    @Named(COMMAND_CLASS_MAP_LITERAL)
    val comClassMap:@JvmSuppressWildcards Map<String,Class<out Any>>) {
    @ToJson
    fun serCommandToStringStringMap(serializableCommand: SerializableCommand):Map<String,String>{
          return serializableCommand.entityMap
    }

    @FromJson
    fun stringToSerCommand(entityMap:Map<String,String>):SerializableCommand{
        val commandType=comClassMap.get(entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME))
        return (commandType!!.getConstructor(Map::class.java).newInstance(entityMap)) as SerializableCommand
    }
}