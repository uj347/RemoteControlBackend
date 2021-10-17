import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.sink
import okio.source
import org.junit.After
import org.junit.Before
import org.junit.Test
import remotecontrolbackend.command_invoker_part.command_hierarchy.POINTER_MAP_FILE_NAME
import remotecontrolbackend.command_invoker_part.command_hierarchy.REPODIR
import remotecontrolbackend.command_invoker_part.command_hierarchy.SERIALIZED_COMMANDS_DIR
import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.command_invoker_part.command_repo.CommandReference
import remotecontrolbackend.command_invoker_part.command_repo.CommandRepo
import remotecontrolbackend.command_invoker_part.command_repo.createReference
import remotecontrolbackend.command_invoker_part.command_repo.getBufferedSink
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.*
import kotlin.test.assertEquals

class CommandRepoTests {
    lateinit var repo:CommandRepo;
    val testPath=Paths.get("c:\\Ujtrash\\testo")
    val testRepoPath=testPath.resolve(REPODIR)
    val testSerializedCommandsDir=testRepoPath.resolve(SERIALIZED_COMMANDS_DIR)
    val testPointeMapPath=testRepoPath.resolve(POINTER_MAP_FILE_NAME)

    val testSerializedCommandPath=testSerializedCommandsDir.resolve("testMock.json")
    val testMap = mutableMapOf<CommandReference,Path>(MockCommand().createReference() to testSerializedCommandPath)

    val comInvSubcomponent=DaggerMainComponent
        .builder()
        .setWorkDirectory(testPath)
        .setPort(34444)
        .isTestRun(true)
        .buildMainComponent()
        .getComandInvokerSubcompBuilder()
        .build()

    @Before
fun initializeRepo(){
if(!testSerializedCommandsDir.exists()){
    testSerializedCommandsDir.createDirectories()
}
        if(!testSerializedCommandPath.exists()){
testSerializedCommandPath.getBufferedSink().let { comInvSubcomponent.getMoshi().adapter(SerializableCommand::class.java).toJson(it,
    MockCommand()
)
it.flush()}
        }

        testPointeMapPath.getBufferedSink().let { comInvSubcomponent.getPointerMapAdapter().toJson(it,testMap)
        it.flush()}
    repo = comInvSubcomponent
        .getRepo()
        .also{
            runBlocking {

            it.initialize()

            }
}
}


    @Test
    fun pointerMapInitialized(){
     assert(repo.pointerMap!=null)
    }



    @Test
    fun repoInitializationWorks(){

            assert(testRepoPath.exists())
        assert(testSerializedCommandsDir.exists())

    }


    @Test
    fun getCommandWorks(){
         var gotCommand: SerializableCommand?=null
        runBlocking{
            val newCommand= MockCommand()
            val newCommandReference=newCommand.createReference()
            repo.addToRepo(newCommand)
            gotCommand=repo.getCommand(newCommandReference)
            println("Got command from repo: $gotCommand")
            assert(gotCommand!=null)
            assert(gotCommand==newCommand)
        }

    }


    @Test
    fun commandAdapterWorks(){
        val commTestPath=testPath.resolve("testCommand.json")
        if(commTestPath.exists()){
            commTestPath.deleteExisting()
        }
       val commandAdapter= comInvSubcomponent.getMoshi().adapter(SerializableCommand::class.java)
        val testCommand= MockCommand()

        val testSink=commTestPath.sink(StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING).buffer()
        val testSource=commTestPath.source(StandardOpenOption.READ).buffer()
        println("Serialized test command: ${commandAdapter.toJson(testCommand)}")
        commandAdapter.toJson(testSink,testCommand)
        testSink.flush()
        assert(commTestPath.exists())
        assert(commTestPath.fileSize()>0)
        val deserialized=commandAdapter.fromJson(testSource)
        println(deserialized)
        assert(deserialized!=null)
    }


    @Test
    fun addCommandWorks(){
      runBlocking {
          repo.addToRepo(MockCommand())
          repo.addToRepo(MockCommand("duck"))
          assert(repo.pointerMap!!.size >1 )
          assert(testSerializedCommandsDir.listDirectoryEntries().size>1)
      }
    }

    @Test
    fun removeCommandWorks(){
        runBlocking {
            val command= MockCommand()
            val initialSize=repo.pointerMap!!.size
            repo.addToRepo(command)
            assert(repo.pointerMap!!.size>initialSize)
            repo.removeCommand(command.createReference())
            assert(repo.pointerMap!!.size==initialSize)

        }
    }


    @Test
    fun assertReferencesWorkCorrectly(){
        val command1= MockCommand("Mock# 1")
        val command2= MockCommand("Mock# 2")

        //Две разные референсы на одну команду равны
        assert(command1.createReference()==command1.createReference())

        //Референсы на разные команды не равны
        assert(command1.createReference()!=command2.createReference())
        //Референса равна сама себе(не ну а что?, вдруг я настолько тупой)
        val reference=command2.createReference()
        assertEquals(reference,reference)
    }

    @Test
    fun pointerMapDeserealizationWorks(){
        println("Starting deserialization test")
        runBlocking{
            val deserialized=repo.deserializePointerMap()

            assert(deserialized != null)
            assert(deserialized is MutableMap)

        }
    }

    @Test
    fun validateDontCrashes(){
        runBlocking{
          assert(  repo.validateRepo())
        }
    }






@After
fun cleanUp(){

    testRepoPath.dirCleanup()
    }

    fun Path.dirCleanup(){
        Files.newDirectoryStream(this).forEach{
            if(it.isDirectory()){
                it.dirCleanup()
            }else {
                println("Deleting in cleanup: $it")
                it.deleteExisting()
            }
        }

    }



}

