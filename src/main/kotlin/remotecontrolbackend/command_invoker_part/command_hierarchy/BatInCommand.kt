package remotecontrolbackend.command_invoker_part.command_hierarchy


class BatInCommand(val batText:String,
                   override val description: String?=null): Command {

    override suspend fun execute(infoToken:Map<String,Any>) {
        TODO("Not yet implemented")
    }





}