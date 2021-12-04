package remotecontrolbackend.file_service_part.path_repo_part

import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.FileServiceScope
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import javax.inject.Inject

@FileServiceScope
class RuntimeFilePathRepo @Inject constructor() : IFilePathRepo {

    companion object {
        val logger = LogManager.getLogger()
    }

    private var _repository = ConcurrentHashMap.newKeySet<Path>()
    private val listenerCallbacks = ConcurrentHashMap<DataSetListener, DataSetCallBack>()


    override fun registerListener(listener: DataSetListener) {
        listenerCallbacks.put(listener, listener.provideCallBack())
    }

    override fun deregisterListener(listener: DataSetListener) {
        listenerCallbacks.remove(listener)
    }

    val repository: MutableSet<Path>
        get() = _repository

    constructor(initCollection: Collection<Path>) : this() {
        _repository = ConcurrentHashMap.newKeySet<Path> ().also { it.addAll(initCollection) }
    }

    //TODO Пропихнуть в  методы триггеринг каллбеков done?!
    override fun get(): Collection<Path> {
        return HashSet(repository)
    }

    /**Returns true if something in repository was changed */
    override fun add(vararg path: Path): Boolean {
        var changed = false
        val added = mutableSetOf<Path>()
        for (p in path) {
            if (repository.add(p)) {
                added.add(p)
                changed = true
            }
        }
        if (changed) {

            notifyCallBacks(added, DataSetCallBack.Companion.ActionType.ADDED)

            logger.debug("Added to repo: ${
                when(added.size>100) {
                   true-> {
                       added.toString().substring(0..100)+".....totaly:  ${added.size} elements"
                   }
                    false->{
                        added
                    }
                }
            }  ")
        }
        return changed
    }

    /**Returns true if something in repository was changed */
    override fun remove(vararg path: Path): Boolean {
        var changed = false
        val removed = mutableSetOf<Path>()
        for (p in path) {
            if (repository.remove(p)) {
                removed.add(p)
                changed = true
            }
        }
        if (changed) {
            notifyCallBacks(removed, DataSetCallBack.Companion.ActionType.DELETED)
            logger.debug("Removed from repo: ${
                        when(removed.size>100) {
                            true-> {
                                removed.toString().substring(0..100)+"....totaly: ${removed.size} elements"
                            }
                            false->{
                                removed
                            }
                        }
                    }")
        }
        return changed
    }

    private fun notifyCallBacks(paths: Collection<Path>, datasetAction: DataSetCallBack.Companion.ActionType) {
        for (callBack in listenerCallbacks.values) {
            callBack.notify(paths, datasetAction)
        }
    }
}