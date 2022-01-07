package com.uj.rcbackend.IntrestingTests


fun main(){
    System.getProperty("user.dir").let {
        println("User dir is : $it")
    }
    System.getenv().let{
        for(entry in it.entries){
            println(entry)
        }
    }
}