package com.uj.rcbackend.configurationutilities.extractionfeeds

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

open class ArgsFeed(private val args:Collection<String>):PropertiesFeed {
    companion object{
        private val _logger= LogManager.getLogger()
    }

    override val logger: Logger=_logger

    override val resultMap  by lazy { extractRawProperties() }

    override fun extractRawProperties(): Map<String, Set<String>> {
       val result= mutableMapOf<String, Set<String>>()
        for(arg in args){
            val colonSplitted=arg.split("==")
            val argPrefix=colonSplitted[0]
            val values=colonSplitted[1].split("&&").toSet()
            if(colonSplitted.size!=2){throw IllegalArgFormatException()}
            result.put(argPrefix,values)
        }
        return result
    }
    class IllegalArgFormatException(msg:String="Argument format is [argname]::[argvalue1]&&[argvalue2]&&..."):Exception(msg)

}