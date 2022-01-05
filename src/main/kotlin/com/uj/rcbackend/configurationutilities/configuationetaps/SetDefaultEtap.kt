package com.uj.rcbackend.configurationutilities.configuationetaps

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.configurationutilities.extractionfeeds.EmptyFeed

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
        private val _logger= LogManager.getLogger()
    }

    override val logger: Logger
        get() = _logger
}
