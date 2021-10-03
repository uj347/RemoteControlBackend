import io.netty.handler.codec.http.*
import org.junit.Test
import remotecontrolbackend.netty_part.auth_part.asciiToBase64
import remotecontrolbackend.netty_part.auth_part.base64ToAscii

import kotlin.test.assertEquals

class Tests {
    @Test fun baseWorking(){
        val testString="Test string"
        val baseEncoded=testString.asciiToBase64()
        assertEquals(testString,baseEncoded.base64ToAscii(),"Nope base 64 doesn't work")
    }
    @Test fun prefixWorking(){
        val testString="basic Alladin password"
        assertEquals(testString.removePrefix("basic "), "Alladin password", "Nope prefix works not as expected")
    }
    @Test fun checkSetContentLengthWorks(){
        val long:Long=347
        val response=DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,false)
            .apply { HttpUtil.setContentLength(this,long) }
        assert(response.headers().get(HttpHeaderNames.CONTENT_LENGTH)!=null)
        assertEquals(response.headers().get(HttpHeaderNames.CONTENT_LENGTH).toLong(),347)
    }
}