package remotecontrolbackend

import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import remotecontrolbackend.netty_part.auth_part.ConcreteAuthHandler
import remotecontrolbackend.netty_part.auth_part.MockUserRepo
import javax.inject.Scope

@AuthScope
@Subcomponent(modules = arrayOf(TestAuthModule::class))
interface AuthComponent {
    fun inject( authHandler: ConcreteAuthHandler)
    @Subcomponent.Builder
    interface AuthBuilder{
        fun build():AuthComponent
    }

}

@Module
abstract interface RealAuthModule{
  //TODO


}


@Module
interface TestAuthModule{
@AuthScope
@Binds abstract fun bindsMockUserRepo(mockUserRepo: MockUserRepo): UserRepo

}




@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthScope