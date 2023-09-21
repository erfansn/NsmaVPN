package ir.erfansn.nsmavpn.data.source.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object LastVpnConnectionSerializer : Serializer<LastVpnConnection> {

    override val defaultValue: LastVpnConnection = LastVpnConnection.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): LastVpnConnection {
        try {
            return LastVpnConnection.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: LastVpnConnection, output: OutputStream) {
        t.writeTo(output)
    }
}
