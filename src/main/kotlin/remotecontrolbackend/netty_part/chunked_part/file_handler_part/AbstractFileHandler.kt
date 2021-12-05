package remotecontrolbackend.netty_part.chunked_part.file_handler_part


import remotecontrolbackend.dagger.NettySubComponent.Companion.FILE_HANDLER_LITERAL
import remotecontrolbackend.netty_part.chunked_part.ChunkWorkModeHandler

abstract class AbstractFileHandler():ChunkWorkModeHandler () {
    companion object{
        const val FILE_QUERY="file"
    }
    override val handlerQuery: String= FILE_QUERY
    override val handlerDescription=FILE_HANDLER_LITERAL
}