package remotecontrolbackend.configuration_utilities.extraction_feeds

interface PropertiesFeed {
    val resultMap:Map<String,Set<String>>
    fun extractRawProperties():Map<String,Set<String>>
}