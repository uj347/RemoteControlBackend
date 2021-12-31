package remotecontrolbackend.configuration_utilities.configuation_etaps

import remotecontrolbackend.configuration_utilities.Property
import remotecontrolbackend.configuration_utilities.extraction_feeds.PropertiesFeed
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

abstract class ProcessingEtap <T:PropertiesFeed> (val propertyInteractor: (Property, PropertiesFeed)->Property,
                                                  val feedType:KClass<T>?) {
  fun  performPropertyModification(property: Property,feed:PropertiesFeed ):Property =propertyInteractor(property,feed)




 companion object{

    fun <T:PropertiesFeed> basicPropertyInteraction(property:Property,feed:T?):Property{
            if(feed!!.containsPropertyKeys(property)){
                property.propertyValues.addAll(feed.extractValuesForProperty(property) )
            }
           return property
    }
//     val  basicFeedPropertyInteraction=
//         { property:Property,feed:PropertiesFeed? ->
//            if(feed!!.containsPropertyKeys(property)){
//                property.propertyValues.addAll(feed.extractValuesForProperty(property) )
//            }
//            property
//        }

        fun PropertiesFeed.containsPropertyKeys(property: Property):Boolean{
            val propKeys=property.argPrefixes+property.propertyName
            return resultMap.keys.filter{it in propKeys}.isNotEmpty()
        }

        fun PropertiesFeed.extractValuesForProperty(property: Property):Set<String>{
            val propKeys=property.argPrefixes+property.propertyName
            return  this.resultMap.entries
                .filter{(k,_)-> k in propKeys}.first().value
        }
    }
}
