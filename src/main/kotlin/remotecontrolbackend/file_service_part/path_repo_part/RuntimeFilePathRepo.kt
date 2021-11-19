package remotecontrolbackend.file_service_part.path_repo_part

import remotecontrolbackend.dagger.FileServiceScope
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject

@FileServiceScope
class RuntimeFilePathRepo @Inject constructor():IFilePathRepo{
    private var _repository=CopyOnWriteArraySet<Path> ()

    val repository:CopyOnWriteArraySet<Path>
    get()=_repository

    constructor(initCollection:Collection<Path>) : this() {
        _repository=CopyOnWriteArraySet<Path> (initCollection)
    }


    override fun get(): Collection<Path> {
        return HashSet(repository)
    }

    /**Returns true if something in repository was changed */
    override fun add(vararg path: Path): Boolean {
    var changed=false
    for(p in path) {
        if( repository.add(p) ){
            changed=true
        }
       }
    return changed
    }

    /**Returns true if something in repository was changed */
    override fun remove(vararg path: Path): Boolean {
            var changed=false
            for(p in path) {
                if( repository.remove(p) ){
                    changed=true
                }
            }
            return changed
    }
}