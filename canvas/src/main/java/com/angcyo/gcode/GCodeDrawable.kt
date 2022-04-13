package com.angcyo.gcode

import android.graphics.Picture
import android.graphics.RectF
import com.angcyo.canvas.ScalePictureDrawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */
class GCodeDrawable(picture: Picture) : ScalePictureDrawable(picture) {

    val gCodeBound: RectF = RectF()

    init {
        gCodeBound.set(0f, 0f, minimumWidth.toFloat(), minimumHeight.toFloat())
    }
}