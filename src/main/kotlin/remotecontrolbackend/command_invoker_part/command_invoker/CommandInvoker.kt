package remotecontrolbackend.command_invoker_part.command_invoker

import INVOKER_DIR_LITERAL
import kotlinx.coroutines.*
import remotecontrolbackend.command_invoker_part.command_hierarchy.Command
import remotecontrolbackend.command_invoker_part.command_repo.CommandRepo
import remotecontrolbackend.dagger.ComInvScope
import remotecontrolbackend.dagger.CommandInvokerSubcomponent
import java.nio.file.Path
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
//TODO вынести в компонент комманд инвокера

@ComInvScope
class CommandInvoker

constructor(
    @Named(INVOKER_DIR_LITERAL)
    workDirectory: Path,
    commandInvokerBuilder: CommandInvokerSubcomponent.CommandInvokerBuilder
) {
    companion object{
        const val WORK_DIR_LITERAL="workDir"
        const val INVOKER_INSTANCE_LITERAL="invokerInstance"
    }

    init {
        commandInvokerBuilder.build().inject(this)
    }

    val infoToken:Map<String,Any> = mapOf(
        WORK_DIR_LITERAL to workDirectory,
        INVOKER_INSTANCE_LITERAL to this

    )

    val invokerIsRunning: Boolean
        get() {
            return invokerJob?.isActive ?: false
        }

    @Inject
    lateinit var commandRepo: CommandRepo
    var currentCommandJob: Job? = null
    var invokerJob: Job? = null
    var invokerCoroutineScope:CoroutineScope?=null
    var workQueue: BlockingDeque<Command> = LinkedBlockingDeque()


    //TODO
    fun launchCommandInvoker(scope: CoroutineScope):Job? {
        if (!this.invokerIsRunning) {

            invokerJob = scope.launch (Dispatchers.IO){
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
                            }else
                            { command?.execute(infoToken) }
                        }
                    }
                }
            }
            return invokerJob
            }else{
                return invokerJob
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
    //TODO
    suspend fun stopCommandInvoker() {
        withContext(Dispatchers.IO){
            commandRepo.saveChanges()
            invokerJob?.cancel()
        }

}



    //TODO
    /** Добавляет команду в конец очереди*/
    suspend fun postFairCommand(command: Command) {
   withContext(Dispatchers.IO){
       workQueue.putLast(command)
   }
    }

    //TODO
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

    //TODO
    suspend fun clearCommandQueue(){
        withContext(Dispatchers.IO){
            workQueue.clear()
        }
    }


}
