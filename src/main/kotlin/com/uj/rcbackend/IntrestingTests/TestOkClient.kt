package com.uj.rcbackend.IntrestingTests

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import nl.altindag.ssl.SSLFactory
import nl.altindag.ssl.util.PemUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.*
import java.io.IOException
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.net.ssl.*


fun main() {
    val liveBody = rqBody()


    val workerThread = Thread {
      val trustMaterial=PemUtils.loadTrustMaterial(Paths.get("J:\\sslMagick\\ca\\cacert.pem"))
        val identityMaterial=PemUtils.loadIdentityMaterial(Paths.get("J:\\sslMagick\\client\\clientcert.pem"),Paths.get("J:\\sslMagick\\client\\clientkey.pem"))
//        val identityMaterial=PemUtils.

       val sslFactory=SSLFactory.builder()
           .withTrustMaterial(trustMaterial)
       .withIdentityMaterial(identityMaterial)
           .withProtocols("TLSv1.3", "TLSv1.2")
           .withCiphers("TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256", "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_256_GCM_SHA384", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA")
//           .withNeedClientAuthentication()
           .build()



        val client = OkHttpClient.Builder().protocols(listOf(Protocol.HTTP_1_1))
            .connectionSpecs(arrayListOf( ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.MODERN_TLS))

            .sslSocketFactory(sslFactory.sslSocketFactory,sslFactory.trustManager.orElseThrow())
            .hostnameVerifier(object:HostnameVerifier {
                override fun verify(hostname: String?, session: SSLSession?): Boolean {
                    return true
                }
            })
            .build()


        val request = Request.Builder().post(liveBody).url("https://127.0.0.1:34747/robot")
            .addHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
            .build()
        client.newCall(request).enqueue(object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                println("Result.Failure")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                println("Failure")
            }
        })
        println("Clientcall executed")
    }

    val inputThread = Thread {
        val scanner = Scanner(System.`in`)
        while (true) {
            val next = scanner.next().also { println("next from scanner: $it") }
            if (next.lowercase() == "end") {

                liveBody.pipeSink.close()
                liveBody.completeFlag.complete(Unit)
                println("end of input")
                break
            }
            liveBody.pipeSink.write((next+"\n").encodeToByteArray()).emit()
        }
    }


    inputThread.start()
    workerThread.start()
    inputThread.join()
    workerThread.join()

}


class rqBody : RequestBody() {

    private val pipe = Pipe(256)
    val pipeSink
        get() = pipe.sink.buffer()
    val completeFlag=CompletableFuture<Unit>()

    override fun writeTo(sink: BufferedSink) {
        sink.flush()
       val pipeSource=pipe.source.buffer()
        while (!completeFlag.isDone) {
            pipeSource.readUtf8Line()?.let{
                it.let { sink.writeUtf8(it) }.flush()
            }

        }
        while(!pipeSource.exhausted()){
            pipeSource.readUtf8Line()?.let{
                it.let { sink.writeUtf8(it) }.flush()
            }
            pipeSource.close()

        }




    }


    override fun contentType(): MediaType? {
        return "application/json; charset=utf-8".toMediaType()
    }
}

fun BufferedSink.writeRoboCommand() {
    val moshi = Moshi.Builder().build()
    val comAdapter = moshi.adapter<Array<Array<String>>>(Types.arrayOf(Types.arrayOf(String::class.java)))
    val arrCapsCommand = comAdapter.toJson(arrayOf(arrayOf("keyPress", "20")))
    writeUtf8(arrCapsCommand)
}

