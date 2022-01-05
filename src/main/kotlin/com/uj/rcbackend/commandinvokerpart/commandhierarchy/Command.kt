package com.uj.rcbackend.commandinvokerpart.commandhierarchy


interface Command {
    suspend fun execute(infoToken:Map<String,Any>)
    val description:String?

}