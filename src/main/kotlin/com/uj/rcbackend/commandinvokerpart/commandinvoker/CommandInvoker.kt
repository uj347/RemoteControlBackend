package com.uj.rcbackend.commandinvokerpart.commandinvoker


import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.commandinvokerpart.commandhierarchy.*
import com.uj.rcbackend.commandinvokerpart.commandrepo.ICommandRepo
import com.uj.rcbackend.dagger.ComInvScope
import com.uj.rcbackend.dagger.CommandInvokerModule.Companion.COMMAND_INVOKER_COROUTINE_CONTEXT_LITERAL
import com.uj.rcbackend.dagger.CommandInvokerSubcomponent
import java.nio.file.Path
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

//TODO вынести в компонент комманд инвокера

@ComInvScope
class CommandInvoker constructor(
    @Named(INVOKER_DIR_LITERAL)
    workDirectory: Path,
    commandInvokerBuilder: CommandInvokerSubcomponent.CommandInvokerBuilder
) {
    val commandInvokerSubcomponent:CommandInvokerSubcomponent
private val logger:Logger
    @Inject
    lateinit var commandRepo: ICommandRepo
    @Inject
    @Named(COMMAND_INVOKER_COROUTINE_CONTEXT_LITERAL)
    lateinit var invokerCoroutineContext:CoroutineContext
    @Inject
    lateinit var moshi: Moshi

    var currentCommandJob: Job? = null
    var invokerJob: Job? = null


    var workQueue: BlockingDeque<Command> = LinkedBlockingDeque()

    init {
       commandInvokerSubcomponent= commandInvokerBuilder.build().also { it.inject(this) }
    logger=LogManager.getLogger()
    }

    val infoToken:Map<String,Any> = mapOf(
        INVOKER_DIR_LITERAL to workDirectory,
        INVOKER_INSTANCE_LITERAL to this

    )

    val invokerIsRunning: Boolean
        get() {
            return invokerJob?.isActive ?: false
        }





    fun launchCommandInvoker():Job? {
        if (!this.invokerIsRunning) {
val invokerScope= CoroutineScope(invokerCoroutineContext+ SupervisorJob(invokerCoroutineContext.job))
            invokerJob = invokerScope.launch (Dispatchers.IO){
                logger.debug("Inititalizing command invoker")
                //Инициализация репо
                commandRepo.initialize()
                //getting command from deque
                while (true) {
                val command: Command? = getNextCommandFromDeque()
                    yield()
                    supervisorScope {
                        currentCommandJob = launch(
                            CoroutineExceptionHandler { _, exc ->
                                println("Command failed because of : $exc")
                            }) {
                            //Check if invoker still active, if not - put command back
                            if(!invokerIsRunning){
                                command?.let { postNonFairCommand(command) }
                            }else {
                                //Check if command RepoChacheable, if so - addTo REPO
                                command?.let { checkAndRunRepoCaching(command) }

                                command?.execute(infoToken)
                            }
                        }
                    }
                }
            }
            return invokerJob
            }else{
                return invokerJob
            }
        }
/**Проверить есть ли у класса  аннотация "RepoCacheable"и если есть - добавить в репо*/
    private suspend fun checkAndRunRepoCaching(command: Command){
        if(command !is SerializableCommand){
            return
        }else{
            if(command.isCacheable()){
                commandRepo.addToRepo(command)
            }
        }
    }


    private suspend fun getNextCommandFromDeque(): Command?{
        if(invokerJob==null) {
            return null
        }else {

           var result: Command?=null
               withContext(Dispatchers.IO) {
                   var command: Command?
                while (true) {
                    yield()
                    command = workQueue.pollFirst(2, TimeUnit.SECONDS)
                    if(command==null){
                        continue
                    }else if (!invokerIsRunning) {
                        workQueue.putFirst(command)
                       result= null
                        break
                    }else {
                        result=command
                        break
                    }
                }
            }
        return result
        }
    }

    suspend fun stopCommandInvoker() {
        withContext(Dispatchers.IO){
            commandRepo.terminalOperation()
            invokerJob?.cancel()
        }

}




    /** Добавляет команду в конец очереди*/
    suspend fun postFairCommand(command: Command) {
   withContext(Dispatchers.IO){
       workQueue.putLast(command)
   }
    }


    /** Пропихивает команду первой в очередь*/
    suspend fun postNonFairCommand(command: Command) {
        withContext(Dispatchers.IO){
            workQueue.putFirst(command)
        }
    }
    /** Стирает все ккоманды в очереди и вставляет комманду аргумент*/
    suspend fun putCommand(command: Command){
        withContext(Dispatchers.IO){
            clearCommandQueue()
            currentCommandJob?.cancel()
            postFairCommand(command)
        }
    }


    suspend fun clearCommandQueue(){
        withContext(Dispatchers.IO){
            workQueue.clear()
        }
    }


}
