@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.signin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atMost
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.feature.signin.google.*
import ir.erfansn.nsmavpn.ui.util.preview.ThemeWithDevicesPreviews
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme

@Composable
fun SignInRoute(
    viewModel: SignInViewModel = viewModel(),
    windowSize: WindowSizeClass,
    showErrorMessage: (Int) -> Unit,
    navigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SignInScreen(
        uiState = uiState,
        windowSize = windowSize,
        showErrorMessage = showErrorMessage,
        verifyVpnGateSubscription = viewModel::verifyVpnGateSubscription,
        saveUserAccountInfo = viewModel::saveUserAccountInfo,
        navigateToHome = navigateToHome
    )
}

@Composable
fun SignInScreen(
    uiState: SignInUiState = SignInUiState.SignedOut,
    windowSize: WindowSizeClass,
    showErrorMessage: (Int) -> Unit,
    verifyVpnGateSubscription: (GoogleSignInAccount) -> Unit,
    saveUserAccountInfo: (GoogleSignInAccount) -> Unit,
    navigateToHome: () -> Unit,
) {
    val clientId = stringResource(id = R.string.web_client_id)
    val googleSignInState = rememberGoogleSignInState(clientId)

    when (uiState) {
        is SignInUiState.Error -> {
            showErrorMessage(uiState.messageId)
        }
        is SignInUiState.SignIn -> {
            saveUserAccountInfo(uiState.userAccount)
            navigateToHome()
        }
        SignInUiState.SignedOut -> {
            if (googleSignInState.isPermissionsGranted()) googleSignInState.signOut()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SignInContent(
            modifier = Modifier.padding(horizontal = 16.dp),
            windowSize = windowSize,
            onErrorReceive = showErrorMessage,
            verifyVpnGateSubscription = verifyVpnGateSubscription,
            googleSignInState = googleSignInState
        )
    }
}

@Composable
fun SignInContent(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
    onErrorReceive: (Int) -> Unit,
    verifyVpnGateSubscription: (GoogleSignInAccount) -> Unit,
    googleSignInState: GoogleSignInState,
) {
    val baseModifier = modifier then when {
        windowSize.widthSizeClass == WindowWidthSizeClass.Compact &&
                windowSize.heightSizeClass == WindowHeightSizeClass.Compact -> {
            Modifier.fillMaxSize()
        }
        windowSize.heightSizeClass == WindowHeightSizeClass.Compact -> {
            Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight()
        }
        else -> {
            Modifier
                .fillMaxHeight(0.7f)
                .fillMaxWidth()
        }
    }

    ConstraintLayout(modifier = baseModifier) {
        val guidelineFromTop50 = createGuidelineFromTop(0.5f)
        val guidelineFromStart50 = createGuidelineFromStart(0.5f)

        val (keyImage, signInLayout) = createRefs()

        Image(
            modifier = Modifier
                .constrainAs(keyImage) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)

                    if (windowSize.heightSizeClass == WindowHeightSizeClass.Compact) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(guidelineFromStart50, margin = 4.dp)
                    } else {
                        bottom.linkTo(guidelineFromTop50)
                        end.linkTo(parent.end)
                    }

                    val keyImageSize = 240.dp
                    width = Dimension.fillToConstraints.atMost(keyImageSize)
                    height = Dimension.fillToConstraints.atMost(keyImageSize)
                },
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary),
            painter = painterResource(id = R.drawable.ic_vpn_key),
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .constrainAs(signInLayout) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)

                    if (windowSize.heightSizeClass == WindowHeightSizeClass.Compact) {
                        top.linkTo(parent.top)
                        start.linkTo(guidelineFromStart50, margin = 4.dp)
                    } else {
                        top.linkTo(guidelineFromTop50)
                        start.linkTo(parent.start)
                    }

                    val signInLayoutMaxWidth = 320.dp
                    width = Dimension.fillToConstraints.atMost(signInLayoutMaxWidth)
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val vpnUsageDescriptionTextId =
                if (googleSignInState.signInIsIncomplete()) {
                    R.string.permission_description
                } else {
                    R.string.sign_in_description
                }
            Text(
                text = stringResource(id = vpnUsageDescriptionTextId),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoogleSignInButton(
                state = googleSignInState,
                onSignInSuccess = verifyVpnGateSubscription,
                onFailure = onErrorReceive
            )
            if (googleSignInState.signInIsIncomplete()) {
                Button(
                    onClick = { googleSignInState.signOut() }
                ) {
                    Text(text = stringResource(id = R.string.sign_out))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GoogleSignInButton(
    state: GoogleSignInState,
    onSignInSuccess: (GoogleSignInAccount) -> Unit = { },
    onFailure: (errorMessageId: Int) -> Unit = { },
) {
    var signInButtonTextId by remember {
        val initialState =
            if (state.signInIsIncomplete()) R.string.grand_permission else R.string.sign_in

        mutableStateOf(initialState)
    }
    LaunchedEffect(state.signInState) {
        signInButtonTextId = if (state.signInIsIncomplete()) {
            R.string.grand_permission
        } else {
            R.string.sign_in
        }
    }

    val signInIntent =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                val googleAccount = state.getSignedInAccountData(it.data)
                if (!state.isPermissionsGranted()) {
                    signInButtonTextId = R.string.grand_permission
                    return@rememberLauncherForActivityResult
                }

                onSignInSuccess(googleAccount)
            } catch (e: SignInFailedException) {
                onFailure(R.string.sign_in_failed)
            } catch (e: NoNetworkConnectionException) {
                onFailure(R.string.network_problem)
            } catch (e: Exception) {
                if (e !is SignInCancelledException) onFailure(R.string.unknown_error)
            }
        }

    if (state.signInState == SignInState.InProgress) {
        CircularProgressIndicator()
    } else {
        Button(
            onClick = { signInIntent.launch(state.prepareSignInIntent()) },
        ) {
            Text(text = stringResource(id = signInButtonTextId))
        }
    }
}

@ThemeWithDevicesPreviews
@Composable
private fun SignInScreenPreview() {
    NsmaVpnTheme {
        BoxWithConstraints {
            NsmaVpnBackground {
                SignInScreen(
                    uiState = SignInUiState.SignedOut,
                    showErrorMessage = { },
                    verifyVpnGateSubscription = { },
                    saveUserAccountInfo = { },
                    navigateToHome = { },
                    windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                )
            }
        }
    }
}
