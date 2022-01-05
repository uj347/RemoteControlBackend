package com.uj.rcbackend.configurationutilities

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters


open class Property(
     val propertyName:String,
    val argPrefixes: Set<String> =setOf(propertyName),
    val defaultValues:Set<String> = mutableSetOf(),
    val required:Boolean=true,
    val multivalued:Boolean=true,
     val valuePredicate:(String)->Boolean={true}
) {

    val isConfigured:Boolean
            get()=propertyValues.isNotEmpty()
    val propertyValues:MutableSet<String> = object:HashSet<String>(){
        override fun add(element: String): Boolean {
            if(valuePredicate.invoke(element)) {
                return super.add(element)
            }else{
                logger.error("Passed invalid value [$element] to property [$propertyName], it will be dropped")
                return false}
        }

    }


    private inline fun <reified R> onConfiguredReturnOrThrow(block:()->R):R{
        if (isConfigured){
            return block()
        }else{
            throw UninitializedPropertyAccessException("$propertyName is now initialized")
        }
    }
    fun getSingleString():String{
        return onConfiguredReturnOrThrow { propertyValues.first() }
    }
    fun getStringValuesCollection():Collection<String>{
        return onConfiguredReturnOrThrow { propertyValues }
    }
    fun getSingleBooleanValue():Boolean{
        return onConfiguredReturnOrThrow {
        propertyValues.first().smartConvert()
        }
    }
    fun getSingleLong():Long{
       return onConfiguredReturnOrThrow { propertyValues.first().smartConvert() }
    }

    fun getCollectionOfLongs():Collection<Long>{
        return  onConfiguredReturnOrThrow { propertyValues.map {it.smartConvert()} }
    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Property

        if (propertyName != other.propertyName) return false

        return true
    }

    override fun hashCode(): Int {
        return propertyName.hashCode()
    }

    fun generateNotConfiguredClone():Property{
        return Property(propertyName,argPrefixes,defaultValues,required,multivalued,valuePredicate)
    }

    companion object{
        private inline fun <reified T> String.smartConvert():T{
           return TypeConverters.convert(this,T::class.java,"Booba") as T
        }

        val logger=LogManager.getLogger()

    }


}