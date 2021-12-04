package remotecontrolbackend.file_service_part.path_list_provider_part

import java.nio.file.Path

class HardCodePathListProvider (val initialPaths:Collection<Path>):IPathListProvider {
    override fun get(): Collection<Path> {
        return initialPaths
    }
}