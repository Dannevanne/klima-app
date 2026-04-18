package no.uio.ifi.in2000.team45.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

fun applyAppLanguage(language: LanguageOption) {
    val localeTag = when (language) {
        LanguageOption.BOKMÅL -> "nb"
        LanguageOption.NYNORSK -> "nn"
        LanguageOption.ENGLISH -> "en"
    }

    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(localeTag)
    )
}