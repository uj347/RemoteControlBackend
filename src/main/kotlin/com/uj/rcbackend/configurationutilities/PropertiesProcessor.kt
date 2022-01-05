package com.uj.rcbackend.configurationutilities

import org.apache.logging.log4j.LogManager
import com.uj.rcbackend.configurationutilities.configuationetaps.CheckEtap
import com.uj.rcbackend.configurationutilities.configuationetaps.ProcessingEtap
import com.uj.rcbackend.configurationutilities.configuationetaps.SetDefaultEtap
import com.uj.rcbackend.configurationutilities.extractionfeeds.PropertiesFeed
import com.uj.rcbackend.configurationutilities.feedgenerators.AbstractFeedGenerator
import java.util.*

class PropertiesProcessor(private val etaps:LinkedHashSet<ProcessingEtap<out PropertiesFeed>> = linkedSetOf(SetDefaultEtap.instance,CheckEtap.instance),
                          private val generator: AbstractFeedGenerator
                          ){
    companion object{
        val logger=LogManager.getLogger()
    }
    fun processProperties(props:Collection<Property>):Collection<Property>{
        val availableFeeds=generator.generateFeeds()
        val notProcessedProps= props.toMutableSet()
        println("Generator generated feeds :${availableFeeds}")
        for(etap in etaps){
            logger.debug("Choosing feed for etap[${etap::class.simpleName}] " +
                    "with feed type[${etap.feedType?.simpleName?:"NO ASSOCIATED FEEDTYPE"}]")
           //TODO

            val currentFeed=availableFeeds
                .filter {etap.feedType?.isInstance(it)?:false }
                .firstOrNull()

           notProcessedProps.forEach {
               etap.performPropertyModification(it,currentFeed)
           }

            notProcessedProps.iterator().let{propIter->
                while (propIter.hasNext()){
                    propIter.next().let {
                        if (it.isConfigured){
                        propIter.remove()
                    }
                    }
                }
            }

        }
        return props
    }

}