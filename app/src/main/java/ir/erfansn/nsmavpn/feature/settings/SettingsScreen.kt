@file:OptIn(ExperimentalMaterial3Api::class)

package ir.erfansn.nsmavpn.feature.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.dp
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.erfansn.nsmavpn.BuildConfig
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnLargeTopBar
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.theme.isSupportDynamicScheme
import ir.erfansn.nsmavpn.ui.util.preview.SettingsStates
import org.xmlpull.v1.XmlPullParser
import kotlin.random.Random

@Composable
fun SettingsRoute(
    onNavigateToBack: () -> Unit,
    onNavigateToTunnelSplitting: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        uiState = uiState,
        onChangeThemeMode = viewModel::updateThemeMode,
        onCheckDynamicScheme = viewModel::toggleDynamicScheme,
        onNavigateToBack = onNavigateToBack,
        onNavigateToTunnelSplitting = onNavigateToTunnelSplitting,
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    onChangeThemeMode: (Configurations.ThemeMode) -> Unit,
    onCheckDynamicScheme: () -> Unit,
    onNavigateToBack: () -> Unit,
    onNavigateToTunnelSplitting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    NsmaVpnScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NsmaVpnLargeTopBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_settings),
                        style = lerp(
                            start = LocalTextStyle.current,
                            stop = MaterialTheme.typography.displayMedium,
                            fraction = 1f - scrollBehavior.state.collapsedFraction
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .consumeWindowInsets(it),
            contentPadding = it,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(modifier = Modifier.padding(top = 12.dp))
            }
            item {
                ThemeModeItem(
                    themeMode = uiState.themeMode,
                    onChangeThemeMode = onChangeThemeMode
                )
            }
            if (isSupportDynamicScheme()) {
                item {
                    SettingsItem(
                        title = stringResource(R.string.item_title_use_dynamic_scheme),
                        onClick = onCheckDynamicScheme
                    ) {
                        Switch(
                            checked = uiState.isEnableDynamicScheme,
                            onCheckedChange = null
                        )
                    }
                }
            }
            item {
                ChangeLanguageItem()
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.item_title_split_tunneling),
                    supporting = stringResource(R.string.desc_split_tunneling),
                    onClick = onNavigateToTunnelSplitting
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.item_title_report_bug),
                    onClick = {
                        uriHandler.openUri(context.getString(R.string.link_feedback))
                    }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.item_title_licence),
                    onClick = {
                        uriHandler.openUri(context.getString(R.string.link_licences))
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(2.dp))
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.app_version),
                    supporting = BuildConfig.VERSION_NAME,
                    enabled = false,
                )
            }
            item {
                Spacer(modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun ChangeLanguageItem() {
    val context = LocalContext.current
    var shouldShowDialog by remember { mutableStateOf(false) }
    if (shouldShowDialog) {
        var selectedLocale by remember(context) {
            mutableStateOf(
                AppCompatDelegate.getApplicationLocales()[0]
                    ?: LocaleManagerCompat.getSystemLocales(context)[0]!!
            )
        }

        AlertDialog(
            onDismissRequest = {
                shouldShowDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val desiredLocale = LocaleListCompat.create(selectedLocale)
                        AppCompatDelegate.setApplicationLocales(desiredLocale)
                        shouldShowDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.settings_change_language_confirm_button))
                }
            },
            title = {
                Text(text = stringResource(R.string.settings_change_language_dialog_title))
            },
            text = {
                Column {
                    context.configLocales.let {
                        for (i in 0..<it.size()) {
                            SelectiveRow(
                                text = it[i]!!.displayName.toString(),
                                selected = it[i]!!.isO3Language == selectedLocale.isO3Language,
                                onSelect = {
                                    selectedLocale = it[i]!!
                                }
                            )
                        }
                    }
                }
            }
        )
    }
    SettingsItem(
        title = stringResource(R.string.item_title_change_language),
        onClick = {
            shouldShowDialog = true
        }
    )
}

private val Context.configLocales: LocaleListCompat
    @SuppressLint("DiscouragedApi")
    get() {
        val tagsList = mutableListOf<String>()
        val localeConfigId = resources.getIdentifier("_generated_res_locale_config", "xml", packageName)
        val xpp: XmlPullParser = resources.getXml(localeConfigId)
        while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
            if (xpp.eventType == XmlPullParser.START_TAG && xpp.name == "locale") {
                tagsList += xpp.getAttributeValue(0)
            }
            xpp.next()
        }
        return LocaleListCompat.forLanguageTags(tagsList.joinToString(","))
    }


@Composable
private fun ThemeModeItem(
    themeMode: Configurations.ThemeMode,
    onChangeThemeMode: (Configurations.ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(vertical = 8.dp)
                .padding(start = 16.dp, end = 24.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = stringResource(id = R.string.item_title_theme_mode),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surface,
        )
        ThemeModeOptions(
            themeMode = themeMode,
            onChangeThemeMode = onChangeThemeMode,
        )
    }
}

@Composable
private fun ThemeModeOptions(
    themeMode: Configurations.ThemeMode,
    onChangeThemeMode: (Configurations.ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .padding(vertical = 8.dp)
    ) {
        val innerPadding = PaddingValues(start = 24.dp)
        SelectiveRow(
            text = stringResource(R.string.option_title_theme_mode_default),
            selected = themeMode == Configurations.ThemeMode.System,
            onSelect = { onChangeThemeMode(Configurations.ThemeMode.System) },
            innerPadding = innerPadding,
        )
        SelectiveRow(
            text = stringResource(R.string.option_title_theme_mode_light),
            selected = themeMode == Configurations.ThemeMode.Light,
            onSelect = { onChangeThemeMode(Configurations.ThemeMode.Light) },
            innerPadding = innerPadding,
        )
        SelectiveRow(
            text = stringResource(R.string.option_title_theme_mode_dark),
            selected = themeMode == Configurations.ThemeMode.Dark,
            onSelect = { onChangeThemeMode(Configurations.ThemeMode.Dark) },
            innerPadding = innerPadding,
        )
    }
}

@Composable
private fun SelectiveRow(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(8.dp)
            .padding(innerPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    onClick: () -> Unit = { },
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(vertical = 8.dp)
                .padding(start = 16.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                supporting?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            Box {
                trailingContent?.invoke()
            }
        }
    }
}

@SettingsStates.PreviewApi30AndEarlier
@Composable
private fun SettingsScreenPreview_Api30AndEarlier() {
    SettingsScreenPreview()
}

@SettingsStates.PreviewApi30Later
@Composable
private fun SettingsScreenPreview_Api30Later() {
    SettingsScreenPreview()
}

@Composable
private fun SettingsScreenPreview() {
    NsmaVpnTheme {
        NsmaVpnBackground {
            SettingsScreen(
                uiState = SettingsUiState(
                    themeMode = Configurations.ThemeMode.entries.random(),
                    isEnableDynamicScheme = Random.nextBoolean(),
                ),
                onChangeThemeMode = { },
                onCheckDynamicScheme = { },
                onNavigateToBack = { },
                onNavigateToTunnelSplitting = { }
            )
        }
    }
}
