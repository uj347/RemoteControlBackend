package remotecontrolbackend.configuration_utilities.feed_generators

import remotecontrolbackend.configuration_utilities.Property
import remotecontrolbackend.configuration_utilities.extraction_feeds.PropertiesFeed
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

    open class PropertyDependency(
        val property: Property,
        val feedGeneratorInteraction: (DependentFeedGenerator, Property) -> Unit
    ) {
        val propName: String
            get() = property.propertyName

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
                argPrefixes = setOf("-propLookUp", "-PLUP"),
                defaultValues = setOf(System.getProperty("user.dir"))
            )


        val propertiesLookUpPathsDependency = PropertyDependency(
            Companion.propertiesLookUpPathsProperty,
            { feedGen, property ->
                val propPaths = mutableSetOf<Path>()
                property.propertyValues.map { Paths.get(it) }.toCollection(propPaths)
                feedGen.pathsInput.clear()
                feedGen.pathsInput.addAll(propPaths)
            })
    }
}