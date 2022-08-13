package com.angcyo.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

/**
 * 显示剪切图片的[AppCompatImageView]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/13
 */
class CropImageView(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {

    var cropImageMatrix: Matrix = Matrix()

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        cropImageMatrix.setTranslate(300f, 300f)
        imageMatrix = cropImageMatrix

        return super.onTouchEvent(event)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        cropImageMatrix.setTranslate(300f, 300f)

        imageMatrix = cropImageMatrix
    }

}