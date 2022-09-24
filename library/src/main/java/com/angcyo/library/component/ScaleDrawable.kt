package com.angcyo.library.component

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import androidx.core.graphics.withSave

/**
 * 包裹一个[drawable], 然后强制指定绘制的宽高
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/24
 */
class ScaleDrawable(val drawable: Drawable, val drawWidth: Int, val drawHeight: Int) : Drawable() {

    override fun draw(canvas: Canvas) {
        canvas.withSave {
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            drawable.setBounds(0, 0, width, height)
            scale(
                drawWidth * 1f / width,
                drawHeight * 1f / height,
                bounds.centerX().toFloat(),
                bounds.centerY().toFloat()
            )
            drawable.draw(this)
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        //drawable.setBounds(left, top, right, bottom)
    }

    override fun setAlpha(alpha: Int) {
        drawable.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        drawable.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = drawable.opacity

    override fun getIntrinsicWidth(): Int {
        return drawWidth
    }

    override fun getIntrinsicHeight(): Int {
        return drawHeight
    }
}

/**缩放绘制指定的[Drawable]*/
fun Drawable?.scaleDrawable(drawWidth: Int, drawHeight: Int): Drawable? {
    if (this == null) {
        return null
    }
    return ScaleDrawable(this, drawWidth, drawHeight)
}