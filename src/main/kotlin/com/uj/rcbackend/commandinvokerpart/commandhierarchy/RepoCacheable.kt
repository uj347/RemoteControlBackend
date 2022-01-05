package com.uj.rcbackend.commandinvokerpart.commandhierarchy
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RepoCacheable

fun Command.isCacheable():Boolean{
    return this::class.java.isAnnotationPresent(RepoCacheable::class.java)
}
