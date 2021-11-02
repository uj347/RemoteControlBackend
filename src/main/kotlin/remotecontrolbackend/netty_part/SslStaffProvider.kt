package remotecontrolbackend.netty_part

import io.netty.handler.ssl.ClientAuth
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import org.apache.logging.log4j.LogManager
import remotecontrolbackend.dagger.NettySSLModule.Companion.CLIENT_SSL_PATH_LITERAL
import remotecontrolbackend.dagger.NettySSLModule.Companion.SERVER_SSL_PATH_LITERAL
import remotecontrolbackend.dagger.NettySSLModule.Companion.SSL_DIRECTORY_LITERAL
import remotecontrolbackend.dagger.NettySSLModule.Companion.SSL_PATHS_MAP_LITERAL
import remotecontrolbackend.dagger.NettyScope
import java.io.File
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named

/**root//SSL/CACert.pem
 *         /SERVER/Cert.pem
 *                /privatekey.pem
 *         /CLIENT/Cert.pem
 *                /privatekey.pem
 * This is the structure, filenames could be ayything you want as long files could be readed as text
 *                */
@NettyScope
class NettySslContextProvider @Inject constructor(@Named(SSL_PATHS_MAP_LITERAL) val staffMap: Map<String, @JvmSuppressWildcards Path>) {
    companion object {
        val logger = LogManager.getLogger()
    }

   private  val sslDir: Path = staffMap.get(SSL_DIRECTORY_LITERAL)!!
    private val servSslDir: Path = staffMap.get(SERVER_SSL_PATH_LITERAL)!!
    private val clientSslDir: Path = staffMap.get(CLIENT_SSL_PATH_LITERAL)!!



    val serverSslContext: SslContext by lazy {
        val caCert: File = sslDir.extractCertPem() ?: throw RuntimeException("No CA Certificate in root/SSL directory ")
        val serverKey: File =
            servSslDir.extractPrivateKeyPem() ?: throw RuntimeException("No server private key in ssl/SERVER directory")
        val serverCert: File =
            servSslDir.extractCertPem() ?: throw RuntimeException("No server certificate in directory ssl/SERVER")
        SslContextBuilder
            .forServer(
                serverCert,
                serverKey,
            )
            .trustManager(caCert)
            .clientAuth(ClientAuth.REQUIRE)
            .build()
    }

    val clientSslContext:SslContext by lazy{
        val caCert: File = sslDir.extractCertPem() ?: throw RuntimeException("No CA Certificate in root/SSL directory ")
        val clientKey: File =
            clientSslDir.extractPrivateKeyPem() ?: throw RuntimeException("No client private key in directory ssl/CLIENT ")
        val clientCert: File =
            clientSslDir.extractCertPem() ?: throw RuntimeException("No client certificate in directory ssl/CLIENT")
        SslContextBuilder
            .forClient()
            .keyManager(
                clientCert,
                clientKey
            )
            .trustManager(caCert)
            .build()
    }


}


fun File.isPemCertFile(): Boolean {
    return this.readLines().filter { it.lowercase().contains("certificate") }.count() > 0
}


fun File.isPemPrivateKeyFile(): Boolean {
    return this.readLines().filter { it.lowercase().contains("private key") }.count() > 0
}

fun Path.extractPrivateKeyPem(): File? {
    Files.newDirectoryStream(this).use {
        return it.find { it.toFile().isPemPrivateKeyFile() }!!.toFile()
    }
}

fun Path.extractCertPem(): File? {
    Files.newDirectoryStream(this).use {
        return it.find { it.toFile().isPemCertFile() }?.toFile()
    }
}
