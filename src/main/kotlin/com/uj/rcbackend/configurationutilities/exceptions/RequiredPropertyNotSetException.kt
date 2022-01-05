package com.uj.rcbackend.configurationutilities.exceptions

import com.uj.rcbackend.configurationutilities.Property
import java.lang.RuntimeException

class RequiredPropertyNotSetException(property: Property):RuntimeException(
    "Required property [${property.propertyName}] isn't set ")