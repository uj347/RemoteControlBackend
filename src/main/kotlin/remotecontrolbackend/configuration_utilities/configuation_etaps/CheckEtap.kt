package remotecontrolbackend.configuration_utilities.configuation_etaps

import remotecontrolbackend.configuration_utilities.exceptions.WrongNumberOfValuesException
import remotecontrolbackend.configuration_utilities.extraction_feeds.EmptyFeed

class CheckEtap private constructor():ProcessingEtap<EmptyFeed> (
    feedType = null,
    propertyInteractor = {
        p,_->
        if(!p.multivalued&&p.propertyValues.size>1){
            throw WrongNumberOfValuesException(p," at most 1")
        }
        if(p.propertyValues.size==0&&p.required){
            throw WrongNumberOfValuesException(p,"at least 1" )
        }
        p
    }){
    companion object{
        val instance=CheckEtap()
    }
}