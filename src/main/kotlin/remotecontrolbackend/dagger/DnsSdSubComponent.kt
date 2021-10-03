package remotecontrolbackend.dagger

import dagger.Subcomponent
import remotecontrolbackend.dns_sd_part.DnsSdManager
import javax.inject.Scope

@DnsSdScope
@Subcomponent
interface DnsSdSubComponent {
fun inject(dnsSdManager: DnsSdManager)
    @Subcomponent.Builder
    interface DnsSdSubComponentBuilder{
        fun buildDnsSdSubComp():DnsSdSubComponent
    }
}
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class DnsSdScope