package remotecontrolbackend.file_service_part.path_repo_part

import java.nio.file.Path

interface DataSetCallBack {
companion object{
    enum class ActionType{MODIFIED,DELETED,ADDED }
}
    fun notify(paths: Collection<Path>, actionType:ActionType)
}