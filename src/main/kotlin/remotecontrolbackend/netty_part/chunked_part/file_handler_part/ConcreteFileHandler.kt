package remotecontrolbackend.netty_part.chunked_part.file_handler_part

import io.netty.channel.ChannelHandler.*
import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.netty_part.utils.ChunkedChain
import javax.inject.Inject

@ChunkedChain
@Sharable
@NettyScope
class ConcreteFileHandler @Inject constructor():AbstractFileHandler (){
    //TODO НУ и здесь еще работы непочатый край....
}