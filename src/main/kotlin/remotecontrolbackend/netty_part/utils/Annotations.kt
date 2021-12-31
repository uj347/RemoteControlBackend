package remotecontrolbackend.netty_part.utils
@Retention(AnnotationRetention.RUNTIME)
annotation class SpecificChain(
    val chainType:ChainType=ChainType.NOT_SPECIFIED
){

enum class ChainType{CHUNKED,FULLREQUEST,NOT_SPECIFIED}

}