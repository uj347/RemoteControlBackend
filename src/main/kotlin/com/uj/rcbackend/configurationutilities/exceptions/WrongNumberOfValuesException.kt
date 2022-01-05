package com.uj.rcbackend.configurationutilities.exceptions

import com.uj.rcbackend.configurationutilities.Property
import java.lang.RuntimeException

class WrongNumberOfValuesException(property: Property, expected:String):RuntimeException(
    "Property [${property.propertyName}] have wrong number of values: ${property.propertyValues.size}") {
}