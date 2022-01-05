package com.uj.rcbackend.commandinvokerpart.commandrepo

import com.squareup.moshi.JsonAdapter
import okio.*
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.CommandReference
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.SerializableCommand
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
fun SerializableCommand.createReference(): CommandReference {
    return CommandReference.createReference(this)
}