package no.uio.ifi.in2000.team45.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A custom animated confirmation dialog with fade and scale transitions.
 *
 * @param visible Controls whether the dialog is shown.
 * @param onDismissed Callback invoked when the 'confirmText' action is clicked.
 * @param title Title text displayed at the top of the dialog.
 * @param text Body message shown inside the dialog.
 * @param confirmText Label for the confirm button.
 */
@Composable
fun AnimatedConfirmDialog(
    visible: Boolean,
    onDismissed: () -> Unit,
    title: String,
    text: String,
    confirmText: String
) {
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = visible

    val keepMounted = transitionState.currentState || transitionState.targetState
    if (!keepMounted) return

    Dialog(
        onDismissRequest = { /* modal: only the confirm button dismisses */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.92f),
            exit = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.92f)
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .widthIn(max = 560.dp),
                shape = AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = AlertDialogDefaults.titleContentColor
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlertDialogDefaults.textContentColor
                    )
                    Spacer(Modifier.height(24.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(onClick = onDismissed) {
                            Text(text = confirmText)
                        }
                    }
                }
            }
        }
    }
}
