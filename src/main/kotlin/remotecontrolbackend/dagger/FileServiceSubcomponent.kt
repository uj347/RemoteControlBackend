package remotecontrolbackend.dagger

import APP_COROUTINE_CONTEXT_LITERAL
import WORK_DIR_LITERAL
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers
import remotecontrolbackend.file_service_part.FileService
import remotecontrolbackend.file_service_part.PathMonitor
import remotecontrolbackend.file_service_part.path_repo_part.IFilePathRepo
import remotecontrolbackend.file_service_part.path_repo_part.RuntimeFilePathRepo
import java.nio.file.Path
import javax.inject.Named
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext

@FileServiceScope
@Subcomponent(modules = [FileServiceModule::class])
interface FileServiceSubcomponent {
fun inject(fileService: FileService)
fun getRuntimeFilePathRepo():RuntimeFilePathRepo
fun getPathMonitor():PathMonitor
    @Subcomponent.Builder
    interface Builder {
        fun build(): FileServiceSubcomponent
    }
}

@Module
interface FileServiceModule{
//TODO Пока без выбора, мб позже что то придумаю с другими имплементациями пафРепо
    @FileServiceScope
    @Binds
    fun bindFilePathRepo(runtimeFilePathRepo: RuntimeFilePathRepo):IFilePathRepo


    companion object{
    const val DROP_BOX_DIRECTORY_LITERAL="DROP_BOX_DIRECTORY"
   const val FILE_SERVICE_DIRECTORY_LITERAL="FILE_SERVICE_DIRECTORY"


    const val DROP_BOX_DIRECTORY_NAME="Dropbox"
    const val FILE_SERVICE_DIRECTORY_NAME="FileService"

    const val FILESERVICE_COROUTINE_CONTEXT_LITERAL="FILESERVICE_COROUTINE_CONTEXT"




@FileServiceScope
    @Provides
    @Named(DROP_BOX_DIRECTORY_LITERAL)
    fun provideDropBoxDirPath(@Named(FILE_SERVICE_DIRECTORY_LITERAL)fileServiceDirPath:Path):Path{
        return fileServiceDirPath.resolve(DROP_BOX_DIRECTORY_NAME)
    }
    @FileServiceScope
    @Provides
    @Named(FILE_SERVICE_DIRECTORY_LITERAL)
    fun provideFileServiceDirPath(@Named(WORK_DIR_LITERAL)workDirPath:Path):Path{
        return workDirPath.resolve(FILE_SERVICE_DIRECTORY_NAME)
    }

    @Provides
    @FileServiceScope
    @Named(FILESERVICE_COROUTINE_CONTEXT_LITERAL)
fun provideFileServiceCoroutineContext(
        @Named(APP_COROUTINE_CONTEXT_LITERAL)
        appCoroutineContext: CoroutineContext
    ): CoroutineContext {return appCoroutineContext + Dispatchers.IO}
}
}



@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FileServiceScope