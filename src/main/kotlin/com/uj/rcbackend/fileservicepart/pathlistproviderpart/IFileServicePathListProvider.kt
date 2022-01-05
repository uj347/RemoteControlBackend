package com.uj.rcbackend.fileservicepart.pathlistproviderpart

import java.nio.file.Path
import javax.inject.Provider

fun interface IFileServicePathListProvider:Provider<Collection<Path>> {

     override fun get():Collection<Path>
}