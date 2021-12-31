package remotecontrolbackend.netty_part.chunked_part.file_handler_part


import io.netty.handler.codec.http.*
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.isDirectory
import remotecontrolbackend.netty_part.chunked_part.file_handler_part.DispatchResult.Companion.FileDirective as FDirective
fun constructChunkedFileResponse(paths: Collection<Path>,):HttpResponse{
    val multiplePaths:Boolean=paths.count()>1
    val isDir:Boolean=when(multiplePaths){
        true->false
        false->paths.first().isDirectory()
    }
    val contentType:String=if(isDir||multiplePaths){
        "application/zip"}else{
        "application/octet-stream"
    }
    val filename:String=if(!multiplePaths && isDir){
        "${paths.first().fileName}.zip"
    }else{
        "pack${LocalDateTime.now()}.zip"
    }

    val result= DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK).also {
        it.headers().add(HttpHeaderNames.CONTENT_DISPOSITION,"attachment; filename=$filename")
       it.headers().add(HttpHeaderNames.TRANSFER_ENCODING,HttpHeaderValues.CHUNKED)
        it.headers().add(HttpHeaderNames.CONTENT_TYPE,contentType)
    }
    return result
}


sealed interface DispatchResult {
    companion object{
        const val AVAILABLEFILES_LITERAL="availablefiles"
        const val  BODY_LISTED_FILES_LITERAL="bodyfiles"

        enum class FileDirective(val compatMethods:Iterable<HttpMethod>,val pathLiteralRestriction:((String)->Boolean)){
            AVAILABLEFILES(setOf(HttpMethod.GET), { it.lowercase()== AVAILABLEFILES_LITERAL }),
            POSTFILE(setOf(HttpMethod.POST, HttpMethod.PUT),{true}),
            GETLISTEDFILES(setOf(HttpMethod.GET), { it.lowercase() == BODY_LISTED_FILES_LITERAL }),
            NOTCONSISTENT(emptySet(),{false});
        }
    }
    val fileDirective:FileDirective
    val isBodyProcessingNeeded:Boolean
}

class AvailableFilesDispatch :DispatchResult{
    override val fileDirective: DispatchResult.Companion.FileDirective
        get() = FDirective.AVAILABLEFILES
    override val isBodyProcessingNeeded
        get() = false
}
class PostFileDispatch(val fileName:String):DispatchResult{
    override val fileDirective: DispatchResult.Companion.FileDirective
        get() = FDirective.POSTFILE
    override val isBodyProcessingNeeded: Boolean
        get() = true
}
class GetListedFilesDispatch:DispatchResult{
    override val fileDirective: DispatchResult.Companion.FileDirective
        get() = FDirective.GETLISTEDFILES
    override val isBodyProcessingNeeded: Boolean
        get() = true
}

class NotConsistentDispatch(val reason:String):DispatchResult{
    override val fileDirective: DispatchResult.Companion.FileDirective
        get() = FDirective.GETLISTEDFILES
    override val isBodyProcessingNeeded: Boolean
        get() = false
}

