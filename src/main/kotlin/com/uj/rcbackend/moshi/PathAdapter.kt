package com.uj.rcbackend.moshi

import com.squareup.moshi.*
import java.nio.file.Path
import java.nio.file.Paths

class PathAdapter{
    @ToJson
    fun pathToString(path:Path):String{
        return path.toString()
    }


    @FromJson
    fun stringToPath(string: String):Path{
        return Paths.get(string)
    }



}