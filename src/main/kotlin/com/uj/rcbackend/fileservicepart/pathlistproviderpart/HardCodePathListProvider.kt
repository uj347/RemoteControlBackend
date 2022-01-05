package com.uj.rcbackend.fileservicepart.pathlistproviderpart

import java.nio.file.Path

class HardCodePathListProvider (val initialPaths:Collection<Path>):IFileServicePathListProvider {
    override fun get(): Collection<Path> {
        return initialPaths
    }
}