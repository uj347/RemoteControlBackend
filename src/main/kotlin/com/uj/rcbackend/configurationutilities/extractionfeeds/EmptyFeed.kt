package com.uj.rcbackend.configurationutilities.extractionfeeds

import org.apache.logging.log4j.LogManager

open class EmptyFeed:PropertiesFeed {

    companion object{
        private val _logger= LogManager.getLogger()
    }

    override val logger=_logger
    override val resultMap: Map<String, Set<String>>
        get() = extractRawProperties()

    override fun extractRawProperties(): Map<String, Set<String>> {
        return mapOf<String, Set<String>>()
    }
}