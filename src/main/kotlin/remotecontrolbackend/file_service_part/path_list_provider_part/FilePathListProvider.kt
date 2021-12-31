package remotecontrolbackend.file_service_part.path_list_provider_part

import java.lang.RuntimeException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readLines

class FilePathListProvider(val manifestFile: Path):IFileServicePathListProvider {
    private val pathsFromManifest:Collection<Path>
    init{
        if(!manifestFile.exists()){
            throw RuntimeException("Manifest file doesn't exists")
        }
        pathsFromManifest=manifestFile.readLines(StandardCharsets.UTF_8).map{Paths.get(it)}.toList()
    }

    override fun get(): Collection<Path> {
       return pathsFromManifest
    }
}