import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import remotecontrolbackend.dagger.FileServiceSubcomponent
import remotecontrolbackend.dagger.PathMonitorFactory

import remotecontrolbackend.file_service_part.path_list_provider_part.HardCodePathListProvider
import remotecontrolbackend.file_service_part.path_list_provider_part.IPathListProvider
import remotecontrolbackend.file_service_part.path_repo_part.DataSetCallBack
import remotecontrolbackend.file_service_part.path_repo_part.RuntimeFilePathRepo
import java.io.FileInputStream
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.assertEquals

class FileServiceTests {
    val testDirPathString: String = TEST_DIRECTORY+"testpathrepo"
    val testDirPath=Paths.get(testDirPathString)
    val fileServiceSubcomponent: FileServiceSubcomponent = DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get(TEST_DIRECTORY))
        .setPort(34444)
        .isTestRun(false)
        .isSSLEnabled(false)
        .isAuthEnabled(false)
        .setPathProvider(HardCodePathListProvider(listOf(Paths.get(testDirPathString))))
        .buildMainComponent()
        .getFileServiceSubcomponentBuilder()
        .build()


    val pathMonitorFactory: PathMonitorFactory = fileServiceSubcomponent.getPathMonitorFactory()
    val dropBoxPath=fileServiceSubcomponent.getDropBoxPath()


    lateinit var callbackInterceptorFlow: Flow<Pair<Path, DataSetCallBack.Companion.ActionType>>

    @Before
    fun init() {
        if(!dropBoxPath.exists()){
            dropBoxPath.createDirectories()
        }
        if(!testDirPath.exists()){
            testDirPath.createDirectories()
        }

        FileUtils.cleanDirectory(dropBoxPath.toFile())

    }

    @After
    fun conclusion() {
        kotlin.runCatching {
            FileUtils.cleanDirectory(dropBoxPath.toFile())
        }.onFailure { println("Some shit happened on cleanup because of: ${it.stackTraceToString()}")}
    }

    @Test
    fun launchAndStopWorks() {
        runBlocking {
            val obsRepo=RuntimeFilePathRepo()
            val excRepo=RuntimeFilePathRepo()
            val pathMonitor=pathMonitorFactory.createFor(obsRepo,excRepo)
            val repoJob = launch(Dispatchers.IO) {
                pathMonitor.initialize()
                assertEquals(true,pathMonitor.initialized)
                obsRepo.add(Paths.get(TEST_DIRECTORY))
                pathMonitor.launch()
                assertEquals(true, pathMonitor.launched)
                pathMonitor.stop()
                assertEquals(false, pathMonitor.launched)
                pathMonitor.launch()
                assertEquals(true, pathMonitor.launched)
            }
            repoJob.join()
        }
    }


    @Test
    fun assertCallbackInterceptorFlowWorks() {
        runBlocking {
            val obsRepo=RuntimeFilePathRepo()
            val excRepo=RuntimeFilePathRepo()
            val pathMonitor=pathMonitorFactory.createFor(obsRepo,excRepo)
            pathMonitor.launch()
            val atomicCounter=AtomicInteger(0)
            val flowCollectingJob = launch(Dispatchers.IO) {
                withTimeout(3000) {
                    pathMonitor.observedRepoCallBackNotificationFlow.collect {
                        println("Collected smth: $it")
                        if (atomicCounter.incrementAndGet() == 3) {
                            this.cancel()
                        }
                    }
                }
                }


            val repoJob = launch(Dispatchers.IO) {
                delay(300)
                obsRepo.add(Paths.get(TEST_DIRECTORY))
                obsRepo.add(Paths.get(TEST_DIRECTORY+1))
                obsRepo.add(Paths.get(TEST_DIRECTORY+2))
            }
            flowCollectingJob.join()
            assertEquals(true,flowCollectingJob.isCancelled)
            assertEquals(3,atomicCounter.get())
        }
    }

    @Test
    fun checkRpoMonitorInterOperability(){
        runBlocking{
            val obsRepo=RuntimeFilePathRepo()
            val excRepo=RuntimeFilePathRepo()
            val pathMonitor=pathMonitorFactory.createFor(obsRepo,excRepo)
            pathMonitor.initialize()
            pathMonitor.launch()
            assertEquals(true, pathMonitor.initialized)
            assertEquals(0, obsRepo.get().size)
            assertEquals(0, pathMonitor.observableDirs.size)

            //Проверить точно ли монитор начинает обсервить добавленные в репо директории
            val testDirectory = Paths.get(TEST_DIRECTORY)
            obsRepo.add(testDirectory)
            assertEquals(1, obsRepo.get().size)
            delay(300)
            assertEquals(1, pathMonitor.observableDirs.size)

            //Проверить не мониторятся ли субдиры
            val testSubdir = testDirectory.resolve("coco")
            obsRepo.add(testSubdir)
            assertEquals(2, obsRepo.get().size)
            assertEquals(1, pathMonitor.observableDirs.size)

            //Проверить перестают ли мониториться диры, удаленные из репо
            obsRepo.remove(testDirectory)
            assertEquals(0, pathMonitor.observableDirs.size)
        }
    }


    @Test
    fun checkDirModificationTriggersMonitor(){
        runBlocking{
            val obsRepo=RuntimeFilePathRepo()
            val excRepo=RuntimeFilePathRepo()
            val pathMonitor=pathMonitorFactory.createFor(obsRepo,excRepo)
            assertEquals(0, obsRepo.get().size)
            assertEquals(0, pathMonitor.observableDirs.size)

            val testDirectory = Paths.get(TEST_DIRECTORY)
            val testFile=testDirectory.resolve("testBullshit.txt")
            Files.createDirectories(testDirectory)
            assertEquals(true,testDirectory.exists())

            obsRepo.add(testDirectory)
            pathMonitor.initialize()
            pathMonitor.stop()
            assertEquals(false,pathMonitor.launched)
            pathMonitor.launch()
            assertEquals(true,pathMonitor.launched)

            var triggered=false
            val flowJob=launch(Dispatchers.IO) { withTimeout(10000) {
                pathMonitor.fileAlterationNotificationFlow.collect {
                    triggered = true
                    println("Monitor triggered with $it")
                    cancel()
                }
            }
            }
            delay(1000)
            assertEquals(true,pathMonitor.initialized)
            assert(testDirectory in pathMonitor.observableDirs)
        if (testFile.exists()) {
            Files.delete(testFile)
        }
            Files.createFile(testFile)
            println("TestFileCreated")

            assertEquals(true, testFile.exists())


            flowJob.join()
            assertEquals(true,triggered)

        }
    }

    @Test
    fun checkMonitorRuntimeTopLevelDirExtractionWorksAsExpected(){
        runBlocking {
            val obsRepo=RuntimeFilePathRepo()
            val excRepo=RuntimeFilePathRepo()
            val pathMonitor=pathMonitorFactory.createFor(obsRepo,excRepo)

            val superLevel=Paths.get(TEST_DIRECTORY)
            val topLevel=Paths.get(TEST_DIRECTORY).resolve("top1")
            val anotherTop=Paths.get(TEST_DIRECTORY).resolve("top2")
            val subdir=topLevel.resolve("1")
            val anotherSubdir=subdir.resolve("12")
            val paths= listOf<Path>(superLevel, topLevel,anotherTop,subdir,anotherSubdir)
            for (path in paths){
                path.createDirectories()
            }
            pathMonitor.launch()
            obsRepo.add(topLevel)
            assertEquals(1,obsRepo.get().size)
            obsRepo.add(anotherTop)
            assertEquals(2,obsRepo.get().size)

            assertEquals(2,pathMonitor.observableDirs.size)
            obsRepo.add(subdir)
            assertEquals(2,pathMonitor.observableDirs.size)
            obsRepo.add(superLevel)
            assertEquals(1,pathMonitor.observableDirs.size)
        }
    }
@Test
fun fileServiceComplexTest(){
    runBlocking {
        val thisRunBlockingContext=this.coroutineContext
        val fillerDir=Paths.get(TEST_DIRECTORY).resolve("FillerDir")
        val fillerSubdir=fillerDir.resolve("FillerSubdir")
        if(!fillerSubdir.exists()){
            fillerSubdir.createDirectories()
            FileWriter(fillerSubdir.resolve("Zalupa.txt").toFile()).use {
                it.write("Ne berega ni dna v etoi borode")
            }
        }

        val context=fileServiceSubcomponent.getCoroutineContext()
        val fileService=fileServiceSubcomponent.getFileService()
        val localFilePathMonitor=fileService.pathMonitor
        val alterationFlow=localFilePathMonitor.fileAlterationNotificationFlow
        launch(Dispatchers.IO){
            alterationFlow.collect{
                println("ALTERATIONFLOW: Alteration [${it.second}] spotted with path : ${it.first} ")
            }
        }
        fileService.initializeFileService()
        fileService.additionalPaths (IPathListProvider{ listOf(fillerDir)})
        delay(250)
        val getted=fileService.getAllPaths()
        assert(getted.isNotEmpty())
    //    println("Getted from file service: ${getted.toString().substring(0..100)}")
        val fileToCreateName="test2911shit.zip"
       FileInputStream(TEST_DIRECTORY+"TESTINPUT.TXT").use{
            fileService.saveFile(it,fileToCreateName)
        }

        delay(1500)
        assertEquals(true,dropBoxPath.resolve(fileToCreateName).exists())
        delay(2000)
        val testDirInDropBox=dropBoxPath.resolve(Paths.get(testDirPathString).fileName)
        assertEquals(true,localFilePathMonitor.launched||localFilePathMonitor.initialized)

        println("WE are righ before testInsertion File Creation")
        val testInsertionLiteral="testInsertion.txt"
        val testInsertionContent="TestInputFile"
        val fileToSaveIntoDropBox=Paths.get(TEST_DIRECTORY).resolve(testInsertionLiteral).toFile()
        if(!fileToSaveIntoDropBox.exists()){
            FileWriter(fileToSaveIntoDropBox).use{
                it.write(testInsertionContent)
            }
        }
            fileService.saveFile(fileToSaveIntoDropBox.inputStream(),testInsertionLiteral)
            delay(400)
            assertEquals(true,dropBoxPath.resolve(testInsertionLiteral).exists())
            assertEquals(testInsertionContent, Files.readString(dropBoxPath.resolve(testInsertionLiteral)))
            println("We are ready to quit")
        thisRunBlockingContext.cancelChildren(CancellationException("End of test"))



    }


    }
    @Test
    fun checkFileServiceReinitWorks(){
       runBlocking {
            val localFileService = fileServiceSubcomponent.getFileService()
            println("Nuff done fileservice is inited: ${localFileService.initialized}")
            localFileService.initializeFileService()
           assertEquals(true,localFileService.initialized)
           localFileService.reInitializeFileService()
           delay(1500)
           assertEquals(true,localFileService.initialized)
           assertEquals(true,localFileService.pathMonitor.initialized&&localFileService.pathMonitor.launched)

        }
    }

    @Test
    fun pathMonitorAdditionalTests(){
        runBlocking {

            val context=fileServiceSubcomponent.getCoroutineContext()
            val fileService=fileServiceSubcomponent.getFileService()
            val localFilePathMonitor=fileService.pathMonitor
            val alterationFlow=localFilePathMonitor.fileAlterationNotificationFlow

            val fileAlterationEntries=ConcurrentHashMap.newKeySet<Path>()

            fileService.initializeFileService()

            launch(Dispatchers.IO){
                alterationFlow.collect{
                    println("ALTERATIONFLOW: Alteration [${it.second}] spotted with path : ${it.first} ")
                    fileAlterationEntries.add(it.first)
                }
            }

            val testTempFile=Paths.get(TEST_DIRECTORY).resolve("booba.txt")
            if(testTempFile.exists()){
                FileUtils.delete(testTempFile.toFile())
            }

            fileService.additionalPaths(IPathListProvider{ setOf(Paths.get(TEST_DIRECTORY)) })
            delay(500)
            FileWriter(testTempFile.toFile()).use{
                it.write("Zaaaaaaazaazaza")
            }

            assertEquals(true,fileAlterationEntries.contains(testTempFile))


        }
    }
}



