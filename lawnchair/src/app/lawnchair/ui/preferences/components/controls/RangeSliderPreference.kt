package app.lawnchair.ui.preferences.components.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.lawnchair.preferences.PreferenceAdapter
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import com.android.launcher3.util.MSDLPlayerWrapper
import com.google.android.msdl.data.model.MSDLToken
import kotlin.math.roundToInt

@Composable
fun RangeSliderPreference(
    label: String,
    lowAdapter: PreferenceAdapter<Int>,
    highAdapter: PreferenceAdapter<Int>,
    valueRange: ClosedRange<Int>,
    step: Int,
    modifier: Modifier = Modifier,
    lowLabel: String = "",
    highLabel: String = "",
) {
    var lowValue by lowAdapter
    var highValue by highAdapter

    val floatRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat()
    val steps = if (step > 0) {
        ((valueRange.endInclusive - valueRange.start) / step) - 1
    } else {
        0
    }

    var sliderValues by remember {
        mutableStateOf(lowValue.toFloat()..highValue.toFloat())
    }

    DisposableEffect(lowValue, highValue) {
        sliderValues = lowValue.toFloat()..highValue.toFloat()
        onDispose { }
    }

    val mMSDLPlayerWrapper = MSDLPlayerWrapper.INSTANCE.get(LocalContext.current)

    PreferenceTemplate(
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Text(text = label)
                val low = sliderValues.start.roundToInt()
                val high = sliderValues.endInclusive.roundToInt()
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (lowLabel.isNotEmpty()) "$lowLabel: $low" else "$low",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (highLabel.isNotEmpty()) "$highLabel: $high" else "$high",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        description = {
            RangeSlider(
                value = sliderValues,
                onValueChange = { newRange ->
                    sliderValues = newRange
                    mMSDLPlayerWrapper.playToken(MSDLToken.DRAG_INDICATOR_DISCRETE)
                },
                onValueChangeFinished = {
                    lowValue = sliderValues.start.roundToInt()
                    highValue = sliderValues.endInclusive.roundToInt()
                },
                valueRange = floatRange,
                steps = steps,
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 12.dp)
                    .padding(horizontal = 14.dp)
                    .height(24.dp),
            )
        },
        modifier = modifier,
        applyPaddings = false,
    )
}
