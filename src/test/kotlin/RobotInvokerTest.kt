import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import com.uj.rcbackend.robot.RobotCommandPack
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture


class RobotInvokerTest {

    val mainComponent= DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get(TEST_DIRECTORY))
        .setPort(34444)
        .isSSLEnabled(false)
        .isAuthEnabled(false)
        .dbPassword(TEST_PSWD)
        .buildMainComponent()
    val robotManager=mainComponent.getRobotManager()
    val testCommand= arrayOf("keyPress","20")

    @Test
    fun assertCapsWorks(){

        runBlocking {

            var promiseToFulfill=CompletableFuture<Unit>()



            robotManager.robotActor.send(RobotCommandPack( testCommand,promiseToFulfill))
delay(1000)
       assert(promiseToFulfill.isDone )
            assert(!promiseToFulfill.isCompletedExceptionally)
        }
    }

    @Test
    fun assertHardcoreRunWorks(){

        runBlocking {

            val promiseToFulfill=CompletableFuture<Unit>()


            repeat(100){
                println("Run $it")
                robotManager.robotActor.send(RobotCommandPack(testCommand, promiseToFulfill))
                delay(50)
                assert(promiseToFulfill.isDone )
                assert(!promiseToFulfill.isCompletedExceptionally)

            }
        }
    }



}