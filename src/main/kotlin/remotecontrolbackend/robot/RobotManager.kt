package remotecontrolbackend.robot
//
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

import kotlinx.coroutines.channels.actor
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.RobotManagerModule.Companion.ROBOT_COROUTINE_CONTEXT_LITERAL
import remotecontrolbackend.dagger.RobotManagerSubcomponent
import remotecontrolbackend.dagger.RobotScope
import java.awt.Robot
import java.lang.RuntimeException
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@RobotScope
class RobotManager(robotManagerSubcomponentBuilder: RobotManagerSubcomponent.RobotManagerBuilder) {
    companion object {
        //NB - code "20" is a Caps Lock
        val logger = LogManager.getLogger()
        val robotMethods = Robot::class.java.methods.asList()//
        val objectMethods = JvmType.Object::class.java.methods.asList()
        val methodsOfIntrest: List<Method> =
            (robotMethods - objectMethods).filter { it.parameterTypes.all { it == Int::class.java } }.toCollection(
                mutableListOf()
            )
        val releaseMethods = listOf(
            "keyRelease",
            "mouseRelease"
        )
        val pressMethods = listOf(
            "keyPress",
            "mousePress"
        )

    }
    init{
        robotManagerSubcomponentBuilder.build().inject(this)
    }

    @Named(ROBOT_COROUTINE_CONTEXT_LITERAL)
    @Inject
    lateinit var robotCoroutineContext: CoroutineContext
    private val robotScope = CoroutineScope(robotCoroutineContext)
    private val robot: Robot = Robot()


    val robotActor = reinitializeRobotActor()

    val actorFailed
    get() = robotActor.isClosedForSend

    fun reinitializeRobotActor(): SendChannel<RobotCommandPack> {
        return robotScope.actor<RobotCommandPack>(capacity = 128) {
            for (commandPack in this) {
                val commandAr=commandPack.commandArray
                val promise=commandPack.promise
                robot.invokeRoboCall(commandAr).let {
                    when (it) {
                        true -> {
                            promise?.complete(Unit)
                            logger.debug("roboCommand completed succesfully")
                        }
                        false -> {
                            promise?.completeExceptionally(RuntimeException("roboCommand {${commandAr[0]}} failed"))
                            logger.debug("roboCommand {${commandAr[0]}} failed")
                            logger.error("roboCommand {${commandAr[0]}} failed")
                        }
                    }
                }
            }
        }
    }


   private suspend fun Robot.invokeRoboCall(stringAr: Array<String>): Boolean {

        var releaseRequired:Boolean=false
        var pressListIndex=-1

        return runCatching {
            val methodName = stringAr[0]
            if(methodName in pressMethods){
                releaseRequired=true
                pressListIndex=pressMethods.indexOf(methodName)
            }
            val args = stringAr.sliceArray(1 until stringAr.size).map { it.toInt() }.toTypedArray()
            val argsLength = args.size
            val methodToCall: Method = methodsOfIntrest.first { it.name == methodName }
            if (methodToCall.parameterCount != argsLength) {
                throw RuntimeException("Wrong number of int parameters passed")
            }
            methodToCall.invoke(this, *args)
            if(releaseRequired){
            delay(25)
                methodsOfIntrest.first{it.name ==releaseMethods.get(pressListIndex)}.invoke(this,*args)
            }
            return@runCatching true

        }.getOrElse {
            it.printStackTrace()
            return@getOrElse false }

    }

}

 data class RobotCommandPack(val commandArray:Array<String>, val promise:CompletableFuture<Unit>?) {

}
