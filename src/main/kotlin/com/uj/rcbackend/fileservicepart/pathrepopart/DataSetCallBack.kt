package com.uj.rcbackend.fileservicepart.pathrepopart

import java.nio.file.Path

interface DataSetCallBack {
companion object{
    enum class ActionType{MODIFIED,DELETED,ADDED }
}
    fun notify(paths: Collection<Path>, actionType:ActionType)
}