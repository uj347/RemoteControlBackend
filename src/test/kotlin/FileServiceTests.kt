import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import remotecontrolbackend.dagger.FileServiceSubcomponent
import remotecontrolbackend.dagger.PathMonitorFactory

import remotecontrolbackend.file_service_part.path_list_provider_part.HardCodePathListProvider
import remotecontrolbackend.file_service_part.path_list_provider_part.IPathListProvider
import remotecontrolbackend.file_service_part.path_repo_part.IFilePathRepo
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.assertEquals

class FileServiceTests {
    val testDirPath: String = "J:\\Ujtrash\\Test\\testpathrepo"
    val fileServiceSubcomponent: FileServiceSubcomponent = DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get(TEST_DIRECTORY))
        .setPort(34444)
        .isTestRun(false)
        .isSSLEnabled(false)
        .isAuthEnabled(false)
        .setPathProvider(HardCodePathListProvider(listOf(Paths.get(testDirPath))))
        .buildMainComponent()
        .getFileServiceSubcomponentBuilder()
        .build()

    val filePathRepo: IFilePathRepo = fileServiceSubcomponent.getRuntimeFilePathRepoProvider().get()
    val exceptedPathRepo:IFilePathRepo=fileServiceSubcomponent.getRuntimeFilePathRepoProvider().get()
    val pathMonitorFactory: PathMonitorFactory = fileServiceSubcomponent.getPathMonitorFactory()
    val pathMonitor=pathMonitorFactory.createFor(filePathRepo,exceptedPathRepo)
    val dropBoxPath=fileServiceSubcomponent.getDropBoxPath()


    var callbackInterceptorFlow = pathMonitor.repoCallBackNotificationFlow

    @Before
    fun init() {
        filePathRepo.get().forEach{
            filePathRepo.remove(it)
        }
        FileUtils.cleanDirectory(dropBoxPath.toFile())

    }

    @After
    fun conclusion() {
        FileUtils.cleanDirectory(dropBoxPath.toFile())
    }

    @Test
    fun launchAndStopWorks() {
        runBlocking {
            val repoJob = launch(Dispatchers.IO) {
                assertEquals(true,pathMonitor.initialized)
                filePathRepo.add(Paths.get(TEST_DIRECTORY))
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
            pathMonitor.launch()
            val atomicCounter=AtomicInteger(0)
            val flowCollectingJob = launch(Dispatchers.IO) {
                withTimeout(3000) {
                    pathMonitor.repoCallBackNotificationFlow.collect {
                        println("Collected smth: $it")
                        if (atomicCounter.incrementAndGet() == 3) {
                            this.cancel()
                        }
                    }
                }
                }


            val repoJob = launch(Dispatchers.IO) {
                delay(300)
                filePathRepo.add(Paths.get(TEST_DIRECTORY))
                filePathRepo.add(Paths.get(TEST_DIRECTORY+1))
                filePathRepo.add(Paths.get(TEST_DIRECTORY+2))
            }
            flowCollectingJob.join()
            assertEquals(true,flowCollectingJob.isCancelled)
            assertEquals(3,atomicCounter.get())
        }
    }

    @Test
    fun checkRpoMonitorInterOperability(){
        runBlocking{
            pathMonitor.launch()
            assertEquals(true, pathMonitor.initialized)
            assertEquals(0, filePathRepo.get().size)
            assertEquals(0, pathMonitor.observableDirs.size)

            //Проверить точно ли монитор начинает обсервить добавленные в репо директории
            val testDirectory = Paths.get(TEST_DIRECTORY)
            filePathRepo.add(testDirectory)
            assertEquals(1, filePathRepo.get().size)
            delay(300)
            assertEquals(1, pathMonitor.observableDirs.size)

            //Проверить не мониторятся ли субдиры
            val testSubdir = testDirectory.resolve("coco")
            filePathRepo.add(testSubdir)
            assertEquals(2, filePathRepo.get().size)
            assertEquals(1, pathMonitor.observableDirs.size)

            //Проверить перестают ли мониториться диры, удаленные из репо
            filePathRepo.remove(testDirectory)
            assertEquals(0, pathMonitor.observableDirs.size)
        }
    }


    @Test
    fun checkDirModificationTriggersMonitor(){
        runBlocking{
            assertEquals(0, filePathRepo.get().size)
            assertEquals(0, pathMonitor.observableDirs.size)

            val testDirectory = Paths.get(TEST_DIRECTORY)
            val testFile=testDirectory.resolve("testBullshit.txt")
            Files.createDirectories(testDirectory)
            assertEquals(true,testDirectory.exists())

            filePathRepo.add(testDirectory)
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
            filePathRepo.add(topLevel)
            assertEquals(1,filePathRepo.get().size)
            filePathRepo.add(anotherTop)
            assertEquals(2,filePathRepo.get().size)

            assertEquals(2,pathMonitor.observableDirs.size)
            filePathRepo.add(subdir)
            assertEquals(2,pathMonitor.observableDirs.size)
            filePathRepo.add(superLevel)
            assertEquals(1,pathMonitor.observableDirs.size)
        }
    }
@Test
fun fileServiceComplextest(){
    runBlocking {
        val context=fileServiceSubcomponent.getCoroutineContext()
        val fileService=fileServiceSubcomponent.getFileService()
        val localFilePathMnitor=fileService.pathMonitor
        fileService.initializeFileService()
      //  fileService.additionalPaths (IPathListProvider{ listOf(Paths.get("J:\\"))})
        delay(250)
        val getted=fileService.getAllPaths()
        assert(getted.isNotEmpty())
    //    println("Getted from file service: ${getted.toString().substring(0..100)}")
        val fileToCreateName="test2911shit.zip"
       fileService.saveFile(FileInputStream("J:\\Ujtrash\\Starostina_EA"),fileToCreateName)
        delay(1500)
        assertEquals(true,dropBoxPath.resolve(fileToCreateName).exists())
        FileUtils.copyDirectory(File(testDirPath),dropBoxPath.toFile())
        delay(15000)
        delay(2000)
        val testDirInDropBox=dropBoxPath.resolve(Paths.get(testDirPath).fileName)
        assertEquals(true,localFilePathMnitor.launched||pathMonitor.initialized)
//        assertEquals(true,FileUtils.directoryContains(dropBoxPath.toFile(),testDirInDropBox.toFile()))

       // val fileStream=fileService.provideFileStream(testDirInDropBox)
//        assertEquals(true,fileStream.read()!=-1)

awaitCancellation()

    }
}

}

