package eu.kanade.domain.ui.model

import androidx.appcompat.app.AppCompatDelegate
import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR

enum class ThemeMode(val titleRes: StringResource) {
    LIGHT(MR.strings.theme_light),
    DARK(MR.strings.theme_dark),
    SYSTEM(MR.strings.theme_system),
}

fun setAppCompatDelegateThemeMode(themeMode: ThemeMode) {
    AppCompatDelegate.setDefaultNightMode(
        when (themeMode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        },
    )
}
