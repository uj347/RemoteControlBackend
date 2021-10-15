package remotecontrolbackend.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import remotecontrolbackend.command_invoker_part.command_repo.CommandReference

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