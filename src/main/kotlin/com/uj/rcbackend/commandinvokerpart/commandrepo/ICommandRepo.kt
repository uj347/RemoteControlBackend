package com.uj.rcbackend.commandinvokerpart.commandrepo

import com.uj.rcbackend.commandinvokerpart.commandhierarchy.CommandReference
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.SerializableCommand

interface ICommandRepo {
    var isInitialized: Boolean

    suspend fun initialize()

    /** Пихнуть сериализованную команду в репо */

    suspend fun addToRepo(command: SerializableCommand): Boolean

    /**Получить Path на комманду из репо */
    suspend fun getCommand(reference: CommandReference): SerializableCommand?

    /**Удалить комманду из репо*/
    suspend fun removeCommand(reference: CommandReference): Boolean

    /** Сделать всю финальную работу если нужно*/
    suspend fun terminalOperation():Boolean

    suspend fun getAllReferences():Collection<CommandReference>
}
