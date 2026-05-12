package no.uio.ifi.in2000.team45.ui.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.material3.RadioButton
import androidx.compose.ui.draw.clip

/**
 * Row representing a selectable language option in the settings screen.
 * Displays a radio button, label, and corresponding flag icon.
 *
 * @param selected Whether this language is currently selected.
 * @param text Display name of the language.
 * @param flagRes Drawable resource ID for the language flag.
 * @param onClick Callback triggered when the row is selected.
 */
@Composable
fun LanguageOptionRow(
    selected: Boolean,
    text: String,
    flagRes: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(text = text)

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = flagRes),
            contentDescription = text,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
        )
    }
}