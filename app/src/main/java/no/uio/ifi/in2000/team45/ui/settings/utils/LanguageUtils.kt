package no.uio.ifi.in2000.team45.ui.settings.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import no.uio.ifi.in2000.team45.data.settings.models.LanguageOption

fun applyAppLanguage(language: LanguageOption) {
    val localeTag = when (language) {
        LanguageOption.BOKMAL -> "nb"
        LanguageOption.NYNORSK -> "nn"
        LanguageOption.ENGLISH -> "en"
        LanguageOption.SAMI -> "smi"
    }
    val desired = LocaleListCompat.forLanguageTags(localeTag)

    // Skip when already set, otherwise setApplicationLocales would
    // trigger an activity recreation for no reason
    if (AppCompatDelegate.getApplicationLocales() == desired) return

    AppCompatDelegate.setApplicationLocales(desired)
}