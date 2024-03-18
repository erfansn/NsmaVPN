package ir.erfansn.nsmavpn.data.source.local

import android.graphics.drawable.ColorDrawable
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.google.common.truth.Truth.assertThat
import ir.erfansn.nsmavpn.data.model.AppInfo
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferences
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferencesSerializer
import ir.erfansn.nsmavpn.data.source.local.datastore.copy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DefaultUserPreferencesLocalDataSourceTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testScope = TestScope()
    private val testDataStore: DataStore<UserPreferences> = DataStoreFactory.create(
        serializer = UserPreferencesSerializer,
        scope = testScope,
        produceFile = { temporaryFolder.newFile("test_user_preferences") }
    )
    
    private val defaultUserPreferencesLocalDataSource = DefaultUserPreferencesLocalDataSource(
        dataStore = testDataStore
    )

    @Test
    fun doesNotAddAnythingToSplitTunnelingList_whenPassingAEmptyList() = testScope.runTest {
        val splitTunnelingApps = defaultUserPreferencesLocalDataSource.userPreferences.first().splitTunnelingAppIdList

        defaultUserPreferencesLocalDataSource.addAppsToSplitTunnelingList(emptyList())

        val currentSplitTunnelingApps = defaultUserPreferencesLocalDataSource.userPreferences.first().splitTunnelingAppIdList
        assertThat(currentSplitTunnelingApps).isEqualTo(splitTunnelingApps)
    }

    @Test
    fun addsAppsToSplitTunnelingList_whenPassingAppsToIt() = testScope.runTest {
        val appsId = listOf(
            "com.example.fake",
            "com.example.fake2",
        )
        val appsInfo = appsId.map {
            AppInfo(id = it, name = "", icon = ColorDrawable(0))
        }

        defaultUserPreferencesLocalDataSource.addAppsToSplitTunnelingList(appsInfo)

        val splitTunnelingApps = defaultUserPreferencesLocalDataSource.userPreferences.first().splitTunnelingAppIdList
        assertThat(splitTunnelingApps).isEqualTo(appsId)
    }
    
    @Test
    fun doesNothingOnSplitTunnelingAppsList_whenPassingNonExistingApp() = testScope.runTest {
        val splitTunnelingApps = defaultUserPreferencesLocalDataSource.userPreferences.first().splitTunnelingAppIdList

        defaultUserPreferencesLocalDataSource.removeAppFromSplitTunnelingList(
            app = AppInfo(
                id = "",
                name = "",
                icon = ColorDrawable(0)
            )
        )

        val currentSplitTunnelingApps = defaultUserPreferencesLocalDataSource.userPreferences.first().splitTunnelingAppIdList
        assertThat(currentSplitTunnelingApps).isEqualTo(splitTunnelingApps)
    }

    @Test
    fun removesSpecifiedAppFromSplitTunnelingList_whenPassingExistingApp() = testScope.runTest {
        val fakeAppId = "com.example.fake"
        testDataStore.updateData {
            it.copy {
                splitTunnelingAppId += fakeAppId
            }
        }
        val splitTunnelingApps = defaultUserPreferencesLocalDataSource.userPreferences.first().splitTunnelingAppIdList

        defaultUserPreferencesLocalDataSource.removeAppFromSplitTunnelingList(
            app = AppInfo(
                id = fakeAppId,
                name = "",
                icon = ColorDrawable(0)
            )
        )

        val currentSplitTunnelingApps = defaultUserPreferencesLocalDataSource.userPreferences.first().splitTunnelingAppIdList
        assertThat(currentSplitTunnelingApps).isNotEqualTo(splitTunnelingApps)
        assertThat(currentSplitTunnelingApps).doesNotContain(fakeAppId)
    }
}
