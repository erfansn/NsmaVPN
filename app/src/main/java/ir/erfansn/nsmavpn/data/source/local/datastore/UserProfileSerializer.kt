package ir.erfansn.nsmavpn.data.source.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object UserProfileSerializer : Serializer<UserProfile> {

    override val defaultValue: UserProfile = UserProfile.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserProfile {
        try {
            return UserProfile.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: UserProfile, output: OutputStream) {
        t.writeTo(output)
    }
}