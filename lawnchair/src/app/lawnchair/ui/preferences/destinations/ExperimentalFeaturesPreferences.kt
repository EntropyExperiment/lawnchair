package app.lawnchair.ui.preferences.destinations

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.controls.SliderPreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.controls.WarningPreference
import app.lawnchair.ui.preferences.components.layout.ExpandAndShrink
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import com.android.launcher3.R

@Composable
fun ExperimentalFeaturesPreferences(
    modifier: Modifier = Modifier,
) {
    val prefs = preferenceManager()
    val prefs2 = preferenceManager2()
    PreferenceLayout(
        label = stringResource(id = R.string.experimental_features_label),
        backArrowVisible = !LocalIsExpandedScreen.current,
        modifier = modifier,
    ) {
        PreferenceGroup {
            SwitchPreference(
                adapter = prefs2.enableFontSelection.getAdapter(),
                label = stringResource(id = R.string.font_picker_label),
                description = stringResource(id = R.string.font_picker_description),
            )
            SwitchPreference(
                adapter = prefs2.enableSmartspaceCalendarSelection.getAdapter(),
                label = stringResource(id = R.string.smartspace_calendar_label),
                description = stringResource(id = R.string.smartspace_calendar_description),
            )
            SwitchPreference(
                adapter = prefs.workspaceIncreaseMaxGridSize.getAdapter(),
                label = stringResource(id = R.string.workspace_increase_max_grid_size_label),
                description = stringResource(id = R.string.workspace_increase_max_grid_size_description),
            )
            SwitchPreference(
                adapter = prefs2.alwaysReloadIcons.getAdapter(),
                label = stringResource(id = R.string.always_reload_icons_label),
                description = stringResource(id = R.string.always_reload_icons_description),
            )
            SwitchPreference(
                adapter = prefs2.enableTrieSearch.getAdapter(),
                label = stringResource(id = R.string.trie_weighted_levenshtein_search_label),
                description = stringResource(id = R.string.trie_weighted_levenshtein_description),
            )

            val enableWallpaperBlur = prefs.enableWallpaperBlur.getAdapter()

            SwitchPreference(
                adapter = enableWallpaperBlur,
                label = stringResource(id = R.string.wallpaper_blur),
            )
            ExpandAndShrink(visible = enableWallpaperBlur.state.value) {
                SliderPreference(
                    label = stringResource(id = R.string.wallpaper_background_blur),
                    adapter = prefs.wallpaperBlur.getAdapter(),
                    step = 5,
                    valueRange = 0..100,
                    showUnit = "%",
                )
            }
            ExpandAndShrink(visible = enableWallpaperBlur.state.value) {
                SliderPreference(
                    label = stringResource(id = R.string.wallpaper_background_blur_factor),
                    adapter = prefs.wallpaperBlurFactorThreshold.getAdapter(),
                    step = 5,
                    valueRange = 0..100,
                    showUnit = "%",
                )
            }
        }
        ExpandAndShrink(visible = prefs2.enableTrieSearch.getAdapter().state.value) {
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceGroup(
                heading = stringResource(id = R.string.trie_weighted_levenshtein_search_label),
                description = stringResource(id = R.string.trie_weighted_levenshtein_group_description),
                showDescription = true,
            ) {
                WarningPreference(
                    text = stringResource(id = R.string.trie_weighted_levenshtein_override),
                )
                SliderPreference(
                    label = stringResource(id = R.string.trie_weighted_levenshtein_distance_label),
                    adapter = prefs2.trieSearchMaxLevenshteinDistance.getAdapter(),
                    step = .5f,
                    valueRange = .5f..3f,
                )
                SwitchPreference(
                    adapter = prefs2.shouldSearchComponent.getAdapter(),
                    label = stringResource(id = R.string.should_search_component_label),
                    description = stringResource(id = R.string.should_search_component_description),
                )
            }
        }
    }
}
