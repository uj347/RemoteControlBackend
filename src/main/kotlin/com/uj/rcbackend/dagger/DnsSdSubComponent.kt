package com.uj.rcbackend.dagger

import dagger.Subcomponent
import com.uj.rcbackend.dnssdpart.DnsSdManager
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