package com.angcyo.drawable.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.ViewGroup
import com.angcyo.drawable.DslGravity
import com.angcyo.drawable.R
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library.ex._color
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import kotlin.math.max

/**
 * 根据[textGravity], 在指定的[Bound]中, 绘制指定的文本信息, 不支持span和换行
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslTextDrawable : AbsDslDrawable() {

    /**文本大小*/
    var textSize = 12 * dp
        set(value) {
            field = value
            textPaint.textSize = field
        }

    /**文本颜色*/
    var textColor = _color(R.color.lib_text_color)
        set(value) {
            field = value
            textPaint.color = field
        }

    /**文本重力*/
    var textGravity = Gravity.CENTER

    /**绘制的文本*/
    var text: String? = null

    /**偏移纠正*/
    var textOffsetX = 0
    var textOffsetY = 0

    /**设置给[TextView]时, 并不会影响高度, 宽度会影响*/
    var textPaddingLeft = 0
    var textPaddingRight = 0
    var textPaddingTop = 0
    var textPaddingBottom = 0

    /**文本背景*/
    var textBgDrawable: Drawable? = null

    /**默认使用文本的宽高, -1,-1可以用时View的宽高*/
    var drawableWidth = ViewGroup.LayoutParams.WRAP_CONTENT
    var drawableHeight = ViewGroup.LayoutParams.WRAP_CONTENT

    val _dslGravity = DslGravity()

    init {
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.style = Paint.Style.FILL
    }

    //计算属性
    val textWidth: Float
        get() = textPaint.textWidth(text)

    val textHeight: Float
        get() = textPaint.textHeight()

    override fun draw(canvas: Canvas) {
        val drawText = text ?: return

        textBgDrawable?.apply {
            bounds = this@DslTextDrawable.bounds
            draw(canvas)
        }

        with(_dslGravity) {
            gravity = textGravity
            setGravityBounds(this@DslTextDrawable.bounds)
            applyGravity(textWidth, textHeight) { centerX, centerY ->

                val textDrawX: Float = centerX - _targetWidth / 2
                val textDrawY: Float = centerY + _targetHeight / 2

                //绘制文本
                canvas.drawText(
                    drawText,
                    textDrawX + textOffsetX + textPaddingLeft,
                    textDrawY - textPaint.descent() + textOffsetY + textPaddingTop,
                    textPaint
                )
            }
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
    }

    override fun getIntrinsicWidth(): Int {
        return if (drawableWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            max(
                textWidth.toInt(),
                textBgDrawable?.minimumWidth ?: 0
            ) + textPaddingLeft + textPaddingRight
        } else {
            super.getIntrinsicWidth()
        }
    }

    override fun getIntrinsicHeight(): Int {
        return if (drawableHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            max(
                textHeight.toInt(),
                textBgDrawable?.minimumHeight ?: 0
            ) + textPaddingTop + textPaddingBottom
        } else {
            super.getIntrinsicHeight()
        }
    }
}

/**快速创建[DslTextDrawable]*/
fun dslTextDrawable(text: CharSequence?, action: DslTextDrawable.() -> Unit = {}): DslTextDrawable {
    return DslTextDrawable().apply {
        this.text = text?.toString()
        action()
    }
}

/**绘制纯文件, 带背景颜色*/
fun dslFullTextDrawable(
    text: CharSequence?,
    action: DslTextDrawable.() -> Unit = {}
): DslTextDrawable {
    return DslTextDrawable().apply {
        this.text = text?.toString()

        drawableWidth = -1
        drawableHeight = -1
        textBgDrawable = ColorDrawable(_color(R.color.lib_theme_white_color))
        textColor = _color(R.color.text_primary_color)

        action()
    }
}