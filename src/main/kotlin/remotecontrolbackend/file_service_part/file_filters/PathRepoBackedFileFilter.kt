package remotecontrolbackend.file_service_part.file_filters

import org.apache.commons.io.file.PathUtils
import org.apache.commons.io.filefilter.FileFilterUtils
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.file_service_part.path_repo_part.IFilePathRepo
import java.io.File
/** Возвращает тру если файл паф в репо*/
class PathRepoBackedFileFilter(private val repo:IFilePathRepo):IOFileFilter {
    companion object{
        val logger=LogManager.getLogger()
    }
    override fun accept(file: File?): Boolean {
//        logger.debug("now filtering: $file")
        file?.let{file->
//            logger.debug("file in repo: ${file.toPath() in repo.get()}")
            return (file.toPath() in repo.get())||
                    (repo.get().any {file.toPath().startsWith(it)})
        }?:return false
    }

    override fun accept(dir: File?, name: String?): Boolean {
       if (dir!=null&&name!=null) {
           return ((dir.toPath().resolve(name)) in repo.get())
       }else return false
    }
    /** Возвращает инвертнутый файлфильтер*/
    fun invert():IOFileFilter{
        return FileFilterUtils.notFileFilter(this)
    }
}