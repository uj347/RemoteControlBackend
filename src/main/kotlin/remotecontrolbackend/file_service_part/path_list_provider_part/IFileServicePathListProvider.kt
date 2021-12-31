package remotecontrolbackend.file_service_part.path_list_provider_part

import java.nio.file.Path
import javax.inject.Provider

fun interface IFileServicePathListProvider:Provider<Collection<Path>> {

     override fun get():Collection<Path>
}