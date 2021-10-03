package remotecontrolbackend.netty_part.request_handler_part.commands

import remotecontrolbackend.dagger.RhScope

@RhScope
class BatInCommand:ExecutableInCommand {
    companion object{
        val entityFields= setOf<String>(

        )
    }
    override suspend fun execute() {
        TODO("Not yet implemented")
    }

    override suspend fun stop() {
        TODO("Not yet implemented")
    }

    override val entity: Map<String, Pair<Any, Class<*>>>
        get() = TODO("Not yet implemented")
}