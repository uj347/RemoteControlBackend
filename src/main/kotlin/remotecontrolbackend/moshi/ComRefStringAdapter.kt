package remotecontrolbackend.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import remotecontrolbackend.command_invoker_part.command_hierarchy.CommandReference
/** Необходим для того, чтобы КомРеференс могла быть ключом вмапе при сериализации */
class ComRefStringAdapter {

        @FromJson
          fun stringToCommRef(string: String): CommandReference {
            return CommandReference.restoreFromString(string)
        }
        @ToJson
        fun ComRefToString(commandReference: CommandReference): String {
            return commandReference.toString()
        }

}