package remotecontrolbackend.command_invoker_part.command_hierarchy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists


//TODO Потестить этот класс
//TODO Прикрутить Кэшабилити
const val OBLIGATORY_ENTITY_KEY_BATCOMMAND_COMMAND_TEXT = "BATCOMMAND_COMMAND_TEXT"
@RepoCacheable
class BatCommand(entityMap: Map<String, String>) : CompilableCommand(entityMap) {
    constructor(batCommandText: String, description: String) : this(
        mapOf(
            OBLIGATORY_ENTITY_KEY_COMMAND_SIMPLE_NAME to BatCommand::class.java.simpleName,
            OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION to description,
            OBLIGATORY_ENTITY_KEY_COMPILABLECOMMAND_FILE_EXTENTION to ".bat",
            OBLIGATORY_ENTITY_KEY_BATCOMMAND_COMMAND_TEXT to batCommandText,
            OBLIGATORY_ENTITY_KEY_COMPILABLECOMMAND_UID to CompilableCommand.generateUniqueID(batCommandText)
        )
    )

    init {
        if (!entityMap.containsKey(OBLIGATORY_ENTITY_KEY_BATCOMMAND_COMMAND_TEXT)) {
            throw RuntimeException("All BAT commands most contain COMMAND_TEXT in entityMap")
        }
    }

    val commandText: String = entityMap.get(OBLIGATORY_ENTITY_KEY_BATCOMMAND_COMMAND_TEXT)!!


    override suspend fun execute(infoToken: Map<String, Any>) {
        withContext(Dispatchers.IO) {

            val invokerDir = infoToken.get(INVOKER_DIR_LITERAL) as Path
            val compiledCommandsDir = invokerDir
                .resolve(REPODIR)
                .resolve(COMPILED_COMMANDS_DIR)


            if (!invokerDir.exists() || !compiledCommandsDir.exists()) {
                throw RuntimeException("There is no necessary directories existing, maybe you forgot to initialize REPO?")
            }


            val isAlreadyCompiled = checkIsCompiled(invokerDir)
            val compiledCommandFile = compiledCommandsDir.resolve(uniqueFileName)

            if (isAlreadyCompiled) {
                Runtime.getRuntime().exec(compiledCommandFile.toString())
            } else {
                Files.writeString(
                    compiledCommandFile,
                    commandText,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
                Runtime.getRuntime().exec(compiledCommandFile.toString())
            }
        }
    }

}