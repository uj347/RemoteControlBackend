import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.uj.rcbackend.dagger.FileServiceSubcomponent
import com.uj.rcbackend.dagger.PathMonitorFactory

import com.uj.rcbackend.fileservicepart.pathlistproviderpart.HardCodePathListProvider
import com.uj.rcbackend.fileservicepart.pathlistproviderpart.IFileServicePathListProvider
import com.uj.rcbackend.fileservicepart.pathrepopart.IFilePathRepo
import java.io.File
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
import kotlin.test.assertFalse
import kotlin.test.assertNotSame

class FileServiceTests {
    val testDirPathString: String = TEST_DIRECTORY + "testpathrepo"
    val testDirPath = Paths.get(testDirPathString)
    val fileServiceSubcomponent: FileServiceSubcomponent = DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get(TEST_DIRECTORY))
        .setPort(34444)
        .isSSLEnabled(false)
        .isAuthEnabled(false)
        .dbPassword(TEST_PSWD)
        .setPathProvider(HardCodePathListProvider(listOf(Paths.get(testDirPathString))))
        .buildMainComponent()
        .getFileServiceSubcomponentBuilder()
        .build()


    val pathMonitorFactory: PathMonitorFactory = fileServiceSubcomponent.getPathMonitorFactory()
    val dropBoxPath = fileServiceSubcomponent.getDropBoxPath()
    val pathRepoProvider=fileServiceSubcomponent.getRuntimeFilePathRepoProvider()
    var obsRepo:IFilePathRepo =pathRepoProvider.get().also{it.initialize()}
    var excRepo:IFilePathRepo= pathRepoProvider.get().also{it.initialize()}
    var pathMonitor = pathMonitorFactory.createFor(obsRepo, excRepo)



    @Before
    fun init() {
        if (!dropBoxPath.exists()) {
            dropBoxPath.createDirectories()
        }
        if (!testDirPath.exists()) {
            testDirPath.createDirectories()
        }

        FileUtils.cleanDirectory(dropBoxPath.toFile())
        obsRepo = pathRepoProvider.get().also{
            println("New Runtime repo initted: $it")
            it.initialize()}
        excRepo= pathRepoProvider.get().also{
            println("New Runtime repo initted: $it")
            it.initialize()}
        pathMonitor = pathMonitorFactory.createFor(obsRepo, excRepo)

    }

    @After
    fun conclusion() {
        kotlin.runCatching {
            FileUtils.cleanDirectory(dropBoxPath.toFile())
            pathMonitor.stop()
            obsRepo.terminate()
            excRepo.terminate()
        }.onFailure { println("Some shit happened on cleanup because of: ${it.stackTraceToString()}") }
    }
@Test
fun checkpathRepoProviderWorksAsExpected(){
    val first=pathRepoProvider.get()
    val second=pathRepoProvider.get()
    assertNotSame(first,second)
}
    @Test
    fun launchAndStopWorks() {
        runBlocking {

            val pathMonitor = pathMonitorFactory.createFor(obsRepo, excRepo)
            val repoJob = launch(Dispatchers.IO) {
                pathMonitor.initialize()
                assertEquals(true, pathMonitor.initialized)
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
        pathMonitor.stop()
    }


    @Test
    fun assertCallbackInterceptorFlowWorks() {
        runBlocking {

            pathMonitor.initialize()
            pathMonitor.launch()
            val atomicCounter = AtomicInteger(0)
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
                delay(450)
                obsRepo.add(Paths.get(TEST_DIRECTORY))
                obsRepo.add(Paths.get(TEST_DIRECTORY + 1))
                obsRepo.add(Paths.get(TEST_DIRECTORY + 2))
            }
            flowCollectingJob.join()
            assertEquals(true, flowCollectingJob.isCancelled)
            assertEquals(3, atomicCounter.get())
        }
        pathMonitor.stop()
    }

    @Test
    fun checkRepoMonitorInteroperability() {
        runBlocking {
            println("path monitor will be created for: ${obsRepo.toString().takeLast(10)}, ${excRepo.toString().takeLast(10)}")
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
        pathMonitor.stop()
    }


    @Test
    fun checkDirModificationTriggersMonitor() {
        runBlocking {
            assertEquals(0, obsRepo.get().size)
            assertEquals(0, pathMonitor.observableDirs.size)

            val testDirectory = Paths.get(TEST_DIRECTORY)
            val testFile = testDirectory.resolve("testBullshit.txt")
            Files.createDirectories(testDirectory)
            assertEquals(true, testDirectory.exists())

            obsRepo.add(testDirectory)
            pathMonitor.initialize()
            pathMonitor.stop()
            assertEquals(false, pathMonitor.launched)
            pathMonitor.launch()
            assertEquals(true, pathMonitor.launched)

            var triggered = false
            val flowJob = launch(Dispatchers.IO) {
                withTimeout(10000) {
                    pathMonitor.fileAlterationNotificationFlow.collect {
                        triggered = true
                        println("Monitor triggered with $it")
                        cancel()
                    }
                }
            }
            delay(1000)
            assertEquals(true, pathMonitor.initialized)
            assert(testDirectory in pathMonitor.observableDirs)
            if (testFile.exists()) {
                Files.delete(testFile)
            }
            Files.createFile(testFile)
            println("TestFileCreated")

            assertEquals(true, testFile.exists())


            flowJob.join()
            assertEquals(true, triggered)

        }
        pathMonitor.stop()
    }

    @Test
    fun checkBasicRepoOperationsWork(){
        var testPath=Paths.get("J:\\UjTrash")
        assert(obsRepo.get().isEmpty())
        assert(obsRepo.add(testPath))
        assert(obsRepo.get().contains(testPath))
        assert(obsRepo.remove(testPath))
        assert(obsRepo.get().isEmpty())

    }

    @Test
    fun checkMonitorRuntimeTopLevelDirExtractionWorksAsExpected() {
        runBlocking {


            val superLevel = Paths.get(TEST_DIRECTORY)
            val topLevel = Paths.get(TEST_DIRECTORY).resolve("top1")
            val anotherTop = Paths.get(TEST_DIRECTORY).resolve("top2")
            val subdir = topLevel.resolve("1")
            val anotherSubdir = subdir.resolve("12")
            val paths = listOf<Path>(superLevel, topLevel, anotherTop, subdir, anotherSubdir)
            for (path in paths) {
                path.createDirectories()
            }
            pathMonitor.launch()
            obsRepo.add(topLevel)
            assertEquals(1, obsRepo.get().size)
            obsRepo.add(anotherTop)
            assertEquals(2, obsRepo.get().size)

            assertEquals(2, pathMonitor.observableDirs.size)
            obsRepo.add(subdir)
            assertEquals(2, pathMonitor.observableDirs.size)
            obsRepo.add(superLevel)
            assertEquals(1, pathMonitor.observableDirs.size)
        }
        pathMonitor.stop()
    }

    @Test
    fun fileServiceComplexTest() {
        runBlocking {
            val thisRunBlockingContext = this.coroutineContext
            val fillerDir = Paths.get(TEST_DIRECTORY).resolve("FillerDir")
            val fillerSubdir = fillerDir.resolve("FillerSubdir")
            if (!fillerSubdir.exists()) {
                fillerSubdir.createDirectories()
                FileWriter(fillerSubdir.resolve("Zalupa.txt").toFile()).use {
                    it.write("Ne berega ni dna v etoi borode")
                }
            }

            val context = fileServiceSubcomponent.getCoroutineContext()
            val fileService = fileServiceSubcomponent.getFileService().also { it.reInitializeFileService() }
            val localFilePathMonitor = fileService.pathMonitor
            val alterationFlow = localFilePathMonitor.fileAlterationNotificationFlow
            launch(Dispatchers.IO) {
                alterationFlow.collect {
                    println("ALTERATIONFLOW: Alteration [${it.second}] spotted with path : ${it.first} ")
                }
            }

            fileService.additionalPaths(IFileServicePathListProvider { listOf(fillerDir) })
            delay(250)
            val getted = fileService.getAllPaths()
            assert(getted.isNotEmpty())
            val testInputTxtFile = File(TEST_DIRECTORY + "TESTINPUT.TXT")
            FileWriter(testInputTxtFile).use {
                it.write("Simple testInput")
            }
            //    println("Getted from file service: ${getted.toString().substring(0..100)}")
            val fileToCreateName = "test2911shit.zip"
            FileInputStream(testInputTxtFile).use {
                fileService.saveFile(it, fileToCreateName)
            }

            delay(1500)
            assertEquals(true, dropBoxPath.resolve(fileToCreateName).exists())
            delay(2000)
            val testDirInDropBox = dropBoxPath.resolve(Paths.get(testDirPathString).fileName)
            assertEquals(true, localFilePathMonitor.launched || localFilePathMonitor.initialized)

            println("WE are righ before testInsertion File Creation")
            val testInsertionLiteral = "testInsertion.txt"
            val testInsertionContent = "TestInputFile"
            val fileToSaveIntoDropBox = Paths.get(TEST_DIRECTORY).resolve(testInsertionLiteral).toFile()
            if (!fileToSaveIntoDropBox.exists()) {
                FileWriter(fileToSaveIntoDropBox).use {
                    it.write(testInsertionContent)
                }
            }
            fileService.saveFile(fileToSaveIntoDropBox.inputStream(), testInsertionLiteral)
            delay(400)
            assertEquals(true, dropBoxPath.resolve(testInsertionLiteral).exists())
            assertEquals(testInsertionContent, Files.readString(dropBoxPath.resolve(testInsertionLiteral)))
            println("We are ready to quit")
            thisRunBlockingContext.cancelChildren(CancellationException("End of test"))


        }
        pathMonitor.stop()


    }

    @Test
    fun checkFileServiceReinitWorks() {
        runBlocking {
            val localFileService = fileServiceSubcomponent.getFileService().also { it.reInitializeFileService() }
            println("Nuff done fileservice is inited: ${localFileService.initialized}")
            localFileService.initializeFileService()
            assertEquals(true, localFileService.initialized)
            localFileService.reInitializeFileService()
            delay(1500)
            assertEquals(true, localFileService.initialized)
            assertEquals(true, localFileService.pathMonitor.initialized && localFileService.pathMonitor.launched)

        }
        pathMonitor.stop()
    }

    //TODO Прочекать работает ли получение Стрима для новой версии зиппера, который принимает Коллекцию файлов чрез файлСервис
    @Test
    fun pathMonitorAdditionalTests() {
        runBlocking (){

                val context = fileServiceSubcomponent.getCoroutineContext()
                val fileService = fileServiceSubcomponent.getFileService().also { it.reInitializeFileService() }

                val localFilePathMonitor = fileService.pathMonitor
                val alterationFlow = localFilePathMonitor.fileAlterationNotificationFlow

                val fileAlterationEntries = ConcurrentHashMap.newKeySet<Path>()

                delay(700)

                val altFlowJob = launch(Dispatchers.IO) {
                    println("In altFlow jobLaunch")
                    alterationFlow.collect {
                        println("ALTERATIONFLOW: Alteration [${it.second}] spotted with path : ${it.first} ")
                        fileAlterationEntries.add(it.first)
                        yield()
                    }
                }

                assertEquals(true, altFlowJob.isActive)
                val testTempFile1 = Paths.get(TEST_DIRECTORY).resolve("booba1.txt")
                val testTempFile2 = Paths.get(TEST_DIRECTORY).resolve("booba2.txt")
                val testDir1 = Paths.get(TEST_DIRECTORY + "BOOBADIR\\")
                val testInDirFile = testDir1.resolve("inDirBooba.txt")
                if (testTempFile1.exists()) {
                    FileUtils.delete(testTempFile1.toFile())
                }
                if (testTempFile2.exists()) {
                    FileUtils.delete(testTempFile2.toFile())
                }
                if (testDir1.exists()) {
                    FileUtils.deleteDirectory(testDir1.toFile())
                }
                assertFalse(testDir1.exists())
                assertFalse(testTempFile1.exists() || testTempFile2.exists())

                fileService.additionalPaths(IFileServicePathListProvider { setOf(Paths.get(TEST_DIRECTORY)) })
                delay(500)
                val writerJob = launch(Dispatchers.IO) {
                    FileWriter(testTempFile1.toFile()).use {
                        it.write("Zaaaaaaazaazaza")
                    }

                    FileWriter(testTempFile2.toFile()).use {
                        it.write("Zaaaaaaazaazaza")
                    }
                    testDir1.createDirectories()
                    FileWriter(testInDirFile.toFile()).use {
                        it.write("Zaaaaaaazaazaza")
                    }
                }
                writerJob.join()

                delay(2500)
                println("testTempFile is: $testTempFile1, fileAlterationEntries is :$fileAlterationEntries")
                assertEquals(true, fileAlterationEntries.contains(testTempFile1))





                altFlowJob.cancel()

                val testOutPutFileFor2Files = File(TEST_DIRECTORY + "testOut2Files.zip")
                val testOutForDirAndFile = File(TEST_DIRECTORY + "testOutDirAndFile.zip")
                val testOutForOnlyDir = File(TEST_DIRECTORY + "onlyDir.zip")
                val testOutOnly1File = File(TEST_DIRECTORY + "1File.txt")

                val outputsSet = setOf(
                    testOutPutFileFor2Files,
                    testOutForDirAndFile,
                    testOutForOnlyDir,
                    testOutOnly1File
                ).also {
                    it.forEach {
                        if (it.exists()) {
                            FileUtils.delete(it)
                        }
                    }
                }

                assert(testTempFile1.exists() && testTempFile2.exists())
                assertFalse(testOutPutFileFor2Files.exists())
                val testOutJob = launch {
                    fileService.provideFileStream(setOf(testTempFile1, testTempFile2)).use { testServiceInput ->
                        IOUtils.copy(testServiceInput, testOutPutFileFor2Files.outputStream())
                    }
                    fileService.provideFileStream(setOf(testDir1, testTempFile1)).use { testServiceInput ->
                        IOUtils.copy(testServiceInput, testOutForDirAndFile.outputStream())
                    }
                    fileService.provideFileStream(setOf(testDir1)).use { testServiceInput ->
                        IOUtils.copy(testServiceInput, testOutForOnlyDir.outputStream())
                    }
                    fileService.provideFileStream(setOf(testTempFile1)).use { testServiceInput ->
                        IOUtils.copy(testServiceInput, testOutOnly1File.outputStream())
                    }

                }
                testOutJob.join()

                outputsSet.forEach {
                    assert(it.exists())
                    assert(FileUtils.sizeOf(it) > 0)
                }


        }
        pathMonitor.stop()
    }

    @Test
     fun stringFuckery(){
         val testStr1="/files/Download"
        val testStr2="/files/Download/"
        assertEquals("files", testStr1.substring(1).substringBefore("/"))
        assertEquals("files", testStr2.substring(1).substringBefore("/"))
     }
}



