package remotecontrolbackend.command_invoker_part.command_hierarchy


interface Command {
    suspend fun execute(infoToken:Map<String,Any>)
    val description:String?

}