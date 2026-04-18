package no.uio.ifi.in2000.team45.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.os.LocaleListCompat
import org.intellij.lang.annotations.Language

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