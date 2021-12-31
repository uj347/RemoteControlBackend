package remotecontrolbackend.configuration_utilities.configuation_etaps

import remotecontrolbackend.configuration_utilities.extraction_feeds.EmptyFeed

class SetDefaultEtap private constructor():ProcessingEtap <EmptyFeed>(
feedType = null,
    propertyInteractor = {p,_->
        if(!p.isConfigured) {
            p.propertyValues.addAll(p.defaultValues.toSet())
        }
        p
    }){
    companion object{
        val instance=SetDefaultEtap()
    }
}
