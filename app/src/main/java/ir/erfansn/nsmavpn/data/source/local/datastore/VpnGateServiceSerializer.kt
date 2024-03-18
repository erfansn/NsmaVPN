package ir.erfansn.nsmavpn.data.source.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object VpnGateServiceSerializer : Serializer<VpnGateService> {

    override val defaultValue: VpnGateService = vpnGateService {
        mirrorLink = "https://vpngate.net"
    }

    override suspend fun readFrom(input: InputStream): VpnGateService {
        try {
            return VpnGateService.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: VpnGateService, output: OutputStream) {
        t.writeTo(output)
    }
}
