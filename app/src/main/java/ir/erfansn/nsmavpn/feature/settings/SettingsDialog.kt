package ir.erfansn.nsmavpn.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.erfansn.nsmavpn.BuildConfig
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.ui.ThemePreviews
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme

@Composable
fun SettingsDialog(
    viewModel: SettingViewModel = viewModel(),
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsDialog(
        uiState = uiState,
        onDismiss = onDismiss,
        onChangeThemeMode = viewModel::updateThemeMode
    )
}

@Composable
private fun SettingsDialog(
    uiState: SettingsUiState,
    onDismiss: () -> Unit,
    onChangeThemeMode: (ThemeMode) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.settings))
        },
        text = {
            Divider()
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .wrapContentSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.theme_mode),
                    style = MaterialTheme.typography.titleMedium,
                )
                Column(modifier = Modifier.selectableGroup()) {
                    SelectiveRow(
                        text = stringResource(R.string.theme_mode_default),
                        selected = uiState.themeMode == ThemeMode.SYSTEM,
                        onClick = { onChangeThemeMode(ThemeMode.SYSTEM) }
                    )
                    SelectiveRow(
                        text = stringResource(R.string.theme_mode_light),
                        selected = uiState.themeMode == ThemeMode.LIGHT,
                        onClick = { onChangeThemeMode(ThemeMode.LIGHT) }
                    )
                    SelectiveRow(
                        text = stringResource(R.string.theme_mode_dark),
                        selected = uiState.themeMode == ThemeMode.DARK,
                        onClick = { onChangeThemeMode(ThemeMode.DARK) }
                    )
                }
                Divider()
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp),
                    text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}

@Composable
private fun SelectiveRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@ThemePreviews
@Composable
private fun SettingsDialogPreview() {
    NsmaVpnTheme {
        SettingsDialog(
            uiState = SettingsUiState(),
            onDismiss = { },
            onChangeThemeMode = { }
        )
    }
}
