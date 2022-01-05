package com.uj.rcbackend.configurationutilities.feedgenerators

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.configurationutilities.extractionfeeds.PropertiesFeed
import java.nio.file.Path

open class SimpleFeedGenerator(
    argsInput:MutableCollection<String> = mutableSetOf(),
    pathsInput:MutableCollection<Path> = mutableSetOf(),
    keyValuesInput:MutableMap<String,Set<String>> = mutableMapOf()
) :AbstractFeedGenerator(argsInput, pathsInput, keyValuesInput) {


    override val logger: Logger
        get() = _logger

    override fun generateFeeds(): Set<PropertiesFeed> {
      val result:MutableSet<PropertiesFeed> = mutableSetOf(generateArgFeed(argsInput))
        result.add(generateFilePropsFeed(pathsInput))
      result.add(generateTransparentFeed(keyValuesInput))
      return result
  }

    companion object{
        private val _logger=LogManager.getLogger()
    }
}