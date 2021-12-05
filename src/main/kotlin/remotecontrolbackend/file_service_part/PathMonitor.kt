package remotecontrolbackend.file_service_part

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationObserver
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.FileServiceModule.Companion.FILESERVICE_COROUTINE_CONTEXT_LITERAL
import remotecontrolbackend.dagger.PathMonitorFactory.Companion.EXCEPTED_PATH_REPO_LITERAL
import remotecontrolbackend.dagger.PathMonitorFactory.Companion.OBSERVED_PATH_REPO_LITERAL
import remotecontrolbackend.file_service_part.path_repo_part.DataSetCallBack
import remotecontrolbackend.file_service_part.path_repo_part.DataSetListener
import remotecontrolbackend.file_service_part.path_repo_part.IFilePathRepo
import java.io.File
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Named
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

//Монитор на корутинах, используя коммонс ио файл чэнж листенер
//Подумать стоит ли заморачиваться с инжектом или же просто создать инстанс в файл сервисе
// Как вариант  - прсто убрать из конструктора файл репо , добавить метод сетРепо и инит, чтобы дать стартовые параметры,а дальше пусть ебется сам
class PathMonitor @AssistedInject constructor(
    @Assisted(OBSERVED_PATH_REPO_LITERAL)
    val observedPathsRepo: IFilePathRepo,
    @Assisted(EXCEPTED_PATH_REPO_LITERAL)
    val exceptedPathsRepo:IFilePathRepo,
    @Named(FILESERVICE_COROUTINE_CONTEXT_LITERAL)
    val fileServiceContext: CoroutineContext
) : DataSetListener {

    companion object {
        val logger = LogManager.getLogger()
    }
    init {
        logger.debug("New pathMonitor creation: ${this.toString()}")
    }


    val observedRepoCallBackNotificationFlow: SharedFlow<Pair<Collection<Path>, DataSetCallBack.Companion.ActionType>>
        get() = _observedRepoCallBackNotificationFlow
    private val _observedRepoCallBackNotificationFlow: MutableSharedFlow<Pair<Collection<Path>, DataSetCallBack.Companion.ActionType>> =
        MutableSharedFlow()

    val fileAlterationNotificationFlow: SharedFlow<Pair<Path, String>>
        get() = _fileAlterationNotificationFlow
    private val _fileAlterationNotificationFlow = MutableSharedFlow<Pair<Path, String>>()

    val observableDirs: Set<Path>
        get() = _observableDirs
    private val _observableDirs = ConcurrentHashMap.newKeySet<Path>()

    private val observers = ConcurrentHashMap<Path, FileAlterationObserver>()

//    private val monitorContext = fileServiceContext
    private val monitorScope = CoroutineScope(fileServiceContext+SupervisorJob(fileServiceContext.job))
    private var pathMonitorJob: Job? = null

    val initialized: Boolean
        get() = _initialized
    private var _initialized = false

    val launched: Boolean
        get() = pathMonitorJob?.isActive ?: false


    fun initialize() {
        val topLevelDirs = mutableSetOf<Path>()
        val allRepoDirs = observedPathsRepo.get()
            .map { it.toAbsolutePath() }
            .filter { it.isDirectory() }
            .toList().toMutableSet()
        logger.debug("All content of repo is ${observedPathsRepo.get()}")
        logger.debug("All observed dirs is: $allRepoDirs")

        allRepoDirs.findTopLevelDirs(_observableDirs).let {
            _observableDirs.addAll(it)
            logger.debug("Initialized $this Path Monitor with $it")
        }

        for (navigableDir in _observableDirs) {
            runCatching {
                val observer = FileAlterationObserver(navigableDir.toFile())
                    .also {
                        it.addListener(customFAL)
                        it.initialize()
                        observers.put(navigableDir, it)

                    }
                logger.debug("Added observer for dir: $navigableDir")
            }.onFailure {
                logger.error(it)
            }
        }
        _initialized = true
    }


    fun launch() {
        logger.debug("PathMonitor $this launched")
        if (!launched) {
            logger.debug("Proceeding to register this Moniator [$this] as listener for repo[$observedPathsRepo]")
            observedPathsRepo.registerListener(this)
            pathMonitorJob = monitorScope.launch {
                launch {
                    while (this.isActive) {
                        for (entry in observers) {
                            entry.value.let { observer ->
                                observer.checkAndNotify()
                            }
                        }
                        delay(1000)
                    }
                }
                launch {
                    while (isActive) {
                        delay(15000)
                        cleanUp()
                    }
                }
            }
        }
    }


    fun stop() {
        //TODO Точно лии это здесь нужно
        logger.debug("Stopping $this pathMonitor")
        observedPathsRepo.deregisterListener(this)
        pathMonitorJob?.cancel()
    }


    override fun provideCallBack(repoToListen:IFilePathRepo): DataSetCallBack {
        when (repoToListen){
            observedPathsRepo->return observedPathsRepoCallBack
            exceptedPathsRepo->return exceptedPathsRepoCallBack
            else->throw RuntimeException("You trying to pass me some Unknown Repo")
        }
    }

    /** Очистть репо от несущестующих путей*/
    private fun cleanUp() {
        val repoPaths = observedPathsRepo.get()
        for (path in repoPaths) {
            if (!path.exists()) {
                observedPathsRepo.remove(path)
            }
        }
    }

//TODO Most be heavily checked
    /** Потный кусок *** который должен брать только  топ-левел дирректории из одной коллекции и возвращать их в виде списка */
    private fun Collection<Path>.findTopLevelDirs(target: MutableCollection<Path>): Collection<Path> {
logger.debug("In finding top lvl dirs with collection:$this \n and target: $target ")
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
        val resultSet = HashSet<Path>()
        this.filter(isDirectory)
            .filterNot(hasParentsInTargetSet)
            .filterNot(hasParentsInSourceSet)
            .forEach {
                resultSet.add(it)
            }
        return resultSet
    }

    /**Returns true if some observers was removed.*/
    private fun removeObserverForPaths(paths: Collection<Path>): Boolean {
        var result = false
        paths.forEach { path ->
            observers.filter { it.key == path }
                .forEach {
                    logger.debug("Removing observer for dirrectory ${it.key}")
                    result = observers.remove(it.key, it.value)
                    it.value.destroy()
                }
            _observableDirs.filter { it == path }.forEach { _observableDirs.remove(it) }
        }
        logger.debug("Result of topLvl Extraction is:")
        return result
    }

//    private fun removePathsFromMonitorScope(paths: Collection<Path>) {
//        for (path in paths) {
//            observers.get(path)?.destroy()
//            observers.remove(path)
//            _observableDirs.remove(path)
//
//        }
//    }

    /** Returns true if some observers was added**/
    private fun addObserversForPaths(paths: Collection<Path>): Boolean {
        var result = false
        paths.forEach { path ->
            var pathAlreadyObserved = false
            observers.filter { it.key == path }
                .forEach {
                    pathAlreadyObserved = true
                }
            if (!pathAlreadyObserved) {
                kotlin.runCatching {
                    val newObserver = FileAlterationObserver(path.toFile()).also {
                        it.addListener(customFAL)
                        it.initialize()
                        observers.put(path, it)
                        result = true
                    }
                }.onFailure { logger.error(it) }
            }
        }
        return result
    }

    private val customFAL = object : FileAlterationListenerAdaptor() {
        //TODO
        fun File.notifyAlteration(altType: String) {
            val alteredPath = this.toPath()
            monitorScope.launch {
                _fileAlterationNotificationFlow.emit(alteredPath to altType)
            }
        }

        override fun onFileChange(file: File?) {
            file?.let {
                logger.debug("Changed file spotted: ${it.toPath()}, adding it to repo")
                observedPathsRepo.add(it.toPath())
                it.notifyAlteration("FILE_CHNG")
            }
        }

        override fun onDirectoryCreate(directory: File?) {
            directory?.let {
                logger.debug("Newly created directory spotted: ${it.toPath()}, adding it to repo")
                observedPathsRepo.add(it.toPath())
                it.notifyAlteration("DIR_CRT")
            }

        }

        override fun onFileCreate(file: File?) {
            file?.let {
                logger.debug("Newly created file spotted: ${it.toPath()}, adding it to repo")
                observedPathsRepo.add(it.toPath())
                it.notifyAlteration("FILE_CRT")
            }
        }

        override fun onFileDelete(file: File?) {
            file?.let {
                logger.debug("Deletion of file spotted: ${it.toPath()}, removing it from repo")
                observedPathsRepo.remove(it.toPath())
                it.notifyAlteration("FILE_DLT")
            }
        }

        override fun onDirectoryDelete(directory: File?) {
            directory?.let {
                logger.debug("Deletion of directory spotted: ${it.toPath()}, removing it from repo")
                observedPathsRepo.remove(it.toPath())
                it.notifyAlteration("DIR_DLT")
            }
        }
    }


    private fun Path.assureNotInExcepted(){
        if(this.isExcepted()){
            removeObserverForPaths(setOf(this))

        }
    }

    private fun Path.isExcepted():Boolean{
        return this in exceptedPathsRepo||exceptedPathsRepo.any {this.startsWith(it)}
    }

    private val observedPathsRepoCallBack = object : DataSetCallBack {
        override fun notify(paths: Collection<Path>, actionType: DataSetCallBack.Companion.ActionType) {
            monitorScope.launch {
                logger.debug("Notifiyng about triggering of callback")
                _observedRepoCallBackNotificationFlow.emit(paths to actionType)

            }
            when (actionType) {

                DataSetCallBack.Companion.ActionType.ADDED -> {
                    paths.findTopLevelDirs(_observableDirs)
                        .let { newTops ->

                            //Следующий блок должен проверить не является ли что то из новых Топлевел Диров папой старых диров
                            //#2 И так же проверить не находится ли сам паф или его папа в Ексептез репо используя зараене заготовленный метод
                            newTops.forEach { oneTop ->
                                _observableDirs
                                    .filter { it.startsWith(oneTop) && it != oneTop }
                                    .filter{!it.isExcepted()}
                                    .forEach {
                                        removeObserverForPaths(setOf(it))
//                                        removePathsFromMonitorScope(setOf(it))
                                    }
                            }
                            _observableDirs.addAll(newTops)
                            addObserversForPaths(newTops)
                        }
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


        private val exceptedPathsRepoCallBack=object:DataSetCallBack{
            override fun notify(paths: Collection<Path>, actionType: DataSetCallBack.Companion.ActionType) {
                when(actionType){
                DataSetCallBack.Companion.ActionType.ADDED -> {
                  for(path in observedPathsRepo){
                      path.assureNotInExcepted()

                  }
                }
                else -> {
                    //Don't worry about anything else
                }

            }
            }
            }
        }

