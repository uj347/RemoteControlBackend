package com.uj.rcbackend.configurationutilities.configuationetaps

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.configurationutilities.extractionfeeds.FilePropertiesFeed

class FileExtractionEtap private constructor():ProcessingEtap<FilePropertiesFeed>(
   ProcessingEtap.Companion::basicPropertyInteraction,
    FilePropertiesFeed::class
) {
    companion object{
        val instance=FileExtractionEtap()
        private val _logger= LogManager.getLogger()
    }

    override val logger: Logger
        get() = _logger
}