package ir.erfansn.nsmavpn.core.application

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.repository.UserProfileRepository
import ir.erfansn.nsmavpn.data.util.NetworkMonitor
import ir.erfansn.nsmavpn.navigation.NsmaVpnNavHost
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.theme.isSupportDynamicScheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var networkMonitor: NetworkMonitor

    @Inject lateinit var userProfileRepository: UserProfileRepository

    private val viewModel by viewModels<MainActivityViewModel>()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .collect {
                        uiState = it
                    }
            }
        }
        splash.setKeepOnScreenCondition {
            uiState == MainActivityUiState.Loading
        }

        enableEdgeToEdge()

        setContent {
            shouldUseDarkTheme(uiState).let { useDarkTheme ->
                LaunchedEffect(useDarkTheme) {
                    val transparentStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                        detectDarkMode = { useDarkTheme }
                    )
                    enableEdgeToEdge(
                        statusBarStyle = transparentStyle,
                        navigationBarStyle = transparentStyle
                    )
                }
            }
            NsmaVpnTheme(
                darkTheme = shouldUseDarkTheme(uiState),
                dynamicColor = shouldUseDynamicColor(uiState)
            ) {
                NsmaVpnApp(
                    onResetApp = viewModel::resetApp,
                    networkMonitor = networkMonitor,
                    windowSize = calculateWindowSizeClass(activity = this),
                    isCompletedAuthFlow = userProfileRepository::isUserProfileSaved,
                )
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun shouldUseDarkTheme(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        MainActivityUiState.Loading -> isSystemInDarkTheme()
        is MainActivityUiState.Success -> when (uiState.themeMode) {
            Configurations.ThemeMode.System -> isSystemInDarkTheme()
            Configurations.ThemeMode.Light -> false
            Configurations.ThemeMode.Dark -> true
        }
    }
}

@Composable
@ReadOnlyComposable
private fun shouldUseDynamicColor(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        MainActivityUiState.Loading -> isSupportDynamicScheme()
        is MainActivityUiState.Success -> uiState.isEnableDynamicScheme
    }
}

@Composable
fun NsmaVpnApp(
    onResetApp: () -> Unit,
    networkMonitor: NetworkMonitor,
    windowSize: WindowSizeClass,
    isCompletedAuthFlow: suspend () -> Boolean,
) {
    NsmaVpnBackground {
        NsmaVpnNavHost(
            networkMonitor = networkMonitor,
            windowSize = windowSize,
            onResetApp = onResetApp,
            isCompletedAuthFlow = isCompletedAuthFlow,
        )
    }
}
