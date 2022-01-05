

import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.mockito.exceptions.base.MockitoAssertionError
import org.mockito.internal.verification.api.VerificationData
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.mockito.verification.VerificationMode
import com.uj.rcbackend.configurationutilities.PropertiesEngine
import com.uj.rcbackend.configurationutilities.PropertiesProcessor
import com.uj.rcbackend.configurationutilities.Property
import com.uj.rcbackend.configurationutilities.extractionfeeds.ArgsFeed
import com.uj.rcbackend.configurationutilities.extractionfeeds.FilePropertiesFeed
import com.uj.rcbackend.configurationutilities.extractionfeeds.PropertiesFeed
import com.uj.rcbackend.configurationutilities.feedgenerators.AbstractFeedGenerator
import com.uj.rcbackend.configurationutilities.feedgenerators.DependentFeedGenerator
import com.uj.rcbackend.configurationutilities.feedgenerators.SimpleFeedGenerator
import java.lang.Exception
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashMap
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class ConfigurationUtilitiesTest {


    @Before
    fun preparations() {
        if (!testDir.exists()) {
            testDir.createDirectories()
        }
    }

    @After
    fun consolidation() {
        if (testDir.exists()) {
            FileUtils.cleanDirectory(testDir.toFile())
        }
    }

    @Test
    fun propertyTest() {
        val property = Property("TestOne", setOf("_test"))
        assert(!property.isConfigured)
        property.propertyValues.add("testValue")
        assert(property.isConfigured)
        assert(!property.generateNotConfiguredClone().isConfigured)

    }

    @Test
    fun feedsTests() {
        createPropFile()
        FilePropertiesFeed(setOf(testDir)).standardCheck()

        ArgsFeed(createPropArgs()).standardCheck()
    }

    @Test
    fun simpleFeedGenTest(){
      createPropFile()

        SimpleFeedGenerator(
            createPropArgs().toMutableSet(),
            mutableSetOf(propsPath),
            readyProps)
            .let{
                it.generateFeeds().forEach {assert(it.resultMap.isNotEmpty())}
            it.generateFeeds().forEach{
                assert(it.checkFeedResultIsConsistent())
            }
        }



    }

    @Test
    fun dependentFeedTest(){
        val spyProperty=Property("mock", setOf("mock")).also { it.propertyValues.add("mockvalue") }
        var mostBeSetByInteractor:Boolean=false
        val spyDep= DependentFeedGenerator.PropertyDependency(spyProperty){ _, _ -> mostBeSetByInteractor = true }
        val dependFeedGen=DependentFeedGenerator(
            createPropArgs().toMutableSet(),
            mutableSetOf(propsPath),
            readyProps,
        mutableSetOf(spyDep)).let {
            it.generateFeeds().forEach {it.checkFeedResultIsConsistent()}
            assert(mostBeSetByInteractor)
        }
    }

    @Test
    fun checkProcessorWorks(){
        val argProp="argprop" to setOf("argval")
        val argPropShort="arg" to setOf("argval")
        val fileProp="fileprop" to setOf("propval")
        val argPropMap= mapOf<String, Set<String>>(argProp)
        val argPropShortMap= mapOf<String, Set<String>>(argPropShort)
        val filePropMap =mapOf<String, Set<String>>(fileProp)
        var argProperty=Property(argProp.first, setOf(argPropShort.first))
        var fileProperty=Property(fileProp.first, setOf("umbaTumba"))

        val testFeedGens= mutableSetOf<AbstractFeedGenerator>()

        val mockArgsFeed:ArgsFeed=mock{
            on(mock.resultMap) doReturn  argPropMap

        }

        val mockArgsShortFeed:ArgsFeed=mock{
            on(mock.resultMap) doReturn argPropShortMap

        }

        val mockFileFeed:FilePropertiesFeed= mock<FilePropertiesFeed> {
            on(mock.resultMap) doReturn filePropMap
        }
        val mockFeedGenWithShortArg:SimpleFeedGenerator= mock<SimpleFeedGenerator> {
            on(mock.generateFeeds()) doReturn setOf<PropertiesFeed>(mockArgsShortFeed,mockFileFeed)
        }.transparentlyPutToCollection(testFeedGens)

        val mockFeedGenWithFullArg:SimpleFeedGenerator= mock<SimpleFeedGenerator> {
            on(mock.generateFeeds()) doReturn setOf<PropertiesFeed>(mockArgsFeed,mockFileFeed)
        }.transparentlyPutToCollection(testFeedGens)


        for(feedGen in testFeedGens) {
            argProperty=argProperty.generateNotConfiguredClone()
            fileProperty=fileProperty.generateNotConfiguredClone()
            val fullChainProcessor = PropertiesProcessor(PropertiesEngine.fullChainEtaps, feedGen)
            fullChainProcessor.processProperties(setOf(argProperty, fileProperty))
            assert(argProperty.isConfigured && fileProperty.isConfigured)

        }
        verify(mockFileFeed, atLeastOnce()).resultMap
        verify(mockArgsFeed, atLeastOnce()).resultMap

        val toDefaultProperty=Property("noMatch", setOf("NoMatch"),
            required = true, defaultValues = setOf("default"))
        val defCheckProcessor=PropertiesProcessor(generator=SimpleFeedGenerator())
        defCheckProcessor.processProperties(setOf(toDefaultProperty))
        assert(toDefaultProperty.propertyValues.containsAll(toDefaultProperty.defaultValues))

        val noDefaultButRequiredProperty=Property("noMatch", setOf("NoMatch"),
            required = true)
        assertThrows<Exception> { defCheckProcessor.processProperties(setOf(noDefaultButRequiredProperty))  }

    }


    @Test
    fun propertiesEngineTest(){
        val pairForFile= "fileProp" to "fileValue"
       Properties().let { it.setProperty(pairForFile.first,pairForFile.second)
       it.store(propsPath.outputStream(),"emptyComment")
       }

        val propArgs=createPropArgs()
        val lookupDep=DependentFeedGenerator.propertiesLookUpPathsDependency
        val lookUpArg=lookupDep.property.argPrefixes.first()+"=="+ propsPath.toString()
        val propsToSet= mutableSetOf<Property>(
            pairForFile.stringPairToProperty(),
            testProp1.stringPairToProperty(),
            testProp2.stringPairToProperty()
        )
        val inputArgs=propArgs+lookUpArg


        val testEngine=PropertiesEngine.Factory(setOf(lookupDep),).newInstance(inputArgs)

        assert(
            testEngine.fire(propsToSet).filter { !it.isConfigured }.isEmpty()
        )
    }

    @Test
    fun checkConvertorsWork(){
        Property("booo",setOf("booo")).let{
            it.propertyValues.add("1")
            assertEquals(1,it.getSingleLong())
            it.propertyValues.clear()
            it.propertyValues.add("true")
            assertEquals(true,it.getSingleBooleanValue())
            it.propertyValues.clear()
            it.propertyValues.add("Zhuzhuzhu")
            assertEquals("Zhuzhuzhu",it.getSingleString())
            it.propertyValues.clear()
            assertThrows<Exception> {it.getSingleLong()  }

        }
    }





    companion object {
        val testDir = Paths.get(TEST_DIRECTORY).resolve("props")
        val propsPath = testDir.resolve("props.properties")
        val testProp1 = "test1" to "test1"
        val testProp2 = "test2" to "test2&&testMulti"

        val props = Properties().also {
            testProp1.let { (one, two) -> it.setProperty(one, two) }
            testProp2.let { (one, two) -> it.setProperty(one, two) }
        }
        val readyProps=HashMap<String,Set<String>>().also{
            it.put(testProp1.first, testProp1.second.split("&&").toSet())
            it.put(testProp2.first, testProp2.second.split("&&").toSet())
        }

        private fun Pair<String, String>.stringPairToProperty():Property{
            return Property(this.first, setOf(this.first))
        }

       private fun createPropArgs():Collection<String>{
            return setOf(testProp1, testProp2).map { it.first + "==" + it.second }
        }

      private   fun createPropFile(){
            props.store(propsPath.outputStream(), "comment")
        }

       private  fun PropertiesFeed.standardCheck() {
            val result = this.resultMap
            assert(result.isNotEmpty())
            assert(result.size == 2)
            assert(result.get(testProp2.first)!!.size == 2)

        }

        private fun PropertiesFeed.checkFeedResultIsConsistent():Boolean{
            return resultMap
                . map <String,Set<String>, Boolean> { (k,v)->
                for(initProp in setOf(testProp1, testProp2)){
                    if(k==initProp.first){
                      return@map v.containsAll(initProp.second.split("&&"))
                    }
                }
                    return@map false
            }.all{ it }
        }


        private fun <T> T.transparentlyPutToCollection(collection: MutableCollection<in T>):T{
          collection.add(this)
            return this
        }

        private fun verifyIfTrue(verificationPredicate: VerificationData.()->Boolean):VerificationMode{
        return FunVerificationMode {
            if(!it.verificationPredicate()){
                throw MockitoAssertionError("Custom error")
            }
        }
        }

        fun interface FunVerificationMode:VerificationMode
    }
}

