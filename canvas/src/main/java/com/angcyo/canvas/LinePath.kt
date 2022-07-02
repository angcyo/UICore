package com.angcyo.canvas

import android.graphics.Path
import android.graphics.RectF
import android.widget.LinearLayout
import com.angcyo.canvas.utils.ShapesHelper
import com.angcyo.library.ex.emptyRectF

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/05
 */
class LinePath : Path() {

    /**线的方向, 水平垂直*/
    var orientation: Int = LinearLayout.VERTICAL

    val lineBounds = emptyRectF()

    override fun computeBounds(bounds: RectF, exact: Boolean) {
        //super.computeBounds(bounds, exact)
        bounds.set(lineBounds)
    }

    fun initPath(length: Float = ShapesHelper.defaultWidth) {
        reset()
        moveTo(0f, 0f)
        lineBounds.left = 0f
        lineBounds.top = 0f
        lineBounds.right = length
        lineBounds.bottom = length
        if (orientation == LinearLayout.VERTICAL) {
            lineTo(0f, length)
            lineBounds.right = 1f
            //bounds.inset(-2f, 0f)
        } else {
            lineTo(length, 0f)
            lineBounds.bottom = 1f
            //bounds.inset(0f, -2f)
        }
    }
}