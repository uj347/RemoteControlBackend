package remotecontrolbackend.configuration_utilities.feed_generators

import remotecontrolbackend.configuration_utilities.extraction_feeds.ArgsFeed
import remotecontrolbackend.configuration_utilities.extraction_feeds.FilePropertiesFeed
import remotecontrolbackend.configuration_utilities.extraction_feeds.PropertiesFeed
import remotecontrolbackend.configuration_utilities.extraction_feeds.TransparentFeed
import java.nio.file.Path

abstract class AbstractFeedGenerator(
    protected val argsInput:MutableCollection<String> = mutableSetOf(),
    protected val pathsInput:MutableCollection<Path> = mutableSetOf(),
    protected val keyValuesInput:MutableMap<String,Set<String>> = mutableMapOf()
) {
     abstract fun generateFeeds():Set<PropertiesFeed>
     protected fun generateArgFeed(args:Collection<String>):ArgsFeed= ArgsFeed(args.toTypedArray())
    protected fun generateFilePropsFeed(inputPaths:Collection<Path>):FilePropertiesFeed=FilePropertiesFeed(inputPaths.toSet())
    protected fun generateTransparentFeed(keyValuesInput:Map<String,Set<String>>):TransparentFeed=TransparentFeed(keyValuesInput)
}