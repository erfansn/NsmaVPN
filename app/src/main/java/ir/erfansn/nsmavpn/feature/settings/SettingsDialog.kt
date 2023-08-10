package ir.erfansn.nsmavpn.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.erfansn.nsmavpn.BuildConfig
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.ThemePreviews

@Composable
fun SettingsRoute(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsDialog(
        modifier = modifier,
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
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.title_settings))
        },
        text = {
            Divider()
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .wrapContentSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeModeOptions(
                    themeMode = uiState.themeMode,
                    onChangeThemeMode = onChangeThemeMode
                )
                Divider()
                LinksPanel(
                    modifier = Modifier.align(CenterHorizontally)
                )
                Text(
                    modifier = Modifier.align(CenterHorizontally),
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
private fun ColumnScope.ThemeModeOptions(
    themeMode: ThemeMode,
    onChangeThemeMode: (ThemeMode) -> Unit,
) {
    Text(
        text = stringResource(R.string.theme_mode),
        style = MaterialTheme.typography.titleMedium,
    )
    ThemeModeConfig(
        themeMode = themeMode,
        onChangeThemeMode = onChangeThemeMode
    )
}

@Composable
private fun ThemeModeConfig(
    themeMode: ThemeMode,
    onChangeThemeMode: (mode: ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.selectableGroup()) {
        SelectiveRow(
            text = stringResource(R.string.theme_mode_default),
            selected = themeMode == ThemeMode.SYSTEM,
            onSelect = { onChangeThemeMode(ThemeMode.SYSTEM) }
        )
        SelectiveRow(
            text = stringResource(R.string.theme_mode_light),
            selected = themeMode == ThemeMode.LIGHT,
            onSelect = { onChangeThemeMode(ThemeMode.LIGHT) }
        )
        SelectiveRow(
            text = stringResource(R.string.theme_mode_dark),
            selected = themeMode == ThemeMode.DARK,
            onSelect = { onChangeThemeMode(ThemeMode.DARK) }
        )
    }
}

@Composable
private fun SelectiveRow(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
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

@Composable
private fun LinksPanel(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextLink(
            text = stringResource(R.string.feedback),
            url = stringResource(R.string.link_feedback)
        )
        TextLink(
            text = stringResource(R.string.licences),
            url = stringResource(R.string.link_licences)
        )
    }
}

@Composable
private fun TextLink(text: String, url: String) {
    val launchResourceIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val context = LocalContext.current

    ClickableText(
        text = AnnotatedString(
            text = text,
            spanStyle = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
            )
        ),
        style = MaterialTheme.typography.labelLarge,
        onClick = {
            ActivityCompat.startActivity(context, launchResourceIntent, null)
        }
    )
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
