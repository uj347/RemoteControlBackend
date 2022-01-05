package com.uj.rcbackend.nettypart.utils

import java.lang.RuntimeException

sealed class HttpException(message:String):RuntimeException(message)
class Exception404(message:String):HttpException(message)
class Exception500(message:String):HttpException(message)
