package app.lawnchair.ui.preferences.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.launcher3.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen() {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor),
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_greeting),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.5).sp,
            ),
            color = onSurfaceColor,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 130.dp),
        )

        ButtonGroup(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) },
        ) {
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    FilledTonalIconButton(
                        onClick = { /* todo */ },
                        modifier = Modifier
                            .size(width = 72.dp, height = 52.dp)
                            .animateWidth(interactionSource),
                        shape = RoundedCornerShape(percent = 50),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = onSurfaceColor.copy(alpha = 0.15f),
                            contentColor = onSurfaceColor,
                        ),
                        interactionSource = interactionSource,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Restore,
                            contentDescription = stringResource(id = R.string.action_restore),
                        )
                    }
                },
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.action_restore)) },
                        onClick = { menuState.dismiss() },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Restore,
                                contentDescription = null,
                            )
                        },
                    )
                },
            )

            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = { /* todo */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .animateWidth(interactionSource),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonDefaults.buttonColors().containerColor.copy(alpha = 0.92f),
                        ),
                        interactionSource = interactionSource,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = stringResource(id = R.string.action_next),
                        )
                    }
                },
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.action_next)) },
                        onClick = { menuState.dismiss() },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                            )
                        },
                    )
                },
            )
        }
    }
}
