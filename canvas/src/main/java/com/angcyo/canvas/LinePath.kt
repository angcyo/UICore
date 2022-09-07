package com.angcyo.canvas

import android.graphics.Matrix
import android.graphics.Path
import com.angcyo.canvas.utils.ShapesHelper

/**
 * 标识是线段
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/05
 */
class LinePath : Path() {

    override fun transform(matrix: Matrix, dst: Path?) {
        super.transform(matrix, dst)
    }

    /**横线*/
    fun initPath(length: Float = ShapesHelper.defaultWidth) {
        reset()
        moveTo(0f, 0f)
        lineTo(length, 0f)
    }
}