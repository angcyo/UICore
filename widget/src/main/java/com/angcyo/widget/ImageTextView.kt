package com.angcyo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.angcyo.drawable.dp
import com.angcyo.drawable.dpi
import com.angcyo.widget.base.drawCenterX
import com.angcyo.widget.drawable.DslAttrBadgeDrawable
import kotlin.math.max

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class ImageTextView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatImageView(context, attributeSet) {

    /**需要绘制显示的文本*/
    var showText: String? = null
        set(value) {
            field = value
            requestLayout()
        }
    var showTextSize: Float = 14 * dp
        set(value) {
            field = value
            textPaint.textSize = field
        }

    /**
     * 默认值请在init中设置
     * */
    var textOffset: Int = 0
        get() {
            if (showText.isNullOrEmpty()) {
                return 0
            }
            return field
        }
        set(value) {
            field = value
            requestLayout()
        }

    var textShowColor: Int = Color.WHITE
        set(value) {
            field = value
            postInvalidate()
        }

    var imageSize: Int = 0

    val textPaint: Paint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG)
    }

    /**角标绘制*/
    var dslBadeDrawable = DslAttrBadgeDrawable()

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ImageTextView)
        showText = typedArray.getString(R.styleable.ImageTextView_r_show_text)
        showTextSize =
            typedArray.getDimensionPixelOffset(
                R.styleable.ImageTextView_r_show_text_size,
                showTextSize.toInt()
            )
                .toFloat()
        textOffset =
            typedArray.getDimensionPixelOffset(R.styleable.ImageTextView_r_text_offset, 6 * dpi)
        textShowColor =
            typedArray.getColor(R.styleable.ImageTextView_r_show_text_color, textShowColor)
        typedArray.recycle()

        dslBadeDrawable.initAttribute(context, attributeSet)
        dslBadeDrawable.callback = this
    }

    override fun verifyDrawable(dr: Drawable): Boolean {
        return super.verifyDrawable(dr) || dr == dslBadeDrawable
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val wMode = MeasureSpec.getMode(widthMeasureSpec)

        val minWidth = minimumWidth

        val textMeasureWidth = textWidth

        if (drawable == null || drawable.intrinsicWidth < 0) {
            imageSize = 0

            //无图片
            if (wMode != MeasureSpec.EXACTLY) {
                if (!TextUtils.isEmpty(showText)) {
                    val width =
                        (paddingLeft + paddingRight + textMeasureWidth).toInt()
                    setMeasuredDimension(
                        max(width, minWidth),
                        measuredHeight
                    )
                }
            }
        } else {
            //有图片
            imageSize = drawable.intrinsicWidth
            if (wMode != MeasureSpec.EXACTLY) {

                if (!TextUtils.isEmpty(showText)) {
                    val width =
                        (paddingLeft + paddingRight + imageSize + textOffset + textMeasureWidth).toInt()

                    setMeasuredDimension(
                        max(width, minWidth),
                        measuredHeight
                    )
                }
            }
        }

        //L.e("call: onMeasure -> $measuredWidth $wSize $showText")
    }

    override fun onDraw(canvas: Canvas) {
        if (!TextUtils.isEmpty(showText)) {

            textPaint.color = textShowColor

            val drawHeight = measuredHeight - paddingTop - paddingBottom
            val drawWidth = measuredWidth - paddingLeft - paddingRight

            val textMeasureWidth = textWidth
            if (imageSize > 0) {
                val centerWidth = imageSize + textOffset + textMeasureWidth
                val centerX = paddingLeft + centerWidth / 2
                val textDrawX = paddingLeft + imageSize + textOffset
                val srcOffset = imageSize / 2 + textOffset + (centerX - textDrawX)

                canvas.save()
                canvas.translate(-srcOffset, 0f)
                super.onDraw(canvas)
                canvas.restore()

                //绘制需要显示的文本文本
                canvas.drawText(
                    showText!!,
                    textDrawX.toFloat(),//getDrawCenterCx() - textMeasureWidth / 2 + textOffset,//paddingLeft + textOffset - 4 * density + drawWidth / 2 - imageSize / 2,
                    paddingTop + drawHeight / 2 + textHeight / 2 - textPaint.descent(),
                    textPaint
                )
            } else {
                super.onDraw(canvas)

                //绘制需要显示的文本文本
                canvas.drawText(
                    showText!!,
                    drawCenterX - textMeasureWidth / 2,
                    paddingTop + drawHeight / 2 + textHeight / 2 - textPaint.descent(),
                    textPaint
                )
            }
        } else {
            super.onDraw(canvas)
        }

        dslBadeDrawable.apply {
            setBounds(0, 0, measuredWidth, measuredHeight)
            draw(canvas)
        }
    }

    val textHeight: Float
        get() {
            textPaint.textSize = showTextSize
            return textPaint.descent() - textPaint.ascent()
        }

    val textWidth: Float
        get() {
            if (showText.isNullOrEmpty()) {
                return 0f
            }
            textPaint.textSize = showTextSize
            return textPaint.measureText(showText)
        }
}
