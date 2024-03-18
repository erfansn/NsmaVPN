package ir.erfansn.nsmavpn.data.source.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object VpnServersSerializer : Serializer<VpnServers> {

    override val defaultValue: VpnServers = VpnServers.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): VpnServers {
        try {
            return VpnServers.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: VpnServers, output: OutputStream) {
        t.writeTo(output)
    }
}
