package remotecontrolbackend.configuration_utilities

import remotecontrolbackend.configuration_utilities.configuation_etaps.CheckEtap
import remotecontrolbackend.configuration_utilities.configuation_etaps.ProcessingEtap
import remotecontrolbackend.configuration_utilities.configuation_etaps.SetDefaultEtap
import remotecontrolbackend.configuration_utilities.extraction_feeds.PropertiesFeed
import remotecontrolbackend.configuration_utilities.feed_generators.AbstractFeedGenerator
import java.util.*

class PropertiesProcessor(private val etaps:LinkedHashSet<ProcessingEtap<out PropertiesFeed>> = linkedSetOf(SetDefaultEtap.instance,CheckEtap.instance),
                          private val generator: AbstractFeedGenerator
                          ){
    fun processProperties(props:Collection<Property>):Collection<Property>{
        val availableFeeds=generator.generateFeeds()
        val notProcessedProps= props.toMutableSet()
        for(etap in etaps){
            val currentFeed=availableFeeds
                .filter { it::class==etap.feedType }
                .first()

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