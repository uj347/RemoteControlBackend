import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import remotecontrolbackend.command_invoker_part.command_hierarchy.Command
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.command_invoker_part.command_repo.CommandRepo
import java.nio.file.Paths

class CommandInvokerTests {

    val testPath= Paths.get("j:\\testo")
    val testRepoPath=testPath.resolve(CommandRepo.REPODIR)
    val testSerializedCommandsDir=testRepoPath.resolve(CommandRepo.SERIALIZED_COMMANDS_DIR)
    val testPointeMapPath=testRepoPath.resolve(CommandRepo.POINTER_MAP_FILE_NAME)
    val testDescription="TESTDESCRIPTION"

    var testCounter=0;
    lateinit var commandInvoker: CommandInvoker

    @Before
    fun testPreparations(){

         commandInvoker=DaggerMainComponent
            .builder()
            .setWorkDirectory(testPath)
            .setPort(34444)
            .isTestRun(true)
            .buildMainComponent()
            .getCommandInvoker()

    }

    @Test
    fun commandInvokerRunsCorrectly(){
        runBlocking{

            val invokerJob=commandInvoker.launchCommandInvoker(CoroutineScope(coroutineContext+Dispatchers.IO))
            assert(invokerJob!!.isActive)
            assert(commandInvoker.invokerIsRunning)
            delay(3300)
            assert(invokerJob!!.isActive)
            assert(commandInvoker.invokerIsRunning)
            commandInvoker.stopCommandInvoker()
            assert(!commandInvoker.invokerIsRunning)
        }
    }

    @Test
    fun checkCommandsExecutedAsExpected(){
        runBlocking{
            val invokerJob=commandInvoker.launchCommandInvoker(CoroutineScope(coroutineContext+Dispatchers.IO))
            commandInvoker.postFairCommand(object : Command {
                override suspend fun execute(infoToken: Map<String, Any>) {
                    testCounter=1
                    delay(100)
                    testCounter=2
                    delay(75)
                }
                override var description: String?=testDescription+1
            })
                commandInvoker.postFairCommand(object : Command {
                    override suspend fun execute(infoToken: Map<String, Any>) {
                        testCounter = 3
                    }

                    override var description: String? = testDescription + 2
                })

            delay(5)
            assert(testCounter==1)
            delay(120)
            assert(testCounter==2)
            delay(55)
            assert(testCounter==3)
            commandInvoker.stopCommandInvoker()
//TODO Доделать тесты, проверить работает ли пут корректно, работает ли очередь комманд корректно и прочая..
            invokerJob!!.join()
            assert(!commandInvoker.invokerIsRunning)
        }
    }
//TODO
    @Test
    fun checkPutExecutedAsExpected(){
        runBlocking{
            val invokerJob=commandInvoker.launchCommandInvoker(CoroutineScope(coroutineContext+Dispatchers.IO))
            commandInvoker.postFairCommand(object : Command {
                override suspend fun execute(infoToken: Map<String, Any>) {
                    testCounter=1
                    delay(100000)

                }

                override var description: String?=testDescription+1
            })
            delay(100)
            assert(testCounter==1)
            commandInvoker.putCommand(object : Command {
                override suspend fun execute(infoToken: Map<String, Any>) {
                    testCounter=2
                    delay(1000)
                }
                override var description: String?=testDescription+1
            })
            delay(15)
            assert(testCounter==2)

            commandInvoker.stopCommandInvoker()


        }
    }

    @Test
    fun queueCorrectneessTests(){
        runBlocking{


            commandInvoker.postFairCommand(object : Command {
                override suspend fun execute(infoToken: Map<String, Any>) {
                    testCounter=1
                    delay(50)
                }
                override var description: String?=testDescription+1
            })

            commandInvoker.postFairCommand(object : Command {
                override suspend fun execute(infoToken: Map<String, Any>) {
                    testCounter=2
                    delay(50)
                }
                override var description: String?=testDescription+2
            })

            commandInvoker.postNonFairCommand(object : Command {
                override suspend fun execute(infoToken: Map<String, Any>) {
                    testCounter=3
                    delay(50)
                }
                override var description: String?=testDescription+3
            })

            val invokerJob=commandInvoker.launchCommandInvoker(CoroutineScope(coroutineContext+Dispatchers.IO))

            delay(10)
            assert(testCounter==3)
            delay(50)
            assert(testCounter==1)
            delay(50)
            assert(testCounter==2)


            commandInvoker.stopCommandInvoker()
            invokerJob?.join()


        }
    }


}