package ir.erfansn.nsmavpn.feature.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.repository.ServersRepository
import ir.erfansn.nsmavpn.data.repository.VpnServersNotExistsException
import ir.erfansn.nsmavpn.data.source.local.datastore.Server
import ir.erfansn.nsmavpn.data.util.PingChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.apache.xpath.operations.Bool
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val serversRepository: ServersRepository,
    private val pingChecker: PingChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(5000)
            _uiState.update {
                it.copy(isCollectingServers = false)
            }
        }
    }

    fun prepareNewServer() {
        blockCurrentServer()
        findFastestServer()
    }

    private fun blockCurrentServer() {

    }

    // TODO: only one request can handle thus when new call will cancel job and start one new
    fun findFastestServer() {

    }

    /*private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Disconnected)
    val uiState = serversRepository.isCollectingVpnServers()
        .combine(_uiState) { isCollecting, currentUiState ->
            if (isCollecting) HomeUiState.CollectingServers else currentUiState
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = _uiState.value
        )

    private var currentVpnServer: Server? = null

    init {
        serversRepository.beginVpnServersWorker()
    }

    fun findBestVpnServer() {
        viewModelScope.launch {
            _uiState.update {
                try {
                    val fastestServer = serversRepository.getFastestVpnServer().also {
                        currentVpnServer = it
                    }
                    HomeUiState.Connecting(vpnServer = fastestServer)
                } catch (e: VpnServersNotExistsException) {
                    HomeUiState.Error(R.string.error_no_exists_server)
                }
            }
        }
    }

    fun checkConnectionEstablishment() {
        viewModelScope.launch {
            val connectionIsEstablish = pingChecker.isReachable()
            if (connectionIsEstablish) {
                _uiState.update { HomeUiState.Connected }
            } else {
                // Block selected server and trigger to select another
                currentVpnServer?.let { serversRepository.blockVpnServer(it) }
                findBestVpnServer()
            }
        }
    }*/
}

/*sealed interface HomeUiState {
    object Disconnected : HomeUiState
    data class Connecting(val vpnServer: Server) : HomeUiState
    object Connected : HomeUiState
    object CollectingServers : HomeUiState
    data class Error(val messageId: Int) : HomeUiState
}*/

@Immutable
data class HomeUiState(
    val vpnServer: Server? = null,
    val isCollectingServers: Boolean = true,
)
