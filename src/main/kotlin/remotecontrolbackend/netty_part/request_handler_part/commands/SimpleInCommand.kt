package remotecontrolbackend.netty_part.request_handler_part.commands

import remotecontrolbackend.dagger.RhScope
//TOdo
@RhScope
class SimpleInCommand:ExecutableInCommand {
    override suspend fun execute() {
        TODO("Not yet implemented")
    }

    override suspend fun stop() {
        TODO("Not yet implemented")
    }

    override val entity: Map<String, Pair<Any, Class<*>>>
        get() = TODO("Not yet implemented")
}