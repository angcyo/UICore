package com.angcyo.widget.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library.attachInEditMode
import com.angcyo.library.ex.have
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/21
 */
abstract class BaseDrawableView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    //集合
    val drawables = mutableListOf<AbsDslDrawable>()

    init {
        attachInEditMode()

        initDrawables(drawables)
        drawables.forEach {
            it.callback = this
            it.initAttribute(context, attributeSet)
        }
    }

    abstract fun initDrawables(list: MutableList<AbsDslDrawable>)

    //必须
    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || drawables.all { it == who }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (drawables.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            var width = 0
            var height = 0
            for (drawable in drawables) {
                width = max(width, drawable.minimumWidth)
                height = max(height, drawable.minimumHeight)
            }

            var widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            var heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            if (widthMode != MeasureSpec.EXACTLY) {
                widthSize = width + paddingLeft + paddingRight
            }
            if (heightMode != MeasureSpec.EXACTLY) {
                heightSize = height + paddingTop + paddingBottom
            }
            setMeasuredDimension(widthSize, heightSize)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            drawables.forEach {
                it.setBounds(
                    paddingLeft,
                    paddingTop,
                    getRight() - getLeft() - paddingRight,
                    getBottom() - getTop() - paddingBottom
                )
            }
        }
    }

    /**[draw]*/
    override fun draw(canvas: Canvas) {
        drawables.forEach {
            if (it.drawType.have(AbsDslDrawable.DRAW_TYPE_DRAW_BEFORE)) {
                it.draw(canvas)
            }
        }
        super.draw(canvas)
        drawables.forEach {
            if (it.drawType.have(AbsDslDrawable.DRAW_TYPE_DRAW_AFTER)) {
                it.draw(canvas)
            }
        }
    }

    /**[onDraw]*/
    override fun onDraw(canvas: Canvas) {
        drawables.forEach {
            if (it.drawType.have(AbsDslDrawable.DRAW_TYPE_ON_DRAW_BEFORE)) {
                it.draw(canvas)
            }
        }
        super.onDraw(canvas)
        drawables.forEach {
            if (it.drawType.have(AbsDslDrawable.DRAW_TYPE_ON_DRAW_AFTER)) {
                it.draw(canvas)
            }
        }
    }

    //方法
    operator fun <T> get(index: Int): T? {
        return drawables?.getOrNull(index) as? T?
    }

    /**在初始化方法中, 调用此方法. 会出现[drawables]为null的情况*/
    fun <T> firstDrawable(): T? {
        return drawables?.firstOrNull() as? T?
    }
}