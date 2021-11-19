package remotecontrolbackend.file_service_part.path_list_provider_part

import java.nio.file.Path

interface IPathListProvider {
    fun provide():Collection<Path>
}