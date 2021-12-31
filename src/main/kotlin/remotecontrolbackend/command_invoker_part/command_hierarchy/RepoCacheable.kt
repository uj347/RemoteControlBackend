package remotecontrolbackend.command_invoker_part.command_hierarchy
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RepoCacheable

fun Command.isCacheable():Boolean{
    return this::class.java.isAnnotationPresent(RepoCacheable::class.java)
}
