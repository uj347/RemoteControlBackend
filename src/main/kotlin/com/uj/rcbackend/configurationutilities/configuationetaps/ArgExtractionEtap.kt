package com.uj.rcbackend.configurationutilities.configuationetaps

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.configurationutilities.extractionfeeds.ArgsFeed

class ArgExtractionEtap private constructor():ProcessingEtap<ArgsFeed>(
    ProcessingEtap.Companion::basicPropertyInteraction,
     ArgsFeed::class
){
    companion object{
        val instance=ArgExtractionEtap()
        private val _logger= LogManager.getLogger()
    }

    override val logger: Logger
        get() = _logger
}
