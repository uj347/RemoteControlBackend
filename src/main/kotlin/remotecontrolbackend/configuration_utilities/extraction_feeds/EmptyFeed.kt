package remotecontrolbackend.configuration_utilities.extraction_feeds

class EmptyFeed:PropertiesFeed {
    override val resultMap: Map<String, Set<String>>
        get() = extractRawProperties()

    override fun extractRawProperties(): Map<String, Set<String>> {
        return mapOf<String, Set<String>>()
    }
}