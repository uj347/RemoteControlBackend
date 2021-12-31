import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommand
import remotecontrolbackend.command_invoker_part.command_repo.createReference
import java.nio.file.Paths
import kotlin.test.assertContains
import kotlin.test.assertNotNull

class CommandRepoTests {

    val commandInvoker=DaggerMainComponent
    .builder()
    .setWorkDirectory(Paths.get(TEST_DIRECTORY))
    .setPort(34444)
    .isSSLEnabled(false)
    .dbPassword(TEST_PSWD)
    .isAuthEnabled(false)
    .buildMainComponent()
    .getCommandInvoker()
    val repo=commandInvoker.commandRepo
    val moshi=commandInvoker.moshi

    val persistentCommandPair=Pair(MockCommand("Persistent"),MockCommand("Persistent").createReference())

    @Before
    fun init(){
        commandInvoker.launchCommandInvoker()
        runBlocking {
            repo.addToRepo(persistentCommandPair.first)
        }
    }

    @Test
    fun addingToRepoWorks(){
       runBlocking {
           val zuzuCommand=MockCommand("Zuzu")
           repo.addToRepo(zuzuCommand)
           val gotFromRepo=repo.getCommand(zuzuCommand.createReference())
           assertNotNull(gotFromRepo)
       }
    }

    @Test
    fun checkGetAllRefsWork(){
        runBlocking {
            assertNotNull(repo.getAllReferences())
            assertContains(repo.getAllReferences(),persistentCommandPair.second)
        }
    }

    @Test
    fun checkDeleteCommandWorks(){
        runBlocking {
            assertContains(repo.getAllReferences(),persistentCommandPair.second)
            repo.removeCommand(persistentCommandPair.second)
            assert(!repo.getAllReferences().contains(persistentCommandPair.second))
        }
    }


}