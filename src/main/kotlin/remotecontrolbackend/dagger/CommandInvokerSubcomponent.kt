package remotecontrolbackend.dagger

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.command_invoker_part.command_repo.CommandReference
import remotecontrolbackend.command_invoker_part.command_repo.CommandRepo
import remotecontrolbackend.moshi.*
import java.lang.reflect.Type
import java.nio.file.Path
import javax.inject.Scope


@ComInvScope
@Subcomponent(modules = [CommandInvokerModule::class])
interface CommandInvokerSubcomponent {
    fun getRepo():CommandRepo
    fun getMoshi():Moshi
    fun getPointerMapAdapter(): JsonAdapter<Map<CommandReference, Path>>
    fun getInvoker():CommandInvoker
    fun inject(commandInvoker: CommandInvoker)

    @Subcomponent.Builder
    interface CommandInvokerBuilder{
        fun build():CommandInvokerSubcomponent
    }
}

@Module
interface CommandInvokerModule{
    companion object{
        @Provides
        fun provideMoshi(): Moshi {
          //  val strStrMapType:Type=Types.newParameterizedType(Map::class.java,String::class.java,String::class.java)
            return Moshi.Builder()
               // .add(TestMockToCommaAdapter())


                .add(SerializableCommandToMapAdapter())
                .add(PathAdapter())
                .add(ComRefStringAdapter())
                .build()

        }

        @Provides
        fun providePointerMapAdapter(moshi: Moshi): JsonAdapter<Map<CommandReference, Path>> {
            val mapType: Type =
                Types.newParameterizedType(Map::class.java, CommandReference::class.java, Path::class.java)
            return moshi.adapter(mapType)
        }
    }

}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ComInvScope