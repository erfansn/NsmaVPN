package ir.erfansn.nsmavpn.data.source.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object VpnProviderSerializer : Serializer<VpnProvider> {

    override val defaultValue: VpnProvider = VpnProvider.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): VpnProvider {
        try {
            return VpnProvider.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: VpnProvider, output: OutputStream) {
        t.writeTo(output)
    }
}
