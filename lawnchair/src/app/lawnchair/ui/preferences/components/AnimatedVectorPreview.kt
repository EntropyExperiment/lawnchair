package app.lawnchair.ui.preferences.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.milliseconds

/** Playback handler for Animated Vector Drawable (AVD)
 *
 * @param animatedVectorResId AVD files
 * @param isPlaying Is the animation allowed to be played or not,
 * **NOTE** when you stop the animation, the handler will play the animation until the end of frames.
 * @param modifier Compose modifier
 * @param contentDescription Text used by accessibility to announce AVD animations to the screen reader.
 * Keep it null usually unless that animations is significantly important to the user experiences.
 *
 * */
@Composable
fun AnimatedVectorPreview(
    @DrawableRes animatedVectorResId: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val animatedImage = AnimatedImageVector.animatedVectorResource(animatedVectorResId)
    val requestedPlaying by rememberUpdatedState(isPlaying)
    val animationDuration = remember(animatedImage) {
        animatedImage.totalDuration.coerceAtLeast(1).milliseconds
    }
    var loopIteration by remember(animatedVectorResId) { mutableIntStateOf(0) }
    var showEndFrame by remember(animatedVectorResId) { mutableStateOf(true) }

    LaunchedEffect(animatedImage, animationDuration) {
        while (true) {
            snapshotFlow { requestedPlaying }.first { it }
            showEndFrame = false
            loopIteration++

            delay(animationDuration)

            if (!requestedPlaying) {
                showEndFrame = true
                continue
            }

            while (requestedPlaying) {
                loopIteration++
                delay(animationDuration)
            }

            showEndFrame = true
        }
    }

    if (showEndFrame) {
        Image(
            painter = rememberAnimatedVectorPainter(
                animatedImageVector = animatedImage,
                atEnd = true,
            ),
            contentDescription = contentDescription,
            modifier = modifier,
        )
    } else {
        key(loopIteration) {
            AnimatedVectorRun(
                animatedImage = animatedImage,
                modifier = modifier,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
private fun AnimatedVectorRun(
    animatedImage: AnimatedImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    var atEnd by remember { mutableStateOf(false) }

    LaunchedEffect(animatedImage) {
        withFrameNanos { }
        atEnd = true
    }

    Image(
        painter = rememberAnimatedVectorPainter(
            animatedImageVector = animatedImage,
            atEnd = atEnd,
        ),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
