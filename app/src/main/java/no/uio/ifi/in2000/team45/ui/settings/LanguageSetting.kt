package no.uio.ifi.in2000.team45.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.stringResource
import no.uio.ifi.in2000.team45.R

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            RadioButton(
                selected = selectedLanguage == LanguageOption.BOKMÅL,
                onClick = { onLanguageSelected(LanguageOption.BOKMÅL) }
            )
            Text(
                text = stringResource(R.string.language_bokmal),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            RadioButton(
                selected = selectedLanguage == LanguageOption.NYNORSK,
                onClick = { onLanguageSelected(LanguageOption.NYNORSK) }
            )
            Text(
                text = stringResource(R.string.language_nynorsk),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            RadioButton(
                selected = selectedLanguage == LanguageOption.ENGLISH,
                onClick = { onLanguageSelected(LanguageOption.ENGLISH) }
            )
            Text(
                text = stringResource(R.string.language_english),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}