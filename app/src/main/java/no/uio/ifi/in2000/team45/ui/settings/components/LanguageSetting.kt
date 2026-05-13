package no.uio.ifi.in2000.team45.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.settings.models.LanguageOption

/**
 * Settings section that allows the user to choose the application language.
 * Displays a list of available language options using [LanguageOptionRow].
 *
 * @param selectedLanguage Currently selected language option.
 * @param onLanguageSelected Callback triggered when the user selects a new language.
 * @param modifier Optional modifier for layout.
 */
@Composable
fun LanguageSetting(
    selectedLanguage: LanguageOption,
    onLanguageSelected: (LanguageOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(R.string.language),
            fontSize = 18.sp
        )

        LanguageOptionRow(
            selected = selectedLanguage == LanguageOption.BOKMAL,
            text = stringResource(R.string.language_bokmal),
            flagRes = R.drawable.norway_flag,
            onClick = { onLanguageSelected(LanguageOption.BOKMAL) }
        )

        LanguageOptionRow(
            selected = selectedLanguage == LanguageOption.NYNORSK,
            text = stringResource(R.string.language_nynorsk),
            flagRes = R.drawable.norway_flag,
            onClick = { onLanguageSelected(LanguageOption.NYNORSK) }
        )

        LanguageOptionRow(
            selected = selectedLanguage == LanguageOption.ENGLISH,
            text = stringResource(R.string.language_english),
            flagRes = R.drawable.gb_flag,
            onClick = { onLanguageSelected(LanguageOption.ENGLISH) }
        )

        LanguageOptionRow(
            selected = selectedLanguage == LanguageOption.SAMI,
            text = stringResource(R.string.language_sami),
            flagRes = R.drawable.sami_flag,
            onClick = { onLanguageSelected(LanguageOption.SAMI) }
        )
    }
}