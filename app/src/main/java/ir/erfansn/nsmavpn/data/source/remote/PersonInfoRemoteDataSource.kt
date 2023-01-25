package ir.erfansn.nsmavpn.data.source.remote

import com.google.api.services.people.v1.model.Person
import ir.erfansn.nsmavpn.data.source.remote.api.PeopleApi
import ir.erfansn.nsmavpn.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultPersonInfoRemoteDataSource @Inject constructor(
    private val api: PeopleApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PersonInfoRemoteDataSource {

    override suspend fun fetchPublicInfo(emailAddress: String): Person = withContext(ioDispatcher) {
        api.selectAccount(emailAddress)
            .people()
            .get("people/me")
            .setPersonFields("photos,names,emailAddresses")
            .execute()
    }
}

interface PersonInfoRemoteDataSource {
    suspend fun fetchPublicInfo(emailAddress: String): Person
}
