package com.uj.rcbackend.configurationutilities.configuationetaps

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.configurationutilities.exceptions.WrongNumberOfValuesException
import com.uj.rcbackend.configurationutilities.extractionfeeds.EmptyFeed

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
        private val _logger= LogManager.getLogger()
    }

    override val logger: Logger
        get() = _logger
}