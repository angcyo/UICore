package com.angcyo.crop

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

/**
 * 显示剪切图片的[AppCompatImageView]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/13
 */
class CropView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    val cropDelegate = CropDelegate(this)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cropDelegate.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return cropDelegate.onTouchEvent(event)
    }
}