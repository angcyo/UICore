package com.angcyo.drawable.base

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.size
import kotlin.math.max
import kotlin.math.min

/**
 * 结合多个[Drawable]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/29
 */
class CombineDrawable(val drawableList: MutableList<Drawable> = mutableListOf()) : BaseDrawable() {

    /**组合的方向*/
    var orientation: Int = LinearLayout.HORIZONTAL

    /**间隙*/
    var margin: Int = 2 * dpi

    fun addDrawable(drawable: Drawable?) {
        drawable ?: return
        drawableList.add(drawable)
    }

    fun removeDrawable(drawable: Drawable?) {
        drawable ?: return
        drawableList.remove(drawable)
    }

    override fun getIntrinsicWidth(): Int {
        var width = -1
        if (orientation == LinearLayout.VERTICAL) {
            drawableList.forEach {
                width = max(width, it.minimumWidth)
            }
        } else {
            drawableList.forEach {
                width += it.minimumWidth
            }
            width += margin * max(0, drawableList.size() - 1)
        }
        return width
    }

    override fun getIntrinsicHeight(): Int {
        var height = -1
        if (orientation == LinearLayout.VERTICAL) {
            drawableList.forEach {
                height += it.minimumHeight
            }
            height += margin * max(0, drawableList.size() - 1)
        } else {
            drawableList.forEach {
                height = max(height, it.minimumHeight)
            }
        }
        return height
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        var left = bounds.left
        var top = bounds.top
        val size = min(bounds.width(), bounds.height())
        drawableList.forEach {
            val w = min(size, it.minimumWidth)
            val h = min(size, it.minimumHeight)
            it.setBounds(left, top, left + w, top + h)
            it.draw(canvas)
            if (orientation == LinearLayout.VERTICAL) {
                top += margin + h
            } else {
                left += margin + w
            }
        }
    }
}