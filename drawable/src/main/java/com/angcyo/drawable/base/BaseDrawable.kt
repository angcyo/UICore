package com.angcyo.drawable.base

import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.view.View
import com.angcyo.library.ex.dp

/**
 * 基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/03
 */
abstract class BaseDrawable : Drawable() {

    /**画笔*/
    val textPaint: TextPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            style = Paint.Style.FILL
            textSize = 12 * dp
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

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

    override fun setAlpha(alpha: Int) {
        if (textPaint.alpha != alpha) {
            textPaint.alpha = alpha
            invalidateSelf()
        }
    }

    override fun getAlpha(): Int {
        return textPaint.alpha
    }

    override fun getColorFilter(): ColorFilter? {
        return textPaint.colorFilter
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        textPaint.colorFilter = colorFilter
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