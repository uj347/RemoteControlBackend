package com.uj.rcbackend.configurationutilities.feedgenerators

import org.apache.logging.log4j.Logger
import com.uj.rcbackend.configurationutilities.extractionfeeds.ArgsFeed
import com.uj.rcbackend.configurationutilities.extractionfeeds.FilePropertiesFeed
import com.uj.rcbackend.configurationutilities.extractionfeeds.PropertiesFeed
import com.uj.rcbackend.configurationutilities.extractionfeeds.TransparentFeed
import java.nio.file.Path

abstract class AbstractFeedGenerator(
    protected val argsInput:MutableCollection<String> = mutableSetOf(),
    protected val pathsInput:MutableCollection<Path> = mutableSetOf(),
    protected val keyValuesInput:MutableMap<String,Set<String>> = mutableMapOf()
) {

     abstract fun generateFeeds():Set<PropertiesFeed>
     protected fun generateArgFeed(args:Collection<String>):ArgsFeed= ArgsFeed(args)
    protected fun generateFilePropsFeed(inputPaths:Collection<Path>):FilePropertiesFeed=FilePropertiesFeed(inputPaths.toSet())
    protected fun generateTransparentFeed(keyValuesInput:Map<String,Set<String>>):TransparentFeed=TransparentFeed(keyValuesInput)
    abstract protected val logger:Logger
}