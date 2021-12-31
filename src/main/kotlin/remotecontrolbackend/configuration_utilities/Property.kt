package remotecontrolbackend.configuration_utilities


class Property(
    val propertyName:String,
    val argPrefixes: Set<String>,
    val defaultValues:Set<String> = mutableSetOf(),
    val required:Boolean=true,
    val multivalued:Boolean=true,
) {
    val isConfigured:Boolean
            get()=propertyValues.isNotEmpty()

    val propertyValues:MutableSet<String> = mutableSetOf()

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
        return Property(propertyName,argPrefixes,defaultValues,required,multivalued)
    }


}