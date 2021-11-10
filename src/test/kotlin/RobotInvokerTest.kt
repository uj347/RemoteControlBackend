import io.netty.channel.DefaultChannelPromise
import io.netty.util.concurrent.DefaultPromise
import io.netty.util.concurrent.Promise
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import remotecontrolbackend.Utils.CommandPromise
import remotecontrolbackend.dagger.NettySubComponent
import remotecontrolbackend.robot.RobotCommandPack
import java.nio.file.Paths
import kotlin.test.assertEquals

class RobotInvokerTest {

    val mainComponent= DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get("j:\\testo"))
        .setPort(34444)
        .isTestRun(false)
        .isSSLEnabled(false)
        .isAuthEnabled(false)
        .buildMainComponent()
    val robotManager=mainComponent.getRobotManager()
    val testCommand= arrayOf("keyPress","20")

    @Test
    fun assertCapsWorks(){

        runBlocking {

            var promiseToFulfill=CommandPromise()



            robotManager.robotActor.send(RobotCommandPack( testCommand,promiseToFulfill))
delay(1000)
       assertEquals(CommandPromise.State.SUCCESS,promiseToFulfill.state )
        }
    }

    @Test
    fun assertHardcoreRunWorks(){

        runBlocking {

            val promiseToFulfill=CommandPromise()


            repeat(100){
                println("Run $it")
                robotManager.robotActor.send(RobotCommandPack(testCommand, promiseToFulfill))
                delay(50)
                assertEquals(CommandPromise.State.SUCCESS, promiseToFulfill.state)
                promiseToFulfill.recharge()

            }
        }
    }



}