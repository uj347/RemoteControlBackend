package com.uj.rcbackend.configurationutilities.extractionfeeds

import org.apache.logging.log4j.LogManager

class TransparentFeed(private val keyValues:Map<String,Set<String>>):PropertiesFeed {

    companion object{
        private val _logger= LogManager.getLogger()
    }

    override val logger=_logger

    override val resultMap: Map<String, Set<String>>
        get() = extractRawProperties()

    override fun extractRawProperties(): Map<String, Set<String>> {
        return keyValues
    }
}