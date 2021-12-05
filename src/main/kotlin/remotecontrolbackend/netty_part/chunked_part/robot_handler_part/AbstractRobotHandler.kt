package remotecontrolbackend.netty_part.chunked_part.robot_handler_part

import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.dagger.NettySubComponent.Companion.ROBOT_HANDLER_LITERAL
import remotecontrolbackend.netty_part.chunked_part.ChunkWorkModeHandler
import remotecontrolbackend.netty_part.utils.ChunkedChain

@ChunkedChain
abstract class AbstractRobotHandler:ChunkWorkModeHandler(){
companion object{
    const val ROBOT_QUERY="robot"
}
    override val handlerQuery: String= ROBOT_QUERY
    override val handlerDescription =ROBOT_HANDLER_LITERAL


}