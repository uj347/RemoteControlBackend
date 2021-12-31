package remotecontrolbackend.configuration_utilities.extraction_feeds

class TransparentFeed(private val keyValues:Map<String,Set<String>>):PropertiesFeed {
    override val resultMap: Map<String, Set<String>>
        get() = extractRawProperties()

    override fun extractRawProperties(): Map<String, Set<String>> {
        return keyValues
    }
}