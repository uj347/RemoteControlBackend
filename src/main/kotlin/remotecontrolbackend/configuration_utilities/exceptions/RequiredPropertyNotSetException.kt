package remotecontrolbackend.configuration_utilities.exceptions

import remotecontrolbackend.configuration_utilities.Property
import java.lang.RuntimeException

class RequiredPropertyNotSetException(property: Property):RuntimeException(
    "Required property [${property.propertyName}] isn't set ")