package IntrestingTests.propfuckerty

import remotecontrolbackend.configuration_utilities.configuation_etaps.ArgExtractionEtap
import remotecontrolbackend.configuration_utilities.configuation_etaps.CheckEtap
import remotecontrolbackend.configuration_utilities.configuation_etaps.FileExtractionEtap
import remotecontrolbackend.configuration_utilities.configuation_etaps.ProcessingEtap
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.inputStream

const val TEST_DIRECTORY="j:\\Ujtrash\\Test\\"
fun main1(){
    val dir= Paths.get(TEST_DIRECTORY)
    val firstPropPath=dir.resolve("testprop1.properties")
    val secondPropPath=dir.resolve("testprop2.properties")
    val propbj=Properties()
    propbj.printPropWithMsg("After construction")
    propbj.load(firstPropPath.inputStream())
    propbj.printPropWithMsg("After loading first file")
    propbj.load(secondPropPath.inputStream())
    propbj.printPropWithMsg("After loading second file")
}
fun Properties.printPropWithMsg(stringMsg:String){
    println("Printing properties {$stringMsg}" )
    for(propName in this.stringPropertyNames()){
        println(propName+"< _______  >"+this.getProperty(propName))
    }
}


fun main(){
    val argExtractionEtap:ArgExtractionEtap=ProcessingEtap.provideInstance(ArgExtractionEtap::class).also { println("${it::class.qualifiedName}") }
}