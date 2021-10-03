package remotecontrolbackend.netty_part.request_handler_part.commands

import remotecontrolbackend.dagger.RhScope

@RhScope
interface Command {
    suspend fun execute()
    suspend fun stop()
    val entity:Map<String,Pair<Any,Class<*>>>
}