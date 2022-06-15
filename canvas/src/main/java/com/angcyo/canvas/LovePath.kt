package com.angcyo.canvas

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.canvas.utils.ShapesHelper
import com.angcyo.library.ex.toDegrees
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/05
 */
class LovePath : Path() {

    val loveBounds = RectF()

    override fun computeBounds(bounds: RectF, exact: Boolean) {
        //super.computeBounds(bounds, exact)
        bounds.set(loveBounds)
    }

    override fun transform(matrix: Matrix) {
        super.transform(matrix)
        matrix.mapRect(loveBounds, loveBounds)
    }

    override fun transform(matrix: Matrix, dst: Path?) {
        super.transform(matrix, dst)
        matrix.mapRect(loveBounds, loveBounds)
    }

    /**菱形+左右2个半圆*/
    fun initPath(
        width: Float = ShapesHelper.defaultWidth,
        height: Float = ShapesHelper.defaultHeight
    ) {
        reset()

        val w2 = width / 2f
        val h2 = height / 2f

        val c = sqrt(w2 * w2 + h2 * h2)
        val r = c / 2

        val a = atan(h2 / w2)

        val x1 = cos(a) * r
        val y1 = sin(a) * r //h2 - sqrt(r * r + x1 * x1)

        val x2 = w2 * 3 / 2
        val y2 = y1

        moveTo(w2, height)
        loveBounds.bottom = height
        lineTo(0f, h2)
        val rect = RectF()
        rect.set(x1 - r, y1 - r, x1 + r, y1 + r)
        loveBounds.left = rect.left
        loveBounds.top = rect.top
        val angle = 90f + 90f - atan(h2 / w2).toDegrees()
        arcTo(rect, angle, 180f)

        rect.set(x2 - r, y2 - r, x2 + r, y2 + r)
        loveBounds.right = rect.right
        arcTo(rect, -angle, 180f)

        lineTo(w2, height)
        close()
    }

}