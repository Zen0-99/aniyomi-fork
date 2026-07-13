package eu.kanade.presentation.novel.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.model.ScreenModel
import eu.kanade.tachiyomi.ui.reader.novel.NovelReaderPreferences
import eu.kanade.tachiyomi.ui.reader.novel.NovelReadingMode
import eu.kanade.tachiyomi.ui.reader.novel.TextAlignment
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.CheckboxItem
import tachiyomi.presentation.core.components.HeadingItem
import tachiyomi.presentation.core.components.SettingsChipRow
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NovelReaderSettingsScreenModel(
    val hasDisplayCutout: Boolean,
    val onReadingModeChange: (Int) -> Unit,
    val onBackgroundColorChange: (Int) -> Unit,
    val onTextSettingChange: () -> Unit,
    val preferences: NovelReaderPreferences = Injekt.get(),
    val readerPreferences: ReaderPreferences = Injekt.get(),
) : ScreenModel

private data class BackgroundColorOption(
    val labelRes: StringResource,
    val themeValue: Int,
)

private val backgroundColorOptions = listOf(
    BackgroundColorOption(MR.strings.white_background, 0),
    BackgroundColorOption(MR.strings.black_background, 1),
    BackgroundColorOption(MR.strings.gray_background, 2),
)

private val readingModeOptions = listOf(
    "Default" to NovelReadingMode.DEFAULT,
    "Infinite scroll" to NovelReadingMode.INFINITE_SCROLL,
    "Overscroll" to NovelReadingMode.OVERSCROLL,
)

@Composable
fun NovelReaderSettingsDialog(
    onDismissRequest: () -> Unit,
    onShowMenus: () -> Unit,
    screenModel: NovelReaderSettingsScreenModel,
) {
    val tabTitles = persistentListOf(
        stringResource(MR.strings.pref_category_general),
        "Text",
    )
    val pagerState = rememberPagerState { tabTitles.size }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = {
            onDismissRequest()
            onShowMenus()
        },
        properties = DialogProperties(
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.heightIn(max = 600.dp),
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title) },
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                ) { page ->
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        when (page) {
                            0 -> NovelGeneralSettingsPage(screenModel)
                            1 -> NovelTextSettingsPage(screenModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.NovelGeneralSettingsPage(screenModel: NovelReaderSettingsScreenModel) {
    val readerTheme by screenModel.readerPreferences.readerTheme().collectAsState()

    HeadingItem("Reading mode")

    val novelReadingMode by screenModel.preferences.readingMode().collectAsState()

    SettingsChipRow(MR.strings.pref_viewer_type) {
        readingModeOptions.map { (label, mode) ->
            FilterChip(
                selected = novelReadingMode == mode,
                onClick = {
                    screenModel.preferences.readingMode().set(mode)
                    screenModel.onReadingModeChange(mode.prefValue)
                },
                label = { Text(label) },
            )
        }
    }

    HeadingItem(MR.strings.pref_reader_theme)

    SettingsChipRow(MR.strings.pref_reader_theme) {
        backgroundColorOptions.map { option ->
            FilterChip(
                selected = readerTheme == option.themeValue,
                onClick = {
                    screenModel.readerPreferences.readerTheme().set(option.themeValue)
                    screenModel.onBackgroundColorChange(option.themeValue)
                },
                label = { Text(stringResource(option.labelRes)) },
            )
        }
    }

    CheckboxItem(
        label = stringResource(MR.strings.pref_fullscreen),
        pref = screenModel.readerPreferences.fullscreen(),
    )

    if (screenModel.hasDisplayCutout && screenModel.readerPreferences.fullscreen().get()) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_cutout_short),
            pref = screenModel.readerPreferences.cutoutShort(),
        )
    }

    CheckboxItem(
        label = stringResource(MR.strings.pref_keep_screen_on),
        pref = screenModel.readerPreferences.keepScreenOn(),
    )
}

@Composable
private fun ColumnScope.NovelTextSettingsPage(screenModel: NovelReaderSettingsScreenModel) {
    val textSize by screenModel.preferences.textSize().collectAsState()
    val lineHeight by screenModel.preferences.lineHeight().collectAsState()
    val paragraphSpacing by screenModel.preferences.paragraphSpacing().collectAsState()
    val textAlignment by screenModel.preferences.textAlignment().collectAsState()

    HeadingItem("Text size")

    SliderItem(
        value = textSize.toInt(),
        valueRange = 10..32,
        label = "Text size",
        valueText = "${textSize.toInt()}sp",
        onChange = { newValue ->
            screenModel.preferences.textSize().set(newValue.toFloat())
            screenModel.onTextSettingChange()
        },
    )

    HeadingItem("Line height")

    val lineHeightInt = remember(lineHeight) { (lineHeight * 10).toInt() }
    SliderItem(
        value = lineHeightInt,
        valueRange = 10..30,
        label = "Line height",
        valueText = "%.1fx".format(lineHeight),
        onChange = { newValue ->
            screenModel.preferences.lineHeight().set(newValue / 10f)
            screenModel.onTextSettingChange()
        },
    )

    HeadingItem("Paragraph spacing")

    SliderItem(
        value = paragraphSpacing,
        valueRange = 0..48,
        label = "Paragraph spacing",
        valueText = "${paragraphSpacing}dp",
        onChange = { newValue ->
            screenModel.preferences.paragraphSpacing().set(newValue)
            screenModel.onTextSettingChange()
        },
    )

    HeadingItem("Text alignment")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextAlignment.entries.forEach { alignment ->
            FilterChip(
                selected = textAlignment == alignment,
                onClick = {
                    screenModel.preferences.textAlignment().set(alignment)
                    screenModel.onTextSettingChange()
                },
                label = {
                    Text(
                        alignment.name.lowercase().replaceFirstChar { it.uppercase() },
                    )
                },
            )
        }
    }
}
