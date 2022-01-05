package com.uj.rcbackend.fileservicepart.pathrepopart

import java.nio.file.Path

interface IFilePathRepo:Iterable<Path> {
    fun initialize()
    fun registerListener(listener: DataSetListener)
    fun deregisterListener(listener: DataSetListener)
    fun get():Collection<Path>
    fun add(vararg path:Path):Boolean
    fun remove(vararg path: Path):Boolean
    /** Irreversably terminates this repo */
    fun terminate()

}