package remotecontrolbackend.command_invoker_part.command_repo

import INVOKER_DIR_LITERAL
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import remotecontrolbackend.command_invoker_part.COMMAND_REPO_LOGGER_NAME_LITERAL
import remotecontrolbackend.command_invoker_part.command_hierarchy.Command
import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.SERIALIZED_COMMANDS_DIR
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.dagger.ComInvScope
import remotecontrolbackend.dagger.CommandInvokerSubcomponent
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.*
import kotlin.io.use
import remotecontrolbackend.command_invoker_part.command_hierarchy.*

//todo Вынести в субкомпонент команд инвокера
@ComInvScope
class CommandRepo
@Inject
constructor(
    @Named(INVOKER_DIR_LITERAL)
    val workPath: Path,
    val commandInvokerSubcomponent: CommandInvokerSubcomponent
) {

companion object {
    val logger=LogManager.getLogger()
}

    @Inject
    lateinit var moshi: Moshi

    //Json запарсенный файл с пойнтерами (Map <Reference, Path>)
    //Из Path - вылавливаем команду и возвращаем ее инвокеру
    private var _pointerMap: MutableMap<CommandReference, Path>? = null
    val pointerMap: Map<CommandReference, Path>?
        get() = _pointerMap
    val repoDirectory: Path = workPath.resolve("command_repo")
    val serializedCommandsDir = repoDirectory.resolve(SERIALIZED_COMMANDS_DIR)
    val compiledCommandsDir = repoDirectory.resolve(COMPILED_COMMANDS_DIR)
    val pointerMapPath = repoDirectory.resolve(POINTER_MAP_FILE_NAME)
    var isInitialized: Boolean = false

    //Чек лист
    private var checkIsRepoDirectoryCreated = false
    private var checkIsCommandsDirectoryCreated = false
    private var checkIsCompiledCommandsDirCreated = false
    private var checkIsPointerMapFileExists = false
    private var checkIsPointerMapFileValid = false



    suspend fun initialize() {
        logger.debug("Command repo initialization started")
        if (isInitialized) {
            return
        }
        runCheckList()
        //Идем по иерархии чек листа
        withContext(Dispatchers.IO) {
            when (checkIsRepoDirectoryCreated) {
                false -> {
                    compiledCommandsDir.createDirectories()
                    serializedCommandsDir.createDirectories()
                }
                true -> {
                    if (!checkIsCommandsDirectoryCreated) {
                        serializedCommandsDir.createDirectories()
                    }
                    if (!checkIsCompiledCommandsDirCreated) {
                        compiledCommandsDir.createDirectories()
                    }

                    if (checkIsPointerMapFileExists) {
                        when (checkIsPointerMapFileValid) {
                            true -> {
                                val pointerMapPathSource = pointerMapPath.getBufferedSource()
                                _pointerMap = deserializePointerMap()
                            }
                            false -> {
                                _pointerMap = HashMap<CommandReference, Path>().toMutableMap()
                            }
                        }
                    } else {
                        _pointerMap = HashMap<CommandReference, Path>().toMutableMap()
                    }
                }
            }
        }
            validateRepo()
            isInitialized = true

    }


    /** Добавить команду в рантайм пойнтермапу и пихнуть сериализованную команду в папку Коммандс*/

    suspend fun addToRepo(command: SerializableCommand): Boolean {
        if (!isInitialized) {
            return false
        }
        val newCommandPath = serializedCommandsDir.resolve(generateCommandFileName(command))
        val reference = command.createReference()
        kotlin.runCatching {

            val commAdapter = moshi.adapter(SerializableCommand::class.java)
            withContext(Dispatchers.IO) {
                val commandSink = newCommandPath.getBufferedSink().writeAndFlushJson(commAdapter, command)
            }
            val deleteOldFile: Boolean = _pointerMap!!.containsKey(reference)
            var oldPath: Path? = null
            if (deleteOldFile) {
                oldPath = _pointerMap!!.get(reference)
            }
            _pointerMap?.put(reference, newCommandPath) ?: return false
            oldPath?.deleteExisting()
            return true
        }
            .onFailure {
                println("Exception occurred when adding new command: $it")
                return false
            }


        return false
    }


    /**Получить Path на комманду из рантайм пойнтермапы и десериализовать команду */
    suspend fun getCommand(reference: CommandReference): SerializableCommand? {
        if (!isInitialized) {
            return null
        }
        val commPath = _pointerMap?.get(reference) ?: return null
        val result: SerializableCommand? = withContext<SerializableCommand?>(Dispatchers.IO) {
            kotlin.runCatching {
                commPath.getBufferedSource().use {
                    commandInvokerSubcomponent.getMoshi().adapter(SerializableCommand::class.java).fromJson(it)
                }
            }.getOrElse {
                println("Exception occurred in getting Command: $it")
                return@getOrElse null
            }
        }
        return result
    }


    /**Удалить комманду из рантайм-Пойнтермапы, валидейт сделает все остальное в свое время*/
    suspend fun removeCommand(reference: CommandReference): Boolean {
        if (_pointerMap?.contains(reference) ?: return false) {
            _pointerMap?.remove(reference) ?: return false
            return true
        }
        return false
    }


    /** ЮТИЛИТИ Сериализовать содержимое рантайм ПоинтерМапы в Джейсон, полностью переписав ее*/
    suspend fun saveChanges(): Boolean {
        if (!isInitialized) {
            return false
        }
        return withContext(Dispatchers.IO) {
            val mapAdapter = commandInvokerSubcomponent.getPointerMapAdapter()
            kotlin.runCatching {
                val mapSink = pointerMapPath.getBufferedSink()
                mapSink.writeAndFlushJson(mapAdapter, _pointerMap)
                return@runCatching true
            }.getOrElse {
                println("Exception occurred in saving RepoChanges: $it")
                return@getOrElse false
            }
        }
    }


    /** ЮТИЛИТИ  Сравнить содержимое пойнтер-мэп и списка комманд, если есть команды не указанные в поинтермапе- удалить их */
    suspend fun validateRepo(): Boolean {
        logger.debug("Starting ValidateRepo......")
        if (!isInitialized) {
            return false
        }
        val pathsFromPointerMap = _pointerMap?.values ?: return false
        logger.debug("In validateRepo(): pointerMapContains paths:$pathsFromPointerMap ")

        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                Files.newDirectoryStream(serializedCommandsDir)
                    .forEach {
                        logger.debug("In validateRepo(): perfoming cheeck of serialized command file: $it")
                        if (!pathsFromPointerMap.contains(it)) {
                            it.deleteExisting()
                        }
                    }
                return@runCatching true


            }.getOrElse {
                println("Error Occurred in validation process: $it")
                return@getOrElse false
            }
        }

    }


    /** ЮТИЛИТИ Сгенерить какое нибуудь уникальное имя для комманды */
    suspend fun generateCommandFileName(command: SerializableCommand): String {
        val sb: StringBuffer = StringBuffer()
        return LocalDateTime.now().let {
            (it.format(DateTimeFormatter.ISO_LOCAL_DATE) +
                    "_" +
                    it.format(DateTimeFormatter.ISO_TIME) +
                    "_" +
                    command::class.simpleName +
                    ".json").replace(":", "!")
        }

    }


    /**ЮТИЛИТИ выгрузить из корневой папки фал и десериализовать его, кинуть Null если его там нет*/
    suspend fun deserializePointerMap(): MutableMap<CommandReference, Path>? {

        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                pointerMapPath.getBufferedSource().let {
                    commandInvokerSubcomponent.getPointerMapAdapter().fromJson(it)
                }
            }.getOrElse {
                println("Exception occured in deserialization of pointerMap $it")
                return@getOrElse null
            }
        } as MutableMap<CommandReference, Path>?
    }


    /** ЮТИЛ Пройтись по чек листу и выставить все флаги в соответсвии*/
    private suspend fun runCheckList() {
        checkIsRepoDirectoryCreated = repoDirectory.exists()
        checkIsCommandsDirectoryCreated = serializedCommandsDir.exists()
        checkIsCompiledCommandsDirCreated = compiledCommandsDir.exists()
        //Проверить сущестует ли файл пойнтермапы
        checkIsPointerMapFileExists = pointerMapPath.exists().also {
            //Пропихнуть этот булл сюда и на его основе решить стоит ли попытаться десериализовать мапу для проверки ее валидности
            if (it) {
                withContext(Dispatchers.IO) {
                    kotlin.runCatching {
                        val pointerMapPathSource = pointerMapPath.getBufferedSource()
                        commandInvokerSubcomponent.getPointerMapAdapter().fromJson(pointerMapPathSource)
                        checkIsPointerMapFileValid = true
                    }.onFailure { checkIsPointerMapFileValid = false }
                }

            } else {
                checkIsPointerMapFileValid = false
            }
        }
//         checkIsRepoDirectoryCreated = false
//    private var checkCommandsDirectoryCreated = false
//    private var checkIsPointerMapFileExists = false
//    private var checkIsPointerMapFileValid = false
        println(
            "checklist:\n" +
                    "checkIsRepoDirectoryCreated: $checkIsRepoDirectoryCreated\n" +
                    "checkCommandsDirectoryCreated: $checkIsCommandsDirectoryCreated\n" +
                    "checkIsPointerMapFileExists: $checkIsPointerMapFileExists\n" +
                    "checkIsPointerMapFileValid: $checkIsPointerMapFileValid"
        )
    }


    /**ЮТИЛ моково инициализирует  репо */
    suspend fun mockInitialize() {
        if (!isInitialized) {

            val mockCommandPath = serializedCommandsDir.resolve("mock_c.json")

            val testRef = CommandReference("cocoMand", "testRef")
            //TODO Mock realization
            repoDirectory.let {
                if (!it.exists()) {
                    it.createDirectory()
                }
            }
            serializedCommandsDir.let {
                if (!it.exists()) {
                    it.createDirectory()
                }


                val testPointMap = mutableMapOf<CommandReference, Path>(
                    testRef to mockCommandPath
                )
                mockCommandPath.sink(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).buffer().let {
                    moshi.adapter(Command::class.java).toJson(it, MockCommand())
                    it.flush()
                }
                val testSink =
                    pointerMapPath.sink(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).buffer()
                commandInvokerSubcomponent.getPointerMapAdapter().toJson(testSink, testPointMap)
                testSink.flush()
                _pointerMap =
                    commandInvokerSubcomponent.getPointerMapAdapter().fromJson(pointerMapPath.source().buffer())
                        ?.toMutableMap()
            }

            isInitialized = true
        }

    }


}