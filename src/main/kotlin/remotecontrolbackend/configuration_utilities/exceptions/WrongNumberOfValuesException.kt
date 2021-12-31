package remotecontrolbackend.configuration_utilities.exceptions

import remotecontrolbackend.configuration_utilities.Property
import java.lang.RuntimeException

class WrongNumberOfValuesException(property: Property, expected:String):RuntimeException(
    "Property [${property.propertyName}] have wrong number of values: ${property.propertyValues.size}") {
}