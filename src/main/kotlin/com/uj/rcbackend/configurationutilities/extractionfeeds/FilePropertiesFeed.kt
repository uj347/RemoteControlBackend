package com.uj.rcbackend.configurationutilities.extractionfeeds

import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory

open class FilePropertiesFeed(private val pathsToLook:Set<Path>):PropertiesFeed {
companion object{
    private val _logger=LogManager.getLogger()
}

    override val logger=_logger
    override val resultMap by lazy { extractRawProperties() }

    override fun extractRawProperties(): Map<String, Set<String>> {
        val result = mutableMapOf<String,Set<String>>()
        for(path in pathsToLook){
        recursiveExtraction(path).let { result.putAll(it) }
           }
        return result
    }

    private fun extractAllPropFilesFromDir(dir: Path):Set<Path>{
        val result= mutableSetOf<Path>()
        if (dir.isDirectory()){
            Files.newDirectoryStream(dir).use{
                it.filter { it.extension=="properties" }.forEach (result::add)
            }
        }
        return result
    }

    private fun extractPropertiesFromFile(file:Path):Map<String,Set<String>>{
        val result = mutableMapOf<String,Set<String>>()
        val props=Properties()
        kotlin.runCatching { props.load(file.inputStream()) }
        for (propName in props.stringPropertyNames()) {
            if (propName.isNotBlank()) {
                Stream.of(props.getProperty(propName)).map { it.split("&&").toSet() }
                    .forEach { result.put(propName, it) }
            }
        }
        return result
    }

    private fun recursiveExtraction(currentPath:Path):Map<String,Set<String>> {
        val result = mutableMapOf<String,Set<String>>()
        if(!currentPath.isAbsolute){
            logger.error("Not absolute path [$currentPath] appeared in File Properties Feed")
        }
        kotlin.runCatching {
                if(currentPath.isDirectory()){
                Files.newDirectoryStream(currentPath)
                    .map { recursiveExtraction(it) }
                    .reduce { acc, map -> acc+map }
                    .let{result.putAll(it)}
                }else{
                    if(currentPath.extension=="properties") {
                        extractPropertiesFromFile(currentPath)
                            .let {result.putAll(it) }
                    }
                }
            }
        return result
    }



}