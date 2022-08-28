package com.angcyo.canvas

import android.graphics.Path
import android.graphics.RectF
import com.angcyo.canvas.utils.ShapesHelper

/**
 * 标识是线段
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/05
 */
class LinePath : Path() {

    val pathBounds = RectF()

    override fun computeBounds(bounds: RectF, exact: Boolean) {
        //super.computeBounds(bounds, exact)
        bounds.set(pathBounds)
    }

    /**横线*/
    fun initPath(length: Float = ShapesHelper.defaultWidth) {
        reset()
        moveTo(0f, 0f)
        lineTo(length, 0f)
        pathBounds.set(0f, 0f, length, 0f)
    }
}