package com.uj.rcbackend.commandinvokerpart.commandhierarchy
const val OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME="COMMAND_SIMPLE_NAME"
const val OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION="COMMAND_DESCRIPTION"


/** All serialzable commands most contain universal constructor, that accepts Map<String,String>*/
abstract class SerializableCommand(val entityMap:Map<String,String>):Command