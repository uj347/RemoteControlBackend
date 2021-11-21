package remotecontrolbackend.file_service_part

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationObserver
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.FileServiceModule.Companion.FILESERVICE_COROUTINE_CONTEXT_LITERAL
import remotecontrolbackend.file_service_part.path_repo_part.DataSetCallBack
import remotecontrolbackend.file_service_part.path_repo_part.DataSetListener
import remotecontrolbackend.file_service_part.path_repo_part.IFilePathRepo
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentSkipListSet
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.isDirectory

//Монитор на корутинах, используя коммонс ио файл чэнж листенер
//Подумать стоит ли заморачиваться с инжектом или же просто создать инстанс в файл сервисе
// Как вариант  - прсто убрать из конструктора файл репо , добавить метод сетРепо и инит, чтобы дать стартовые параметры,а дальше пусть ебется сам
class PathMonitor @Inject constructor(val repo: IFilePathRepo,
                                      @Named(FILESERVICE_COROUTINE_CONTEXT_LITERAL)
                                      val fileServiceContext: CoroutineContext) :
    DataSetListener {
    companion object {
        val logger = LogManager.getLogger()
    }

    //TODO Проверить работает ли это
    val callBackFlow: MutableSharedFlow<Pair<Collection<Path>, DataSetCallBack.Companion.ActionType>> = MutableSharedFlow(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val observableDirs = ConcurrentSkipListSet<Path>()
    private val observers = ConcurrentSkipListSet<FileAlterationObserver>()

    private val monitorScope = CoroutineScope(fileServiceContext)
    private val pathMonitorJob = monitorScope.launch(start = CoroutineStart.LAZY) {
        delay(1000)
        for (observer in observers) {
            observer.checkAndNotify()
        }
    }


    fun initialize() {
        val topLevelDirs = mutableSetOf<Path>()
        val allRepoDirs = repo.get()
            .map { it.toAbsolutePath() }
            .filter { it.isDirectory() }
            .toList().toMutableSet()

        allRepoDirs.extractTopLevelDirsToCollection(observableDirs).also {
            logger.debug("Initialized Path Monitor with $it")
        }

        for (navigableDir in observableDirs) {
            runCatching {
                val observer = FileAlterationObserver(navigableDir.toFile())
                    .also {
                        it.addListener(customFAL)
                        it.initialize()
                    }
                logger.debug("Added observer for dir: $navigableDir")
            }.onFailure {
                logger.error(it)
            }
        }

        pathMonitorJob.start()

    }

    fun stop() {
        monitorScope.cancel(CancellationException("Stopped by user"))
    }


    override fun provideCallBack(): DataSetCallBack {
        return dataSetCallBack
    }
//TODO Most be heavily checked
    /** Потный кусок *** который должен брать только  топ-левел дирректории из одной коллекции и пихать их в другую,
    возвращая коллекцию состоящую из того что он только что пропихнул во вторую коллекцию*/
    private fun Collection<Path>.extractTopLevelDirsToCollection(target: MutableCollection<Path>): Collection<Path> {

        val isDirectory = { p: Path ->
            p.isDirectory()
        }

        val hasParentsInTargetSet = { p: Path ->
            var result = false
            target.forEach { resultDirPath ->
                if (p != resultDirPath && p.startsWith(resultDirPath)) {
                    result = true
                }
            }
            result
        }

        val hasParentsInSourceSet = { p: Path ->
            var result = false
            forEach { sourcePath ->
                if (p != sourcePath && p.startsWith(sourcePath)) {
                    result = true
                }
            }
            result
        }
        val alterationSet = HashSet<Path>()
        this.filter(isDirectory)
            .filterNot(hasParentsInTargetSet)
            .filterNot(hasParentsInSourceSet)
            .forEach {
                alterationSet.add(it)
                target.add(it)
            }
        return alterationSet
    }

    /**Returns true if some observers was removed.*/
    private fun removeObserverForPaths(paths: Collection<Path>): Boolean {
        var result = false
        paths.forEach { path ->
            observers.filter { it.directory.toPath() == path }
                .forEach {
                    logger.debug("Removing observer for dirrectory ${it.directory.toPath()}")
                    it.destroy()
                    result = observers.remove(it)

                }
            observableDirs.filter{it==path}.forEach{observableDirs.remove(it)}
        }
        return result
    }


    /** Returns true if some observers was added**/
    private fun addObserversForPaths(paths: Collection<Path>): Boolean {
        var result = false
        paths.forEach { path ->
            var pathAlreadyObserved = false
            observers.filter { it.directory.toPath() == path }
                .forEach {
                    pathAlreadyObserved = true
                }
            if (!pathAlreadyObserved) {
                kotlin.runCatching {
                    val newObserver = FileAlterationObserver(path.toFile()).also {
                        it.addListener(customFAL)
                        it.initialize()
                        result = true
                    }
                }.onFailure { logger.error(it) }
            }
        }
        return result
    }

    private val customFAL = object : FileAlterationListenerAdaptor() {
        //TODO
        override fun onDirectoryCreate(directory: File?) {
            directory?.let{
                logger.debug("Newly created directory spotted: ${it.toPath()}, adding it to repo")
                repo.add(it.toPath())
            }

        }

        override fun onFileCreate(file: File?) {
            file?.let{
                logger.debug("Newly created file spotted: ${it.toPath()}, adding it to repo")
                repo.add(it.toPath())
            }
        }

        override fun onFileDelete(file: File?) {
            file?.let{
                logger.debug("Deletion of file spotted: ${it.toPath()}, removing it from repo")
                repo.remove(it.toPath())
            }
        }

        override fun onDirectoryDelete(directory: File?) {
            directory?.let{
                logger.debug("Deletion of directory spotted: ${it.toPath()}, removing it from repo")
                repo.remove(it.toPath())
            }
        }
    }

    private val dataSetCallBack=object : DataSetCallBack {
        override fun notify(paths: Collection<Path>, actionType: DataSetCallBack.Companion.ActionType) {
           callBackFlow.tryEmit(paths to actionType)
            when (actionType) {
                //todo?? Или все с этим пунктом
                DataSetCallBack.Companion.ActionType.ADDED -> {
                    paths.extractTopLevelDirsToCollection(observableDirs)
                        .let{addObserversForPaths(it)}
                }
                DataSetCallBack.Companion.ActionType.DELETED -> {
                    removeObserverForPaths(paths)
                }
                DataSetCallBack.Companion.ActionType.MODIFIED -> {
                    //I don't give a fuck about modification
                }
            }


        }
    }


}