package IntrestingTests.propfuckerty

import com.uj.rcbackend.appProperties
import com.uj.rcbackend.configurationutilities.PropertiesEngine
import com.uj.rcbackend.configurationutilities.Property
import com.uj.rcbackend.propertiesPreprocessorDependencies
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.outputStream

const val TEST_DIRECTORY="C:\\Ujtrash\\TestProps\\"
fun main (args:Array<String>){
    val testP=Paths.get(TEST_DIRECTORY)
    val pathToPropFile=Paths.get(TEST_DIRECTORY).resolve("testy.properties")
    if(!testP.exists()){
        testP.createDirectories()
    }
    val modifiedArgs=args+"plup==$pathToPropFile"
    val prop=Properties().also { it.setProperty("workdir","tumbaumba")
    it.store(pathToPropFile.outputStream(), "No comments!")}
    val localProps= appProperties.map{it.generateNotConfiguredClone()}
    val propertiesEngine= PropertiesEngine.Factory(propertiesPreprocessorDependencies).newInstance(modifiedArgs.asList())
    propertiesEngine.fire(localProps).printPropWithMsg()
}
    
fun Collection<Property>.printPropWithMsg(){
        forEach { println(it.propertyName+" <---setted propery--->  "+ it.propertyValues) }
}


