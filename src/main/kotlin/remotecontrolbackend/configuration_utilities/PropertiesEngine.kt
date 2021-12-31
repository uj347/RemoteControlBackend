package remotecontrolbackend.configuration_utilities

import remotecontrolbackend.configuration_utilities.configuation_etaps.*
import remotecontrolbackend.configuration_utilities.extraction_feeds.PropertiesFeed
import remotecontrolbackend.configuration_utilities.feed_generators.AbstractFeedGenerator
import remotecontrolbackend.configuration_utilities.feed_generators.DependentFeedGenerator
import remotecontrolbackend.configuration_utilities.feed_generators.SimpleFeedGenerator

class PropertiesEngine(
    private val appArgs:Collection<String>,
    private val mainProcessorDependencies
    :Collection<DependentFeedGenerator.PropertyDependency> =setOf(
        DependentFeedGenerator.Companion.propertiesLookUpPathsDependency),
    private val preprocessingEtaps:LinkedHashSet<ProcessingEtap<out PropertiesFeed>> = onlyArgExtractionEtaps,
    private val processingEtaps: LinkedHashSet<ProcessingEtap<out PropertiesFeed>>,
    private val appPropertiesToSet:Collection<Property>

    ,

                       private val processorDeps:Collection<DependentFeedGenerator.PropertyDependency>) {
    var fired:Boolean=false


//    fun fire():Collection<Property>{
//
//    }
    companion object{

    val defaultAndCheckEtaps:LinkedHashSet<ProcessingEtap<out PropertiesFeed>>
        = linkedSetOf<ProcessingEtap<out PropertiesFeed>>(
            SetDefaultEtap.instance,
            CheckEtap.instance)

    val fullChainEtaps
        get() = linkedSetOf<ProcessingEtap<out PropertiesFeed>> (
            ArgExtractionEtap.instance,
            FileExtractionEtap.instance,
        ).also { it.addAll(defaultAndCheckEtaps) }

    val  onlyArgExtractionEtaps
        get()= linkedSetOf <ProcessingEtap<out PropertiesFeed>> ( ArgExtractionEtap.instance)
            .also { it.addAll(defaultAndCheckEtaps) }
}
}