package com.uj.rcbackend.configurationutilities.feedgenerators

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.configurationutilities.Property
import com.uj.rcbackend.configurationutilities.extractionfeeds.PropertiesFeed
import java.nio.file.Path
import java.nio.file.Paths

class DependentFeedGenerator(
    argsInput: MutableCollection<String> = mutableSetOf(),
    pathsInput: MutableCollection<Path> = mutableSetOf(),
    keyValuesInput: MutableMap<String, Set<String>> = mutableMapOf(),
    val propDependencies: MutableCollection<PropertyDependency> = mutableSetOf()
) : SimpleFeedGenerator(
    argsInput, pathsInput, keyValuesInput
) {
    override fun generateFeeds(): Set<PropertiesFeed> {
        for (propertyDependency in propDependencies) {
            propDependencies.forEach { propertyDependency.feedGeneratorInteraction(this, it.property) }
        }
        return super.generateFeeds()
    }

    override val logger: Logger
        get() = _logger

    open class PropertyDependency(
        val property: Property,
        val feedGeneratorInteraction: (DependentFeedGenerator, Property) -> Unit
    ) {
        val propName: String
            get() = property.propertyName

        fun notConfiguredClone():PropertyDependency{
            return PropertyDependency(property.generateNotConfiguredClone(),feedGeneratorInteraction)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PropertyDependency

            if (property != other.property) return false

            return true
        }

        override fun hashCode(): Int {
            return property.hashCode()
        }

    }


    companion object {
        val propertiesLookUpPathsProperty: Property
            get() = Property(
                propertyName = "PropertiesLookUpPaths",
                argPrefixes = setOf("-propLookUp", "plup"),
                defaultValues = setOf(System.getProperty("user.dir"))
            )

        private val _logger=LogManager.getLogger()


        val propertiesLookUpPathsDependency
              get()= PropertyDependency(
            Companion.propertiesLookUpPathsProperty,
            { feedGen, property ->
                _logger.debug("Dependency interaction performed by property [${property.propertyName} with values [${property.propertyValues}]]" +
                        "with feed gen [$feedGen] ")
                val propPaths = mutableSetOf<Path>()
                property.propertyValues.map { Paths.get(it) }.toCollection(propPaths)
                feedGen.pathsInput.clear()
                _logger.debug("Adding lookUpDirs to dependent feed: $propPaths")
                feedGen.pathsInput.addAll(propPaths)
            })
    }
}