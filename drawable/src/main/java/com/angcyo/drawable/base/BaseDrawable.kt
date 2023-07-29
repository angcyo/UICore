package com.angcyo.drawable.base

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/29
 */
abstract class BaseDrawable : Drawable() {

    /**标识, 是否需要半透明*/
    var isTranslucent: Boolean? = null

    /**能够绘制的范围*/
    val drawRect: Rect = Rect()
        get() {
            attachView?.let { view: View ->
                field.set(
                    view.paddingLeft,
                    view.paddingTop,
                    view.measuredWidth - view.paddingRight,
                    view.measuredHeight - view.paddingBottom
                )
            }
            return field
        }

    override fun draw(canvas: Canvas) {
        //draw
    }

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
    }

    /**[setBounds] 最终走这个方法*/
    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
    }

    override fun setAlpha(alpha: Int) {
        invalidateSelf()
    }

    override fun getAlpha(): Int {
        return super.getAlpha()
    }

    override fun getColorFilter(): ColorFilter? {
        return super.getColorFilter()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        invalidateSelf()
    }

    //不透明度
    override fun getOpacity(): Int {
        val translucent = isTranslucent
        return if (translucent == null) {
            if (alpha < 255) PixelFormat.TRANSLUCENT else PixelFormat.OPAQUE /*不需要alpha通道*/
        } else {
            if (translucent) PixelFormat.TRANSLUCENT else PixelFormat.OPAQUE
        }
    }
}

/**附着的[View]*/
val Drawable.attachView: View?
    get() = if (callback is View) callback as? View else null