package com.uj.rcbackend.nettypart.chunkedpart.robothandlerpart

import com.uj.rcbackend.dagger.NettySubComponent.Companion.ROBOT_HANDLER_LITERAL
import com.uj.rcbackend.nettypart.chunkedpart.ChunkWorkModeHandler
import com.uj.rcbackend.nettypart.utils.SpecificChain

@SpecificChain(SpecificChain.ChainType.CHUNKED)
abstract class AbstractRobotHandler:ChunkWorkModeHandler(){
companion object{
    const val ROBOT_QUERY="robot"
}
    override val handlerQuery: String= ROBOT_QUERY
    override val handlerDescription =ROBOT_HANDLER_LITERAL


}