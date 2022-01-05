package com.uj.rcbackend.fileservicepart.pathrepopart

import DataBaseModule.Companion.IN_MEMORY_DB_LITEREAL
import com.zaxxer.hikari.HikariDataSource
import org.apache.logging.log4j.LogManager
import com.uj.rcbackend.database.H2Database
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named
import com.uj.rcbackend.fileservicepart.pathrepopart.DataSetCallBack.Companion.ActionType


class DbBackedFilePathRepo @Inject constructor(
    @Named(IN_MEMORY_DB_LITEREAL)
     runtimeDbPack: @JvmSuppressWildcards Pair<H2Database,HikariDataSource>):IFilePathRepo {

    companion object{
        val logger= LogManager.getLogger()
    }

    private val runtimeDb=runtimeDbPack.first
    private val underlyingDataSource=runtimeDbPack.second
    private val listenerCallbacks = ConcurrentHashMap<DataSetListener, DataSetCallBack>()

    override fun initialize() {
        logger.debug(" REPO [${this.toString().takeLast(10)}] Initializing Db Backed Repo")
        runtimeDb.h2Queries.createIfNotExistsPathRepo()
    }

    override fun registerListener(listener: DataSetListener) {
    logger.debug(" REPO [${this.toString().takeLast(10)}] Registered listener:[$listener] of type: [${listener::class.simpleName}]")
        listenerCallbacks.put(listener, listener.provideCallBack(this))
    }

    override fun deregisterListener(listener: DataSetListener) {
        listenerCallbacks.remove(listener)
    }
    override fun get(): Collection<Path> {
       return runtimeDb.getAllPaths().also { logger.debug(" REPO [${this.toString().takeLast(10)}] Performing GETALL operation, retrieving: $it")}
    }

    override fun add(vararg path: Path): Boolean {
        val modified= mutableSetOf<Path>()
        for ( p in path) {
            kotlin.runCatching {
                logger.debug(" REPO [${this.toString().takeLast(10)}] Performing ADD operation for: $p")
                runtimeDb.h2Queries.insertIngnoringExistence(p.toString())
                modified.add(p)
            }.onFailure { logger.debug(" REPO [${this.toString().takeLast(10)}] Error occured in adding [$p]: ${it.stackTraceToString()}.") }
        }
        if(modified.isNotEmpty()){
            logger.debug(" REPO [${this.toString().takeLast(10)}] Adding next entries from repo: $modified")
            notifyCallBacks(modified,ActionType.ADDED)
        }
        return modified.isNotEmpty()
    }


    override fun remove(vararg path: Path): Boolean {
        val modified= mutableSetOf<Path>()
        for ( p in path) {
            kotlin.runCatching {
                logger.debug(" REPO [${this.toString().takeLast(10)}] Performing DELETE operation for: $p")
                runtimeDb.h2Queries.deletePath(p.toString())
                modified.add(p)
            }.onFailure {logger.debug( " REPO [${this.toString().takeLast(10)}] Error occured in removing [$p]: ${it.stackTraceToString()}.") }
        }
        if(modified.isNotEmpty()){
            logger.debug(" REPO [${this.toString().takeLast(10)}] Deleting next entries from repo: $modified")
            notifyCallBacks(modified,ActionType.DELETED)
        }
        return modified.isNotEmpty()
    }


    override fun iterator(): Iterator<Path> {
        return  runtimeDb.getAllPaths().also { logger.debug(" REPO [${this.toString().takeLast(10)}] Getting iterator for collection\n $it") }
            .iterator()
    }

    override fun terminate() {
        listenerCallbacks.clear()
        underlyingDataSource.close()
        logger.debug(" REPO [${this.toString().takeLast(10)}] Termination of datasource with url: [${underlyingDataSource.jdbcUrl}] performed ")
    }

    private fun H2Database.getAllPaths():Collection<Path>{
       return h2Queries.selectAllPaths()
            .executeAsList()
            .map { Paths.get(it) }
    }

    private fun notifyCallBacks(paths: Collection<Path>, datasetAction: ActionType) {
        for (callBack in listenerCallbacks.values) {
            logger.debug(" REPO [${this.toString().takeLast(10)}] Notifing  callback $callBack about ${datasetAction.name} for paths: $paths")
            callBack.notify(paths, datasetAction)
        }
    }
}