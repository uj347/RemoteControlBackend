package com.uj.rcbackend.configurationutilities

import com.uj.rcbackend.configurationutilities.configuationetaps.*
import com.uj.rcbackend.configurationutilities.extractionfeeds.PropertiesFeed
import com.uj.rcbackend.configurationutilities.feedgenerators.DependentFeedGenerator
import com.uj.rcbackend.configurationutilities.feedgenerators.SimpleFeedGenerator

class PropertiesEngine private constructor(

   private val processor: PropertiesProcessor,
    ){
//    private val appArgs:Collection<String>,
//
//    private val mainProcessorDependencies
//    :Collection<DependentFeedGenerator.PropertyDependency> =setOf(
//        DependentFeedGenerator.Companion.propertiesLookUpPathsDependency),
//
//    private val preprocessingEtaps:LinkedHashSet<ProcessingEtap<out PropertiesFeed>> = onlyArgExtractionEtaps,
//
//    private val processingEtaps: LinkedHashSet<ProcessingEtap<out PropertiesFeed>> = fullChainEtaps,
//
//    private val appPropertiesToSet:Collection<Property>,
//
//    private val processorDeps:Collection<DependentFeedGenerator.PropertyDependency>) {


    fun fire( propertiesToSet:Collection<Property>):Collection<Property>{
return processor.processProperties(propertiesToSet)
    }



    class Factory(
                  private val mainProcessorDependencies
                  :Collection<DependentFeedGenerator.PropertyDependency> =setOf(),

                  private val preProcessingEtaps:LinkedHashSet<ProcessingEtap<out PropertiesFeed>> = onlyArgExtractionEtaps,

                  private val processingEtaps: LinkedHashSet<ProcessingEtap<out PropertiesFeed>> = fullChainEtaps,

                ){


        fun newInstance(appArgs:Collection<String>):PropertiesEngine{
            val instanceDeps=mainProcessorDependencies.map{it.notConfiguredClone()}
            val instanceDepProps=instanceDeps.map{it.property}
            val preporcessorFeedGenerator=SimpleFeedGenerator(appArgs.toMutableSet())
            val processorFeedGenerator=DependentFeedGenerator(argsInput = appArgs.toMutableSet(),
                propDependencies =instanceDeps.toMutableSet())

            val instPreprocessor=PropertiesProcessor(preProcessingEtaps,preporcessorFeedGenerator)

            instPreprocessor.processProperties(instanceDepProps)
            PropertiesProcessor(processingEtaps,processorFeedGenerator).let{return PropertiesEngine(it)}

        }
    }

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
