package remotecontrolbackend.command_invoker_part.command_repo

import DataBaseModule.Companion.FILEBASED_DB_LITEREAL
import com.squareup.moshi.Moshi
import com.zaxxer.hikari.HikariDataSource
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.command_invoker_part.command_hierarchy.CommandReference
import remotecontrolbackend.command_invoker_part.command_hierarchy.SerializableCommand
import remotecontrolbackend.database.H2Database
import javax.inject.Inject
import javax.inject.Named

class DbBackedCommandRepo @Inject constructor(@Named(FILEBASED_DB_LITEREAL)
                                             private val databasePair:@JvmSuppressWildcards Pair<H2Database, HikariDataSource>,
                                              moshi: Moshi):ICommandRepo {
    companion object{
        private val logger=LogManager.getLogger()
    }

    val database=databasePair.first
    val serializableComMoshiAdapter=moshi.adapter<SerializableCommand>(SerializableCommand::class.java)

    override var isInitialized: Boolean=false
        @Synchronized()
        set(newVal){field=newVal}
        @Synchronized
        get():Boolean{return field}


    override suspend fun initialize() {
        database.h2Queries.initIfNotExistsCacheCommandRepo()
        isInitialized=true
    }

    override suspend fun addToRepo(command: SerializableCommand): Boolean {
        return withContext(Dispatchers.IO){
            kotlin.runCatching{
                logger.debug("Inserting into database JSON: [[${command.createReference().toJson()}]]")
                database.h2Queries.insertOrUpdate(
                    CommandReference.createReference(command).toJson(),
                    command.toJson())
                true
            }.getOrElse {
                logger.error("Error occurred in adding to CachedCommands repo: ${it.message}")
                false }
        }
    }

    override suspend fun getCommand(reference: CommandReference): SerializableCommand? {
       return withContext(Dispatchers.IO){
            kotlin.runCatching {
              database
                  .h2Queries
                  .selectSerializedCommand(reference.toJson(),
                      {_,serCom->
                          serializableCommandRestorer
                              .invoke(serCom)
                              .toSerializableCommand()}
                  )
                  .executeAsOne()
            }.getOrElse {
                logger.error("Error occurred in getting command from CachedCommands repo: ${it.message}")
                null }
        }
    }

    override suspend fun removeCommand(reference: CommandReference): Boolean {
        return withContext(Dispatchers.IO){
            kotlin.runCatching {
                database.
               h2Queries.deleteByRef(reference.toJson())
               true
            }.getOrElse {
                logger.error("Error occurred in deleting command from CachedCommands repo: ${it.message}")
                false
            }
        }
    }

    override suspend fun getAllReferences(): Collection<CommandReference> {
      return  withContext(Dispatchers.IO){
          kotlin.runCatching {
              database
                  .h2Queries
                  .selectAllReferences()
                  .executeAsList()
                  .map (serializableCommandRestorer)
                  .map {it.toForcedCommandReference()}

          }.getOrElse {
              logger.error("Error occurred in  getting all references from CachedCommands repo: ${it.message}\n" +
                      "stack trace: ${it.stackTraceToString()}")
              setOf()
          }
      }
    }

    override suspend fun terminalOperation(): Boolean {
       //No terminal operations required
        return true
    }

    private fun SerializableCommand.toJson():String{
        return serializableComMoshiAdapter.toJson(this)
    }
    private fun String.toSerializableCommand():SerializableCommand{
        return serializableComMoshiAdapter.fromJson(this)!!
    }

    private fun String.toForcedCommandReference():CommandReference{
        return serializableComMoshiAdapter.fromJson(this)!! as CommandReference
    }

   private val serializableCommandRestorer= {malformedJson:String->
       malformedJson.replace("\\\"","\"")
           .removePrefix("\"").removeSuffix("\"")
   }



}