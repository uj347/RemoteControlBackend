package remotecontrolbackend.file_service_part

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.filefilter.*
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.FileServiceModule.Companion.DROP_BOX_DIRECTORY_LITERAL
import remotecontrolbackend.dagger.FileServiceModule.Companion.FILESERVICE_COROUTINE_CONTEXT_LITERAL
import remotecontrolbackend.dagger.FileServiceScope
import remotecontrolbackend.dagger.FileServiceSubcomponent
import remotecontrolbackend.dagger.PathMonitorFactory
import remotecontrolbackend.file_service_part.file_filters.PathRepoBackedFileFilter
import remotecontrolbackend.file_service_part.path_list_provider_part.IPathListProvider
import remotecontrolbackend.file_service_part.path_repo_part.IFilePathRepo
import java.io.*
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@FileServiceScope
class FileService(
    fileServiceSCBuilder: FileServiceSubcomponent.Builder,
    private var initialPathProvider: IPathListProvider?
) {
    //NB - File repo most contain path for all possible terminal and intermediate nodes
    @Inject
    lateinit var zipper: ZipStreamer

    @Inject
    lateinit var repoProvider: Provider<IFilePathRepo>

    @Inject
    lateinit var pathMonitorFactory: PathMonitorFactory

    @Inject
    @Named(DROP_BOX_DIRECTORY_LITERAL)
    lateinit var dropBoxPath: Path

    @Inject
    @Named(FILESERVICE_COROUTINE_CONTEXT_LITERAL)
    lateinit var fileServiceContext: CoroutineContext

    lateinit var observedPathsRepo: IFilePathRepo


    lateinit var exceptedPathRepo: IFilePathRepo


    val initialized
        get() = _initialized.get()
    private val _initialized = AtomicBoolean(false)

    lateinit var pathMonitor: PathMonitor


    companion object {
        val logger = LogManager.getLogger()

    }

    init {
        fileServiceSCBuilder.build().inject(this)
        observedPathsRepo = repoProvider.get()
        exceptedPathRepo = repoProvider.get()
        logger.debug("In initialization observed repo is: $observedPathsRepo, excepted repo is $exceptedPathRepo")
        pathMonitor = pathMonitorFactory.createFor(observedPathsRepo, exceptedPathRepo)
    }

    suspend fun initializeFileService() {
        logger.debug("In File Service initialization")
        withContext(fileServiceContext) {
            if (!initialized) {
                if (!dropBoxPath.exists()) {
                    Files.createDirectories(dropBoxPath)
                }
                dropBoxPath.extractAllNodes().let {
                    logger.debug("All extracated from dropBox Nodes is: $it")
                    observedPathsRepo.add(*it.toTypedArray())
                }


                initialPathProvider?.let {
                    it.get().flatMap { it.extractAllNodes() }.toTypedArray().let { arr ->
                        observedPathsRepo.add(*arr)
                    }
                }

                pathMonitor.initialize()
                pathMonitor.launch()
                _initialized.set(true)
            }
        }
    }

    suspend fun reInitializeFileService(){
        logger.debug("Reinitialization of File Service started")
        when(initialized){

            //TODO Почему пафМонитор не ребутится
            true->{
                logger.debug("Already initialized->complex initialization")
                fileServiceContext.cancelChildren()
                pathMonitor.stop()
                _initialized.set(false)
                observedPathsRepo=repoProvider.get()
                exceptedPathRepo=repoProvider.get()
                pathMonitor=pathMonitorFactory.createFor(observedPathsRepo,exceptedPathRepo)
                initializeFileService()

            }
            false->{
                logger.debug("Not initialized-> simple initialization")
                initializeFileService()}

        }

    }
    suspend fun additionalPaths(additionalProvider: IPathListProvider) {
        withContext(fileServiceContext) {
            if (initialized) {
                val additonalPaths = additionalProvider.get().flatMap { it.extractAllNodes() }.toTypedArray()
                observedPathsRepo.add(*additonalPaths)
            } else {
                throw RuntimeException("FileService isn't initialized")
            }
        }
    }

    suspend fun getAllPaths(): Collection<Path> {
        return withContext(fileServiceContext) {
            if (initialized) {
                return@withContext observedPathsRepo.get()
            } else {
                throw RuntimeException("FileService isn't initialized")
            }
        }
    }

    suspend fun provideFileStream(path: Path): InputStream {
        return withContext(fileServiceContext) {
            if (initialized) {
                if (path !in observedPathsRepo.get()) {
                    throw IllegalAccessException("Path is out of scope")
                }
                when (path.isDirectory()) {
                    false -> {
                        return@withContext BufferedInputStream(FileInputStream(path.toFile()))
                    }
                    true -> {
                        val (inputStream, job) = zipper.zipThisFile(path.toFile())
                        job.start()
                        return@withContext inputStream
                    }
                }

            } else {
                throw RuntimeException("FileService isn't initialized")
            }
        }
    }

    suspend fun saveFile(stream: InputStream, fileName: String) {
        withContext(fileServiceContext) {
            if (initialized) {
                logger.debug("Invoked file saving with the name of {$fileName}")
                stream.checkAndSave(fileName)

            } else {
                throw RuntimeException("FileService isn't initialized")
            }
        }
    }


    suspend private fun InputStream.checkAndSave(fileName: String, count: Int? = null) {
        withContext(fileServiceContext) {
            val fileExtension = FilenameUtils.getExtension(fileName)
            val pureName = FilenameUtils.removeExtension(fileName)
//            ("check and save is running with fileExtension: $fileExtension and pureName: $pureName")
            val scopeSuffix: String? = when (count) {
                null -> {
                    //("No suffix is needed ")
                    null
                }
                else -> {
                    //("Suffix is $count")
                    "($count)"
                }
            }
            val scopeFileName: String = when (scopeSuffix) {
                null -> {
                    fileName
                }
                else -> {
                    if (count == 1) {
                        pureName + scopeSuffix + "." + fileExtension
                    } else {
                        pureName.substring(0, pureName.length - 3) + scopeSuffix + "." + fileExtension
                    }
                }
            }
//           ("scopeFileName is $scopeFileName")
            val dropBoxFileNames: Collection<String> =
                dropBoxPath.extractAllNodes().map { it.fileName.toString() }.toSet()
            if (scopeFileName in dropBoxFileNames) {
//                logger.debug("DropBox contains file with scope fileName")
                val nextScopeCount: Int = count?.inc() ?: 1
//                "Proceeding to the next invocation of checkAndSave with nextCount: $nextScopeCount")
                this@checkAndSave.checkAndSave(scopeFileName, nextScopeCount)
            } else {
                val newFilePath = dropBoxPath.resolve(Paths.get(scopeFileName))
                BufferedOutputStream(FileOutputStream(newFilePath.toFile())).use {
                    IOUtils.copy(this@checkAndSave, it)
                    it.flush()
                    logger.debug("Saved new File to DropBox: $scopeFileName")
                }

            }
        }
    }


    //TODO Понять почему эта хуйнища выкидывает стакОверфлоу
    fun Path.extractAllNodes(): Collection<Path> {
       var result= FileUtils.listFiles(this.toFile(),TrueFileFilter.TRUE,null)
            .flatMap { it.extractNode()}
        result=result.plusElement(this)

        //TODO TestBench
        return result

    }

    fun File.extractNode():Collection<Path>{

val result= when(this.isDirectory){
    true->{
        kotlin.runCatching {
            FileUtils.listFiles(this,TrueFileFilter.TRUE, null)
                .flatMap{it.extractNode()}.plusElement(this.toPath())
        }.getOrElse {
            logger.debug("spotted exception in ExtractNode: $it ")
            exceptedPathRepo.add(this.toPath())
            logger.debug("Added to excepted paths: $this because of exception: $it")
            emptySet<Path>()
        }
    }
    false->{
       kotlin.runCatching {
           if(this.canRead()){
              setOf(this.toPath())
           }
           else emptySet<Path>()
       }.getOrElse {
           logger.debug("spotted exception in ExtractNode: $it ")
           exceptedPathRepo.add(this.toPath())
           logger.debug("Added to excepted paths: $this because of exception: ${it}")
           emptySet<Path>() }
    }
}
        return result
    }


}







