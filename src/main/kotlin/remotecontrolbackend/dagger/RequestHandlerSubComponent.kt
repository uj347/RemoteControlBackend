package remotecontrolbackend.dagger

import dagger.Component
import dagger.Subcomponent
import javax.inject.Scope
@RhScope
@Subcomponent
interface RequestHandlerSubComponent {

    @Subcomponent.Builder
    interface RhBuilder{
        fun buildRh():RequestHandlerSubComponent
    }
}
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RhScope