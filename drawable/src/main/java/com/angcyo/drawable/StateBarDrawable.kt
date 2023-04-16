package com.angcyo.drawable

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library._refreshRateRatio
import com.angcyo.library.ex._color
import com.angcyo.library.ex.createPaint

/**
 * 状态显示的控件
 * 显示进行中...
 * 显示完成
 * 显示失败等状态
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class StateBarDrawable : AbsDslDrawable() {

    companion object {
        /**正常状态*/
        const val STATE_NORMAL = 0

        /**进行中*/
        const val STATE_ING = 1

        /**成功*/
        const val STATE_SUCCESS = 2

        /**失败*/
        const val STATE_ERROR = 3
    }

    var state: Int = STATE_NORMAL
        set(value) {
            field = value
            invalidateSelf()
        }

    var ingColor: Int = _color(R.color.colorAccent)
    var successColor: Int = _color(R.color.success)
    var errorColor: Int = _color(R.color.error)

    private val paint = createPaint(style = Paint.Style.FILL)

    override fun draw(canvas: Canvas) {
        when (state) {
            STATE_ING -> drawIng(canvas)
            STATE_SUCCESS -> drawRect(canvas, successColor)
            STATE_ERROR -> drawRect(canvas, errorColor)
        }
    }

    /**进度[0~200]*/
    var ingProgress: Float = 0f
    private var ingStep: Float = 3f

    /**绘制进行中*/
    private fun drawIng(canvas: Canvas) {
        paint.color = ingColor
        drawRect.set(bounds.left, bounds.top, bounds.right, bounds.bottom)
        if (ingProgress > 100) {
            val progress = ingProgress - 100
            drawRect.left = drawRect.left + (drawRect.width() * progress / 100f).toInt()
        } else {
            drawRect.right = (drawRect.left + drawRect.width() * ingProgress / 100f).toInt()
        }
        canvas.drawRect(drawRect, paint)

        if (ingProgress >= 200) {
            ingProgress = 0f
        } else {
            ingProgress += ingStep / _refreshRateRatio
        }

        invalidateSelf()
    }

    private fun drawRect(canvas: Canvas, color: Int) {
        paint.color = color
        drawRect.set(bounds.left, bounds.top, bounds.right, bounds.bottom)
        canvas.drawRect(drawRect, paint)
    }

}