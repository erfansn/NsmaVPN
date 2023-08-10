package ir.erfansn.nsmavpn.feature.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.erfansn.nsmavpn.data.repository.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState = profileRepository.userProfile.map {
        ProfileUiState(
            avatarUrl = it.avatarUrl,
            emailAddress = it.emailAddress,
            displayName = it.displayName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )
}

@Immutable
data class ProfileUiState(
    val avatarUrl: String? = null,
    val emailAddress: String = "",
    val displayName: String = ""
) {
    val isInfoLoaded: Boolean get() = emailAddress.isNotEmpty() && displayName.isNotEmpty()
}
