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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.lawnchair.ui.theme.preferenceGroupColor

@Composable
fun PreferenceGroup(
    modifier: Modifier = Modifier,
    heading: String? = null,
    description: String? = null,
    showDescription: Boolean = true,
    showDividers: Boolean = true,
    dividerStartIndent: Dp = 0.dp,
    dividerEndIndent: Dp = 0.dp,
    dividersToSkip: Int = 0,
    dividerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        PreferenceGroupHeading(heading)
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = preferenceGroupColor(),
        ) {
            if (showDividers) {
                DividerColumn(
                    startIndent = dividerStartIndent,
                    endIndent = dividerEndIndent,
                    content = content,
                    dividersToSkip = dividersToSkip,
                    color = dividerColor,
                )
            } else {
                Column {
                    content()
                }
            }
        }
        PreferenceGroupDescription(description = description, showDescription = showDescription)
    }
}

/**
 * PreferenceGroup with scope-based content for position-aware items.
 * Items added via scope automatically get isFirst/isLast tracking
 */
@Composable
fun PreferenceGroupPositionAware(
    modifier: Modifier = Modifier,
    heading: String? = null,
    description: String? = null,
    showDescription: Boolean = true,
    itemSpacing: Dp = 4.dp,
    content: @Composable PreferenceGroupScope.() -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        PreferenceGroupHeading(heading)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            val scope = PreferenceGroupScopeImpl()
            scope.content()
            scope.Render()
        }
        PreferenceGroupDescription(description = description, showDescription = showDescription)
    }
}

interface PreferenceGroupScope {
    fun item(
        key: Any? = null,
        content: @Composable (position: PreferenceGroupItemPosition) -> Unit,
    )
}

private class PreferenceGroupScopeImpl : PreferenceGroupScope {
    private val items = mutableListOf<@Composable (position: PreferenceGroupItemPosition) -> Unit>()

    override fun item(
        key: Any?,
        content: @Composable (position: PreferenceGroupItemPosition) -> Unit,
    ) {
        items.add(content)
    }

    @Composable
    fun Render() {
        val count = items.size
        items.forEachIndexed { index, item ->
            val position = PreferenceGroupItemPosition(
                isFirst = index == 0,
                isLast = index == count - 1,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = preferenceGroupItemShape(position),
                color = preferenceGroupColor(),
            ) {
                item(position)
            }
        }
    }
}

data class PreferenceGroupItemPosition(
    val isFirst: Boolean = false,
    val isLast: Boolean = false,
)

fun preferenceGroupItemShape(
    position: PreferenceGroupItemPosition,
    largeCorner: Dp = 24.dp,
    smallCorner: Dp = 4.dp,
): Shape {
    val topCorner = if (position.isFirst) largeCorner else smallCorner
    val bottomCorner = if (position.isLast) largeCorner else smallCorner
    return RoundedCornerShape(
        topStart = topCorner,
        topEnd = topCorner,
        bottomStart = bottomCorner,
        bottomEnd = bottomCorner,
    )
}

@Composable
fun PreferenceGroupHeading(
    heading: String?,
    modifier: Modifier = Modifier,
) {
    if (heading != null) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .height(48.dp)
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = heading,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { this.heading() },
            )
        }
    } else {
        Spacer(modifier = modifier.requiredHeight(8.dp))
    }
}

@Composable
fun PreferenceGroupDescription(
    modifier: Modifier = Modifier,
    description: String? = null,
    showDescription: Boolean = true,
) {
    description?.let {
        ExpandAndShrink(
            modifier = modifier,
            visible = showDescription,
        ) {
            Row(modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 16.dp)) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
