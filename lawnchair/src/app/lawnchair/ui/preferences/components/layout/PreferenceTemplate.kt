/*
 * Copyright 2021, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair.ui.preferences.components.layout

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.lawnchair.ui.util.addIf

/***
 * A template used to create most preference-related components in the Preference UI.
 */
@Suppress("ktlint:compose:modifier-not-used-at-root")
@Composable
fun PreferenceTemplate(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    enabled: Boolean = true,
    applyPaddings: Boolean = true,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 10.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    description: @Composable () -> Unit = {},
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
) {
    Column {
        Row(
            verticalAlignment = verticalAlignment,
            modifier = modifier
                .height(IntrinsicSize.Min)
                .semantics(mergeDescendants = true) {}
                .fillMaxWidth()
                .addIf(applyPaddings) {
                    padding(horizontal = horizontalPadding, vertical = verticalPadding)
                },
        ) {
            startWidget?.let {
                startWidget()
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(12.dp))
                }
            }
            Row(
                modifier = contentModifier
                    .weight(1f)
                    .addIf(!enabled) {
                        alpha(0.38f)
                    },
                verticalAlignment = verticalAlignment,
            ) {
                Column(Modifier.weight(1f)) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                        LocalTextStyle provides MaterialTheme.typography.titleMedium,
                    ) {
                        title()
                    }
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                        LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    ) {
                        description()
                    }
                }
            }
            endWidget?.let {
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(12.dp))
                }
                endWidget()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewPreferenceTemplate(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    enabled: Boolean = true,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    description: @Composable () -> Unit = {},
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    overlineContent: (@Composable () -> Unit)? = null,
    shapes: ListItemShapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    colors: ListItemColors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    interactionSource: MutableInteractionSource? = null,
) {
    PreferenceTemplate(
        title,
        modifier,
        contentModifier,
        enabled,
        verticalAlignment,
        description,
        startWidget,
        endWidget,
        overlineContent,
        onClick = onClick,
        onLongClick = onLongClick,
        onLongClickLabel = onLongClickLabel,
        colors = colors,
        interactionSource = interactionSource,
    )
}

/***
 * A template used to create most preference-related components in the Preference UI.
 *
 * Material Expressive
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Suppress("ktlint:compose:modifier-not-used-at-root")
@Composable
fun PreferenceTemplate(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    enabled: Boolean = true,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    description: @Composable () -> Unit = {},
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    overlineContent: (@Composable () -> Unit)? = null,
    shapes: ListItemShapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    colors: ListItemColors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    interactionSource: MutableInteractionSource? = null,
) {
    val localInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    Column(modifier) {
        SegmentedListItem(
//            selected = TODO(),
            onClick = { onClick?.invoke() },
            // Since we don't know the position of the list, we assume it's in a middle position,
            // then we clip or round the column make the list round instead.
            shapes = shapes,
            modifier = contentModifier,
            enabled = enabled,
            leadingContent = startWidget,
            trailingContent = endWidget,
            overlineContent = overlineContent,
            supportingContent = {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                ) {
                    description()
                }
            },
            verticalAlignment = verticalAlignment,
            onLongClick = onLongClick,
            onLongClickLabel = onLongClickLabel,
            colors = colors,
            elevation = ListItemDefaults.elevation(),
            contentPadding = ListItemDefaults.ContentPadding,
            interactionSource = localInteractionSource,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                LocalTextStyle provides MaterialTheme.typography.titleMedium,
            ) {
                title()
            }
        }
    }
}
