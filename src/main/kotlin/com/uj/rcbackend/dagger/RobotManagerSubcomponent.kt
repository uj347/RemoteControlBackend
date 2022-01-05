package com.uj.rcbackend.dagger

import APP_COROUTINE_CONTEXT_LITERAL
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import com.uj.rcbackend.robot.RobotManager
import javax.inject.Named
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext

@RobotScope
@Subcomponent(modules = [RobotManagerModule::class])
interface RobotManagerSubcomponent {


    fun inject(robootManager:RobotManager)
    @Subcomponent.Builder
    interface RobotManagerBuilder{
        fun build():RobotManagerSubcomponent
    }
}

@Module
interface RobotManagerModule{
    companion object{
        const val ROBOT_COROUTINE_CONTEXT_LITERAL="ROBOT_COROUTINE_CONTEXT"
        @RobotScope
        @Named(ROBOT_COROUTINE_CONTEXT_LITERAL)
        @Provides
        fun provideRobotCoroutineContext(
            @Named(APP_COROUTINE_CONTEXT_LITERAL)
            appCoroutineContext: CoroutineContext
        ): CoroutineContext {
            return appCoroutineContext+ Dispatchers.IO+ SupervisorJob(appCoroutineContext.job)
        }
    }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RobotScope