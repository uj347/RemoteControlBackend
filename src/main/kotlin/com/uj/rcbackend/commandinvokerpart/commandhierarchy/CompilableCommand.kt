package com.uj.rcbackend.commandinvokerpart.commandhierarchy

import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists


const val OBLIGATORY_ENTITY_KEY_COMPILABLECOMMAND_UID = "COMPILABLECOMMAND_UID"
const val OBLIGATORY_ENTITY_KEY_COMPILABLECOMMAND_FILE_EXTENTION = "COMPILABLECOMAND_FILE_EXTENSION"

//При запуске - комманда должна проверить не уществует ли в инвокере закэшированной версии бат файла, если есть - запустить его, если нет - создать новый.
abstract class CompilableCommand(entityMap: Map<String, String>) : SerializableCommand(entityMap) {
    init {
        if (!entityMap.containsKey(OBLIGATORY_ENTITY_KEY_COMPILABLECOMMAND_UID)) {
            throw RuntimeException("All compilable commands most contain UID in entityMap")
        }
        if (!entityMap.containsKey(OBLIGATORY_ENTITY_KEY_COMPILABLECOMMAND_FILE_EXTENTION)) {
            throw RuntimeException("All compilable commands most contain File Extension in entityMap")
        }
    }

    val uid: String
        get() {
            return entityMap.get(OBLIGATORY_ENTITY_KEY_COMPILABLECOMMAND_UID)!!
        }
    val fileExtension:String
    get()=entityMap.get(OBLIGATORY_ENTITY_KEY_COMPILABLECOMMAND_FILE_EXTENTION)!!

    override val description: String?
        get() = entityMap.get(OBLIGATORY_ENTITY_KEY_COMMAND_DESCRIPTION)

    val uniqueFileName:String=uid+fileExtension


    protected fun checkIsCompiled(invokerDirectory: Path):Boolean {
val compiledCommandsDir=invokerDirectory
    .resolve(REPODIR)
    .resolve(COMPILED_COMMANDS_DIR)

       if (!compiledCommandsDir.exists()){
           throw RuntimeException("Compiled command dir doesn't exist")
       }
        val expectingCompiledFile=compiledCommandsDir.resolve(uniqueFileName)
       return expectingCompiledFile.exists()

    }

    companion object{
        fun generateUniqueID(string: String): String {
            val originalLength = string.length

            val stringHash = Objects.hashCode(string)
            val processingBuff = StringBuffer().append(originalLength + stringHash * 347).apply {
                while (this.length < 16) {
                    this.append(0)
                }
            }
            return processingBuff.substring(0, 16)
        }
    }

}