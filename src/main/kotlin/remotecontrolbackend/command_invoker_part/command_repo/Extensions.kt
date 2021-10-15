package remotecontrolbackend.command_invoker_part.command_repo

import com.squareup.moshi.JsonAdapter
import okio.*
import remotecontrolbackend.command_invoker_part.command_hierarchy.Command
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.jvm.Throws

@Throws (IOException::class)
 fun < T> BufferedSink.writeAndFlushJson(adapter:JsonAdapter<T>,value:T?){
    adapter.toJson(this,value)
    this.flush()
}
/**Creates sink with op-optons TRUNCATE and CREATE */
fun Path.getBufferedSink():BufferedSink{
   return this.sink(StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING).buffer()
}
/** Creates buffered source from this Path */
fun Path.getBufferedSource():BufferedSource{
    return this.source().buffer()
}
/**Create reference for this command */
fun Command.createReference():CommandReference{
    return CommandReference.createReference(this)
}