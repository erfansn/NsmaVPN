package ir.erfansn.nsmavpn.data.source.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object VpnServiceStateSerializer : Serializer<VpnServiceState> {

    override val defaultValue: VpnServiceState = VpnServiceState.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): VpnServiceState {
        try {
            return VpnServiceState.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: VpnServiceState, output: OutputStream) {
        t.writeTo(output)
    }
}
