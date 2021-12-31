package remotecontrolbackend.configuration_utilities.feed_generators

import remotecontrolbackend.configuration_utilities.extraction_feeds.PropertiesFeed
import java.nio.file.Path

open class SimpleFeedGenerator(
    argsInput:MutableCollection<String> = mutableSetOf(),
    pathsInput:MutableCollection<Path> = mutableSetOf(),
    keyValuesInput:MutableMap<String,Set<String>> = mutableMapOf()
) :AbstractFeedGenerator(argsInput, pathsInput, keyValuesInput) {

 override fun generateFeeds(): Set<PropertiesFeed> {
      val result:MutableSet<PropertiesFeed> = mutableSetOf(generateArgFeed(argsInput))
        result.add(generateFilePropsFeed(pathsInput))
      result.add(generateTransparentFeed(keyValuesInput))
      return result
  }
}