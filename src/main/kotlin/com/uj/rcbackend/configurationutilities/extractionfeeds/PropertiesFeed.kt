package com.uj.rcbackend.configurationutilities.extractionfeeds

import org.apache.logging.log4j.Logger


interface PropertiesFeed {
    val logger: Logger
    val resultMap:Map<String,Set<String>>
    fun extractRawProperties():Map<String,Set<String>>
}