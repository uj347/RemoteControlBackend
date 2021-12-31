import org.junit.After
import org.junit.Before
import org.junit.Test
import remotecontrolbackend.configuration_utilities.extraction_feeds.FilePropertiesFeed
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.test.assertEquals

class FeedsTest {
    val testDirPath=Paths.get(TEST_DIRECTORY).resolve("FEED_TEST")
    val propSubdir1=testDirPath.resolve("propSubdir1")
    val propSubdir2=testDirPath.resolve("propSubdir2")
    val propSubdirMulti=testDirPath.resolve("multidir")
    val testDirSet= setOf<Path>(testDirPath,propSubdir1,propSubdir2,propSubdirMulti)

    val propFileName="TestProps.properties"
    val testProps=Properties().also { it.setProperty("testName","testValue") }
    @Before
    fun prepare(){
        for(dir in testDirSet){
            if(!dir.exists())
            dir.createDirectories()
        }
        for(dir in testDirSet){
          val propFile=dir.resolve(propFileName)
          if(dir!=propSubdirMulti){
              produceCustomSingleValueProps(dir.fileName.toString())
                  .store(propFile.outputStream(), "Hmmm...comment!")
              if(dir==propSubdir2){
                  produceCustomSingleValueProps(propSubdir1.fileName.toString())
                      .store(dir.resolve("duplicateOf1prop"+propFileName).outputStream(),"Dupe comment")
              }
          }else{

              produceCustomMultiValueProps("MULTI1").store(propFile.outputStream(),"Multicomment")
          }
        }

    }
    @After
    fun finalize(){}

    @Test
    fun testFilePropFeed(){
        FilePropertiesFeed(setOf(propSubdir1,propSubdir2)).let {
            assert(it.extractRawProperties().size==2)
        }
        FilePropertiesFeed(setOf(propSubdirMulti)).let{
            assert(it.extractRawProperties().values.first().size==2)
        }
        FilePropertiesFeed(setOf(testDirPath)).let{
            assertEquals(4,it.extractRawProperties().size)
        }

    }

    private fun produceCustomSingleValueProps(modifier:String):Properties{
        return Properties().also { it.setProperty("testName$modifier","testValue$modifier") }
    }
    private fun produceCustomMultiValueProps(modifier:String):Properties{
        return Properties().also { it.setProperty("testMultiName$modifier","testValue$modifier&&testmultivalue") }
    }

}