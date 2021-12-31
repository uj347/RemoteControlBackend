package remotecontrolbackend.file_service_part.path_repo_part

import io.netty.util.internal.ConcurrentSet
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList

interface IFilePathRepo:Iterable<Path> {
    fun initialize()
    fun registerListener(listener: DataSetListener)
    fun deregisterListener(listener: DataSetListener)
    fun get():Collection<Path>
    fun add(vararg path:Path):Boolean
    fun remove(vararg path: Path):Boolean
    /** Irreversably terminates this repo */
    fun terminate()

}