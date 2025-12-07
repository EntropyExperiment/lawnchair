/*
 * Copyright 2025, Lawnchair
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.lawnchair.ui.theme.preferenceGroupColor

/**
 * A PreferenceGroup variant that renders each child as a visually stacked card with:
 * - First item: large top corners, small bottom corners
 * - Middle items: small corners all around
 * - Last item: small top corners, large bottom corners
 *
 * Use PreferenceStackedCardItem inside this group.
 */
@Composable
fun PreferenceStackedCardGroup(
    modifier: Modifier = Modifier,
    heading: String? = null,
    description: String? = null,
    showDescription: Boolean = true,
    itemSpacing: Dp = 4.dp,
    content: @Composable PreferenceStackedCardScope.() -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        PreferenceGroupHeading(heading)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            val scope = PreferenceStackedCardScopeImpl()
            scope.content()
        }
        PreferenceGroupDescription(description = description, showDescription = showDescription)
    }
}

interface PreferenceStackedCardScope {
    @Composable
    fun PreferenceStackedCardItem(
        modifier: Modifier = Modifier,
        isFirst: Boolean = false,
        isLast: Boolean = false,
        content: @Composable () -> Unit,
    )
}

private class PreferenceStackedCardScopeImpl : PreferenceStackedCardScope {
    @Composable
    override fun PreferenceStackedCardItem(
        modifier: Modifier,
        isFirst: Boolean,
        isLast: Boolean,
        content: @Composable () -> Unit,
    ) {
        val largeCorner = 24.dp
        val smallCorner = 4.dp

        val topCorner = if (isFirst) largeCorner else smallCorner
        val bottomCorner = if (isLast) largeCorner else smallCorner

        val shape = RoundedCornerShape(
            topStart = topCorner,
            topEnd = topCorner,
            bottomStart = bottomCorner,
            bottomEnd = bottomCorner,
        )

        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = preferenceGroupColor(),
        ) {
            content()
        }
    }
}
