package IntrestingTests

import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject

class DaggerCheck
@Inject
constructor()
{

    @Inject
    lateinit var string: String

    fun printCont(){
        println(string)
    }


}

@Component(modules = [testModule::class])
interface TestComponent{
    fun getDaggerCheck():DaggerCheck
}

@Module
interface testModule{
    companion object{
        @Provides
        fun provideString():String{
            return "Injection occured in constructor, but i'm here"
        }
    }
}
fun main(){
    DaggerTestComponent.create().getDaggerCheck().printCont()
}