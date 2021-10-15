package remotecontrolbackend.command_invoker_part.command_hierarchy
const val OBLIGATORY_ENTITY_KEY_COMMAND_TYPE="COMMAND_TYPE"
const val OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION="COMMAND_DESCRIPTION"


/** All serialzable commands most contain universal constructor, that accepts Map<String,String>*/
abstract class SerializableCommand(val entityMap:Map<String,String>):Command {


}