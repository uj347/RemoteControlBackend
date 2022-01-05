package com.uj.rcbackend.fileservicepart

import java.nio.file.Path
import kotlin.io.path.isDirectory

fun Collection<Path>.findTopLevelNodes(
    target: MutableCollection<Path> = mutableSetOf(),
    returnDirsOnly: Boolean = false
): Collection<Path> {
      val hasParentsInTargetSet = { p: Path ->
        var result = false
        target.forEach { resultDirPath ->
            if (p != resultDirPath && p.startsWith(resultDirPath)) {
                result = true
            }
        }
        result
    }

    val hasParentsInSourceSet = { p: Path ->
        var result = false
        forEach { sourcePath ->
            if (p != sourcePath && p.startsWith(sourcePath)) {
                result = true
            }
        }
        result
    }
    val resultSet = HashSet<Path>()
    this.filter {
        if (returnDirsOnly) {
            it.isDirectory()
        } else {
            true
        }
    }
        .filterNot(hasParentsInTargetSet)
        .filterNot(hasParentsInSourceSet)
        .forEach {
            resultSet.add(it)
        }
    return resultSet
}