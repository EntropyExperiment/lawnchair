package app.lawnchair.ui.preferences.components.controls

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.lawnchair.ui.preferences.components.layout.NewPreferenceTemplate
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import app.lawnchair.ui.theme.LawnchairTheme
import app.lawnchair.ui.util.preview.PreferenceGroupPreviewContainer
import app.lawnchair.ui.util.preview.PreviewLawnchair

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WarningPreference(
    text: String,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    iconTint: Color = MaterialTheme.colorScheme.error,
    textColor: Color = MaterialTheme.colorScheme.error,
) {
    NewPreferenceTemplate(
        modifier = modifier,
        title = {},
        description = {
            Text(
                text = text,
                color = textColor,
            )
        },
        startWidget = {
            Icon(
                imageVector = Icons.Rounded.Warning,
                tint = iconTint,
                contentDescription = null,
            )
        },
        colors = colors,
    )
}

@PreviewLawnchair
@Composable
private fun WarningPreferencePreview() {
    LawnchairTheme {
        PreferenceGroupPreviewContainer {
            WarningPreference(
                text = "Text",
            )
        }
    }
}
