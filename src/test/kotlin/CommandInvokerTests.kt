import kotlinx.coroutines.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import remotecontrolbackend.command_invoker_part.command_hierarchy.*
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.command_invoker_part.command_repo.createReference
import java.nio.file.Paths
import kotlin.io.path.exists

class CommandInvokerTests {

    val testPath= Paths.get(TEST_DIRECTORY)
    val testRepoPath=testPath.resolve(REPODIR)
    val testSerializedCommandsDir=testRepoPath.resolve(SERIALIZED_COMMANDS_DIR)
    val testPointeMapPath=testRepoPath.resolve(POINTER_MAP_FILE_NAME)
    val testCompiledCommandsDir=testRepoPath.resolve(COMPILED_COMMANDS_DIR)
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
             .isSSLEnabled(false)
             .isAuthEnabled(false)
            .buildMainComponent()
            .getCommandInvoker()

    }

    @After
    fun cleanUp(){
        runBlocking {
        commandInvoker.stopCommandInvoker()


        }
    }

    @Test
    fun commandInvokerRunsCorrectly(){
        runBlocking{

            val invokerJob=commandInvoker.launchCommandInvoker()
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
            val invokerJob=commandInvoker.launchCommandInvoker()
            commandInvoker.postFairCommand(object : Command {
                override suspend fun execute(infoToken: Map<String, Any>) {
                    println("Assigning 1 to testCounter")
                    testCounter=1
                    delay(100)
                    testCounter=2
                    println("Assigning 2 to testCounter")
                    delay(100)
                }
                override var description: String?=testDescription+1
            })
                commandInvoker.postFairCommand(object : Command {
                    override suspend fun execute(infoToken: Map<String, Any>) {
                        println("Assigning 3 to testCounter")
                        testCounter = 3
                    }

                    override var description: String? = testDescription + 2
                })

            delay(65)
            assert(testCounter==1, {
                println("assertion failed with testCounter ==$testCounter")
            })
            delay(120)
            assert(testCounter==2,{
                println("assertion failed with testCounter ==$testCounter")
            })
            delay(100)
            assert(testCounter==3,{
                println("assertion failed with testCounter ==$testCounter")
            })
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
            val invokerJob=commandInvoker.launchCommandInvoker()
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

            val invokerJob=commandInvoker.launchCommandInvoker()

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


    @Test
    fun checkBatCommandWorks (){
       runBlocking {
            val batCommand = BatCommand("cmd /c start \"\" ping vk.com", "TESTBATCOMMAND")

           val compiledFilePath=testCompiledCommandsDir.resolve(batCommand.uniqueFileName)

           val invokerJob =
                commandInvoker.launchCommandInvoker()
                 commandInvoker.putCommand(batCommand)
           println(commandInvoker.workQueue)

           delay(700)


           // Check is compiled file created
           assert(compiledFilePath.exists())

           //check is command added to repo
               //TODO Подумать что тут можно сделать

           assert(commandInvoker.commandRepo.isInitialized)
           val repoPointerMapSize=commandInvoker.commandRepo.pointerMap?.size

           assert(repoPointerMapSize!=null)
           assert(repoPointerMapSize!! >0)
           val batCommandReference=batCommand.createReference()
           assert(commandInvoker.commandRepo.pointerMap!!.contains(batCommandReference))

           assert (true)
           commandInvoker.stopCommandInvoker()
        }

    }


}