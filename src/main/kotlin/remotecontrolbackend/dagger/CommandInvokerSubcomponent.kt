package remotecontrolbackend.dagger

import APP_COROUTINE_CONTEXT_LITERAL
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import remotecontrolbackend.command_invoker_part.command_hierarchy.BatCommand
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker
import remotecontrolbackend.command_invoker_part.command_hierarchy.CommandReference
import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommand
import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommandV2
import remotecontrolbackend.command_invoker_part.command_repo.BarbarianCommandRepo
import remotecontrolbackend.command_invoker_part.command_repo.DbBackedCommandRepo
import remotecontrolbackend.command_invoker_part.command_repo.ICommandRepo
import remotecontrolbackend.dagger.CommandInvokerModule.Companion.COMMAND_INVOKER_COROUTINE_CONTEXT_LITERAL
import remotecontrolbackend.moshi.*
import java.lang.reflect.Type
import java.nio.file.Path
import javax.inject.Named
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext


@ComInvScope
@Subcomponent(modules = [CommandInvokerModule::class])
interface CommandInvokerSubcomponent {
    fun getRepo():BarbarianCommandRepo
    fun getMoshi():Moshi
    fun getPointerMapAdapter(): JsonAdapter<Map<CommandReference, Path>>
    fun getInvoker():CommandInvoker
    @Named(COMMAND_INVOKER_COROUTINE_CONTEXT_LITERAL)
    fun getCommandInvokerCoroutineContext():CoroutineContext
    fun inject(commandInvoker: CommandInvoker)

    @Subcomponent.Builder
    interface CommandInvokerBuilder{
        fun build():CommandInvokerSubcomponent
    }
}

@Module
interface CommandInvokerModule{
    @ComInvScope
    @Binds
    fun bindDbRepo(dbRepo:DbBackedCommandRepo):ICommandRepo

    companion object{
        const val COMMAND_CLASS_MAP_LITERAL="COMMAND_CLASS_MAP"
        const val COMMAND_INVOKER_COROUTINE_CONTEXT_LITERAL="COMMAND_INVOKER_COROUTINE_CONTEXT"



        @ComInvScope
        @Provides
        @Named(COMMAND_INVOKER_COROUTINE_CONTEXT_LITERAL)
        fun provideCommanInvokerCoroutineContext(
            @Named(APP_COROUTINE_CONTEXT_LITERAL) appContext:CoroutineContext
        ):CoroutineContext{
            return appContext+Dispatchers.IO+ SupervisorJob(appContext.job)
        }


        @ComInvScope
        @Provides
        fun provideMoshi(serializableCommandToMapAdapter: SerializableCommandToMapAdapter): Moshi {

            return Moshi.Builder()
                .add(serializableCommandToMapAdapter)
                .add(PathAdapter())
                .build()

        }
        @ComInvScope
        @Provides
        fun providePointerMapAdapter(moshi: Moshi): JsonAdapter<Map<CommandReference, Path>> {
            val mapType: Type =
                Types.newParameterizedType(Map::class.java, CommandReference::class.java, Path::class.java)
            return moshi.adapter(mapType)
        }
       @ComInvScope
        @Provides
        @Named(COMMAND_CLASS_MAP_LITERAL)
        fun provideCommandClassMap():@JvmSuppressWildcards Map<String,Class<out Any>>{
            return mapOf(
                //TODO Здесь должны быть замаплены все команды: симплНейм - Класс
                BatCommand::class.java.simpleName to BatCommand::class.java,
                CommandReference::class.java.simpleName to CommandReference::class.java,
                MockCommand::class.java.simpleName to MockCommand::class.java,
                MockCommandV2::class.java.simpleName to MockCommandV2::class.java
            )
        }
    }

}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ComInvScope