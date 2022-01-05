package com.uj.rcbackend.fileservicepart.pathrepopart

interface DataSetListener {
    fun provideCallBack(repoToListen:IFilePathRepo):DataSetCallBack
}