package ir.erfansn.nsmavpn.feature.home.vpn.protocol.terminal

import androidx.documentfile.provider.DocumentFile
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.*
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ClientBridge
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.ControlMessage
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Result
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.client.Where
import ir.erfansn.nsmavpn.feature.home.vpn.protocol.unit.DataUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedInputStream
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

const val SSL_REQUEST_INTERVAL = 10_000L

class SSLTerminal(private val bridge: ClientBridge) {
    private val mutex = Mutex()
    private var socket: SSLSocket? = null
    private var jobInitialize: Job? = null

    private val selectedVersion = _SSL_VERSION
    private val enabledSuites = _SSL_SUITES

    suspend fun initializeSocket() {
        jobInitialize = bridge.service.scope.launch(bridge.handler) {
            if (!createSocket()) return@launch

            if (!establishHttpLayer()) return@launch

            bridge.controlMailbox.send(ControlMessage(Where.SSL, Result.PROCEEDED))
        }
    }

    private fun createTrustManagers(): Array<TrustManager> {
        val document = DocumentFile.fromTreeUri(
            bridge.service,
            _SSL_CERT_DIR!!
        )!!

        val certFactory = CertificateFactory.getInstance("X.509")
        val keyStore = KeyStore.getDefaultType().let {
            KeyStore.getInstance(it)
        }
        keyStore.load(null, null)

        for (file in document.listFiles()) {
            if (file.isFile) {
                val stream =
                    BufferedInputStream(bridge.service.contentResolver.openInputStream(file.uri))
                val ca = certFactory.generateCertificate(stream) as X509Certificate
                keyStore.setCertificateEntry(file.name, ca)
                stream.close()
            }
        }

        val tmFactory = TrustManagerFactory.getDefaultAlgorithm().let {
            TrustManagerFactory.getInstance(it)
        }
        tmFactory.init(keyStore)

        return tmFactory.trustManagers
    }

    private suspend fun createSocket(): Boolean {
        val socketFactory = if (_SSL_DO_ADD_CERT) {
            val context = SSLContext.getInstance(selectedVersion) as SSLContext
            context.init(null, createTrustManagers(), null)
            context.socketFactory
        } else {
            SSLSocketFactory.getDefault()
        }

        socket = socketFactory.createSocket(
            bridge.HOME_HOSTNAME,
            _SSL_PORT
        ) as SSLSocket

        if (selectedVersion != "DEFAULT") {
            socket!!.enabledProtocols = arrayOf(selectedVersion)
        }

        if (_SSL_DO_SELECT_SUITES) {
            val sortedSuites = socket!!.supportedCipherSuites.filter {
                enabledSuites.contains(it)
            }

            socket!!.enabledCipherSuites = sortedSuites.toTypedArray()
        }

        if (_SSL_DO_VERIFY) {
            HttpsURLConnection.getDefaultHostnameVerifier().also {
                if (!it.verify(bridge.HOME_HOSTNAME, socket!!.session)) {
                    bridge.controlMailbox.send(ControlMessage(Where.SSL, Result.ERR_VERIFICATION_FAILED))
                    return false
                }
            }
        }

        socket!!.startHandshake()

        return true
    }

    private suspend fun establishHttpLayer(): Boolean {
        val httpDelimiter = "\r\n"
        val httpSuffix = "\r\n\r\n"

        val request = arrayOf(
            "SSTP_DUPLEX_POST /sra_{BA195980-CD49-458b-9E23-C84EE0ADCD75}/ HTTP/1.1",
            "Content-Length: 18446744073709551615",
            "Host: ${bridge.HOME_HOSTNAME}",
            "SSTPCORRELATIONID: {${bridge.guid}}"
        ).joinToString(separator = httpDelimiter, postfix = httpSuffix).toByteArray(Charsets.US_ASCII)

        socket!!.outputStream.write(request)
        socket!!.outputStream.flush()


        var response = ""
        while (true) {
            response += socket!!.inputStream.read().toChar() // don't use buffer. It could read too much.

            if (response.endsWith(httpSuffix)) {
                break
            }
        }

        if (!response.split(httpDelimiter)[0].contains("200")) {
            bridge.controlMailbox.send(ControlMessage(Where.SSL, Result.ERR_UNEXPECTED_MESSAGE))
            return false
        }


        socket!!.soTimeout = 1_000
        bridge.service.protect(socket)
        return true
    }

    fun getSession(): SSLSession {
        return socket!!.session
    }

    fun getServerCertificate(): ByteArray {
        return socket!!.session.peerCertificates[0].encoded
    }

    fun receive(maxRead: Int, buffer: ByteBuffer) {
        try {
            val readSize = socket!!.inputStream.read(buffer.array(), buffer.limit(), maxRead)
            buffer.limit(buffer.limit() + readSize)
        } catch (_: SocketTimeoutException) { }
    }

    suspend fun send(buffer: ByteBuffer) {
        mutex.withLock {
            socket!!.outputStream.write(buffer.array(), 0, buffer.limit())
            socket!!.outputStream.flush()
        }
    }

    suspend fun sendDataUnit(unit: DataUnit) {
        ByteBuffer.allocate(unit.length).also {
            unit.write(it)
            it.flip()
            send(it)
        }
    }

    fun close() {
        jobInitialize?.cancel()
        socket?.close()
    }
}
