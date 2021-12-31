package remotecontrolbackend.configuration_utilities.configuation_etaps

import remotecontrolbackend.configuration_utilities.Property
import remotecontrolbackend.configuration_utilities.extraction_feeds.ArgsFeed

class ArgExtractionEtap private constructor():ProcessingEtap<ArgsFeed>(
    ProcessingEtap.Companion::basicPropertyInteraction,
     ArgsFeed::class
){
    companion object{
        val instance=ArgExtractionEtap()
    }
}
