import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariDataSource
import dagger.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.annotations.Nullable

import com.uj.rcbackend.MainLauncher
import com.uj.rcbackend.commandinvokerpart.commandinvoker.CommandInvoker
import com.uj.rcbackend.dagger.*
import com.uj.rcbackend.database.H2Database

import com.uj.rcbackend.dnssdpart.DnsSdManager
import com.uj.rcbackend.fileservicepart.FileService
import com.uj.rcbackend.fileservicepart.pathlistproviderpart.IFileServicePathListProvider

import com.uj.rcbackend.nettypart.NettyConnectionManager
import com.uj.rcbackend.robot.RobotManager
import java.nio.file.Files

import java.nio.file.Path
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.exists

const val PORT_LITERAL = "port"
const val ROOT_DIR_LITERAL = "rootDir"
const val APP_COROUTINE_CONTEXT_LITERAL = "AppCoroutineContext"
const val SSL_ENABLED_LITERAL = "SSLEnabled"
const val AUTH_ENABLED_LITERAL = "AuthEnabled"
const val DB_PASSWORD_LITERAL ="DbPassword"
const val SSL_TAKEOUT_DIR_LITERAL="SSlTakeoutDir"


@Singleton
@Component(
    modules = [MainModule::class, NettySubcomponentModule::class, DnsSdSubcomponentModule::class,
        CommandInvokerSubcomponentModule::class, CommandInvokerSubcomponentModule::class,
        RobotManagerSubcomponentModule::class, FileServiceSubcomponentModule::class,
        DataBaseModule::class]
)
interface MainComponent {
    fun inject(mainClass: MainLauncher)
    fun getCommandInvoker(): CommandInvoker
    fun getComandInvokerSubcompBuilder(): CommandInvokerSubcomponent.CommandInvokerBuilder
    fun getNettySubcomponentBuilder(): NettySubComponent.NettySubComponentBuilder
    fun getNettyConnectionManager():NettyConnectionManager
    fun getLauncher(): MainLauncher
    fun getRobotManager(): RobotManager
    fun getFileServiceSubcomponentBuilder():FileServiceSubcomponent.Builder
    fun getFileService():FileService


    @Named(APP_COROUTINE_CONTEXT_LITERAL)
    fun getAppCoroutineContext(): CoroutineContext

    @Component.Builder
    interface MainBuilder {
        fun buildMainComponent(): MainComponent

        @BindsInstance()
        fun setPathProvider(filePathsProvider: IFileServicePathListProvider?): MainBuilder

        @BindsInstance
        fun setPort(@Named(PORT_LITERAL) portN: Int): MainBuilder

        @BindsInstance
        fun setWorkDirectory(@Named(ROOT_DIR_LITERAL) invokerDirectory: Path): MainBuilder


        @BindsInstance
        fun isSSLEnabled(@Named(SSL_ENABLED_LITERAL) isSSLEnabled: Boolean): MainBuilder

        @BindsInstance
        fun isAuthEnabled(@Named(AUTH_ENABLED_LITERAL) isAuthEnabled: Boolean): MainBuilder

        @BindsInstance
        fun dbPassword(@Named(DB_PASSWORD_LITERAL) dbPassword:String):MainBuilder

        @BindsInstance
        fun sslTakeout(@Named(SSL_TAKEOUT_DIR_LITERAL)@Nullable sslTakeoutDir:String? ):MainBuilder
    }

}


@Module
interface MainModule {
    companion object {
        @Singleton
        @Named(APP_COROUTINE_CONTEXT_LITERAL)
        @Provides
        fun provideAppCoroutineScope(): CoroutineContext {
            return Dispatchers.Default + SupervisorJob()
        }

    }
}


@Module(subcomponents = [DnsSdSubComponent::class])
interface DnsSdSubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideDnsSdManager(
            dnsSdSubComponentBuilder: DnsSdSubComponent.DnsSdSubComponentBuilder,
            @Named(PORT_LITERAL) portN: Int
        ): DnsSdManager {
            return DnsSdManager(dnsSdSubComponentBuilder, portN)
        }
    }
}


@Module(subcomponents = [NettySubComponent::class])
interface NettySubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideNettyManager(
            nettySubComponentBuilder: NettySubComponent.NettySubComponentBuilder,
            @Named(PORT_LITERAL) portN: Int,
            @Named(SSL_ENABLED_LITERAL) isSSLEnabled: Boolean,
            @Named(AUTH_ENABLED_LITERAL) isAuthEnabled: Boolean
        ): NettyConnectionManager {
            return NettyConnectionManager(nettySubComponentBuilder, portN, isSSLEnabled, isAuthEnabled)
        }
    }


}


@Module(subcomponents = [CommandInvokerSubcomponent::class])
interface CommandInvokerSubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideCommandInvoker(
            @Named(ROOT_DIR_LITERAL)
            invokerDirectory: Path,
            commandInvokerSubcomponentBuilder: CommandInvokerSubcomponent.CommandInvokerBuilder
        ): CommandInvoker {
            return CommandInvoker(invokerDirectory, commandInvokerSubcomponentBuilder)
        }
    }


}

@Module(subcomponents = [RobotManagerSubcomponent::class])
interface RobotManagerSubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideRobotManager(
            subcomponentBuilder: RobotManagerSubcomponent.RobotManagerBuilder
        ): RobotManager {
            return RobotManager(subcomponentBuilder)
        }
    }
}


@Module(subcomponents = [FileServiceSubcomponent::class])
interface FileServiceSubcomponentModule {
    companion object {
        @Singleton
        @Provides
        fun provideFileService(
            fileServiceBuilder: FileServiceSubcomponent.Builder,
            pathProvider:IFileServicePathListProvider?
        ): FileService {
            return FileService(fileServiceBuilder,pathProvider)
        }
    }
}


@Module
interface DataBaseModule{
    companion object {
        const val IN_MEMORY_DB_LITEREAL="IN_MEMORY_DB"
        const val FILEBASED_DB_LITEREAL="FILE_DB"
        const val DB_FILE_NAME="ujRemoteControlDB"
        const val DB_DIR_PATH_LITERAL="Database"

        @Singleton
        @Named(DB_DIR_PATH_LITERAL)
        @Provides
        fun provideDbPath(@Named(ROOT_DIR_LITERAL)workDir:Path):Path{
            return workDir.resolve("database")
        }

        @Singleton
        @Named(FILEBASED_DB_LITEREAL)
        @Provides
        fun provideH2FileDb(
            @Named(DB_DIR_PATH_LITERAL)dbDir:Path,
            @Named(DB_PASSWORD_LITERAL)pswd:String
        ):@JvmSuppressWildcards Pair<H2Database,HikariDataSource>{
            if(!dbDir.exists()){
                Files.createDirectories(dbDir)
            }
            val jdbcUrl="jdbc:h2:file:$dbDir/$DB_FILE_NAME;CIPHER=AES;USER=uj-remote-app;PASSWORD=$pswd root;MODE=MYSQL;DATABASE_TO_LOWER=TRUE"
            val hikariDataSource=HikariDataSource()
            hikariDataSource.jdbcUrl=jdbcUrl
            hikariDataSource.driverClassName="org.h2.Driver"
//            hikariDataSource.password=pswd
            return Pair (H2Database.invoke(hikariDataSource.asJdbcDriver()),hikariDataSource)
        }


        var runTimeDbCounter:Int=1

        @Named(IN_MEMORY_DB_LITEREAL)
        @Provides
        fun provideH2MemDb():@JvmSuppressWildcards Pair<H2Database,HikariDataSource>{
            val jdbcUrl="jdbc:h2:mem:runtimeDB#${runTimeDbCounter++};MODE=MYSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
            val hikariDataSource=HikariDataSource()
            hikariDataSource.maximumPoolSize=1
            hikariDataSource.jdbcUrl=jdbcUrl
            return  Pair(H2Database.invoke(hikariDataSource.asJdbcDriver()),hikariDataSource)
        }

    }
}

