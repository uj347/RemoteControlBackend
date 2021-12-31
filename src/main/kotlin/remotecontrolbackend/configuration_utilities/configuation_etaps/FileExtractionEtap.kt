package remotecontrolbackend.configuration_utilities.configuation_etaps

import remotecontrolbackend.configuration_utilities.Property
import remotecontrolbackend.configuration_utilities.extraction_feeds.FilePropertiesFeed

class FileExtractionEtap private constructor():ProcessingEtap<FilePropertiesFeed>(
   ProcessingEtap.Companion::basicPropertyInteraction,
    FilePropertiesFeed::class
) {
    companion object{
        val instance=FileExtractionEtap()
    }
}