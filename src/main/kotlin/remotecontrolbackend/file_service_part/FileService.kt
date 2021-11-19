package remotecontrolbackend.file_service_part

import WORK_DIR_LITERAL
import jdk.jfr.Name
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.file.PathUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import remotecontrolbackend.dagger.FileServiceModule.Companion.DROP_BOX_DIRECTORY_LITERAL
import remotecontrolbackend.dagger.FileServiceScope
import remotecontrolbackend.dagger.FileServiceSubcomponent
import remotecontrolbackend.file_service_part.path_list_provider_part.IPathListProvider
import remotecontrolbackend.file_service_part.path_repo_part.IFilePathRepo
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.exists

@FileServiceScope
class FileService(fileServiceSCBuilder: FileServiceSubcomponent.Builder,
                  var pathProvider: IPathListProvider?) {
   //NB - File repo most contain path for all possible terminal and intermediate nodes
    @Inject
    lateinit var pathsRepo:IFilePathRepo

    @Inject
    @Named(DROP_BOX_DIRECTORY_LITERAL)
    lateinit var dropBoxPath:Path
  init {
      fileServiceSCBuilder.build().inject(this)
      if(!dropBoxPath.exists()){
          Files.createDirectories(dropBoxPath)
      }
      FileUtils.listFilesAndDirs(dropBoxPath.toFile(),TrueFileFilter.TRUE,TrueFileFilter.TRUE).map { it.toPath() }.let {
      //TODO Выглядит слепленным на говне и палках, нжно проверить следующую строку.
          pathsRepo.add(*it.toTypedArray())
      }

  }
    //TODO Выглядит слепленным на говне и палках, нжно проверить следующую строку.
fun Path.extractAllNodes():Collection<Path>{
    return FileUtils.listFilesAndDirs(toFile(),TrueFileFilter.TRUE,TrueFileFilter.TRUE).map{it.toPath()}.toCollection(HashSet<Path>())
}
    //
    fun resscanDropBox(){

    }
}
