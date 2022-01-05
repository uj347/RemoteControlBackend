package com.uj.rcbackend.configurationutilities.configuationetaps

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.uj.rcbackend.configurationutilities.Property
import com.uj.rcbackend.configurationutilities.extractionfeeds.PropertiesFeed
import kotlin.reflect.KClass

abstract class ProcessingEtap <T:PropertiesFeed> (val propertyInteractor: (Property, PropertiesFeed?)->Property,
                                                  val feedType:KClass<T>?) {

  fun  performPropertyModification(property: Property,feed:PropertiesFeed? ):Property =propertyInteractor(property,feed)

abstract val logger:Logger


 companion object{
    private val _logger=LogManager.getLogger()
    fun <T:PropertiesFeed> basicPropertyInteraction(property:Property,feed:T?):Property{

        _logger.debug("Performing basic property interaction with feed : [${feed}]")
            if(feed!!.containsPropertyKeys(property)){
                property.propertyValues.addAll(feed.extractValuesForProperty(property) )
            }
           return property
    }

        fun PropertiesFeed.containsPropertyKeys(property: Property):Boolean{
            val propKeys=setOf(property.propertyName)+property.argPrefixes
            logger.debug("Check  that PropertiesFeed[${this::class.simpleName}] with values: [${this.resultMap}] " +
                    "contains any keys for Property:[Name: ${property.propertyName} ArgPrefixes: ${property.argPrefixes}]" +
                    " result is ${resultMap.keys.any { it in propKeys }}")

            return resultMap.keys.any { it in propKeys }
        }

        fun PropertiesFeed.extractValuesForProperty(property: Property):Set<String>{
            val propKeys=property.argPrefixes+property.propertyName
            logger.debug("Extracting values for property: [${property.propertyName}]")
            return  this.resultMap.entries
                .filter{(k,_)-> k in propKeys}
                .first()
                .also { logger.debug("Extracted values for property[${property.propertyName}]: [$it]") }
                .value
        }
    }
}
