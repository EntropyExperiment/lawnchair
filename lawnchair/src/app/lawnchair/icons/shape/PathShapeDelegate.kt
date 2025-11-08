package app.lawnchair.icons.shape

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.View
import com.android.launcher3.graphics.ShapeDelegate
import com.android.launcher3.views.ClipPathView

/**
 * A ShapeDelegate that is initialized directly with a Path object,
 * bypassing SVG string conversion. The provided path is assumed to be
 * defined within a [0, 0, 100, 100] viewport.
 */
data class PathShapeDelegate(private val basePath: Path) : ShapeDelegate {

    private val tmpPath = Path()
    private val tmpMatrix = Matrix()

    override fun drawShape(
        canvas: Canvas,
        offsetX: Float,
        offsetY: Float,
        radius: Float,
        paint: Paint,
    ) {
        tmpPath.reset()
        addToPath(tmpPath, offsetX, offsetY, radius, tmpMatrix)
        canvas.drawPath(tmpPath, paint)
    }

    override fun addToPath(path: Path, offsetX: Float, offsetY: Float, radius: Float) {
        addToPath(path, offsetX, offsetY, radius, Matrix())
    }

    private fun addToPath(
        path: Path,
        offsetX: Float,
        offsetY: Float,
        radius: Float,
        matrix: Matrix,
    ) {
        // The base path is 100x100. We need to scale it to fit the desired 2 * radius.
        // The radius in ShapeDelegate is half the size of the icon.
        val scale = radius / 50f
        matrix.setScale(scale, scale)
        matrix.postTranslate(offsetX, offsetY)
        // Apply the transformation to our base path and add it to the destination path.
        basePath.transform(matrix, path)
    }

    override fun <T> createRevealAnimator(
        target: T,
        startRect: Rect,
        endRect: Rect,
        endRadius: Float,
        isReversed: Boolean,
    ): android.animation.ValueAnimator where T : View, T : ClipPathView {
        // This is complex to implement correctly without a proper morph.
        // For now, we can fall back to a simple Rect-based animation,
        // which is what would happen if a proper morph isn't possible anyway.
        // A more advanced implementation would use androidx.graphics.shapes.Morph.
        Log.w(
            "PathShapeDelegate",
            "createRevealAnimator is not fully implemented for custom paths.",
        )
        return ShapeDelegate.RoundedSquare(0f).createRevealAnimator(
            target,
            startRect,
            endRect,
            endRadius,
            isReversed,
        )
    }
}
