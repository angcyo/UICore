package com.angcyo.picker.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */
class PickerLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatImageView(context, attributeSet) {

    var rotateDegrees = 0f

    override fun onDraw(canvas: Canvas) {
        canvas.rotate(rotateDegrees, width / 2.toFloat(), height / 2.toFloat())
        super.onDraw(canvas)

        if (isEnabled && drawable != null) {
            rotateDegrees += 5f
            rotateDegrees = if (rotateDegrees < 360) rotateDegrees else rotateDegrees - 360
            postInvalidate()
        }
    }
}