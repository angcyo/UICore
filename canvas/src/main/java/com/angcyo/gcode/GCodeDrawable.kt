package com.angcyo.gcode

import android.graphics.Picture
import android.graphics.RectF
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.library.ex.emptyRectF

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */
class GCodeDrawable(picture: Picture) : ScalePictureDrawable(picture) {

    /**gCode的bounds
     * [com.angcyo.gcode.GCodeHelper.GCodeHandler.getGCodeBounds]*/
    val gCodeBound: RectF = emptyRectF()

    /**GCode数据*/
    var gCodeData: String? = null

    init {
        gCodeBound.set(0f, 0f, minimumWidth.toFloat(), minimumHeight.toFloat())
    }
}