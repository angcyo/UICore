package com.angcyo.media.audio.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.angcyo.library.ex.dpi
import com.angcyo.media.R

/**
 * 语音播放提示控件
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/04/17
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class VoiceView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    private val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }

    var color: Int = Color.parseColor("#020202")
        set(value) {
            field = value
            paint.color = value
        }

    /**
     * 线的厚度
     * */
    var lineWidth = 2 * dpi

    /**线之间的间隙*/
    var lineSpace = 1 * dpi

    /**每增加一根线, 起始角度偏移多少度*/
    var stepAngle = 3

    /**每增加一根线, 高度增加多少*/
    var stepHeight = 4 * dpi

    /**线开始的角度偏移*/
    var startAngle = 40f

    /**线的数量*/
    var lineCount = 2

    private var drawCount = -1

    val tempRectF: RectF by lazy {
        RectF()
    }

    init {
        val array = context.obtainStyledAttributes(attributeSet, R.styleable.VoiceView)
        color = array.getColor(R.styleable.VoiceView_r_voice_color, color)
        lineWidth =
            array.getDimensionPixelOffset(R.styleable.VoiceView_r_voice_line_width, lineWidth)
        lineSpace =
            array.getDimensionPixelOffset(R.styleable.VoiceView_r_voice_line_space, lineSpace)
        stepHeight =
            array.getDimensionPixelOffset(R.styleable.VoiceView_r_voice_step_height, stepHeight)
        stepAngle =
            array.getInt(R.styleable.VoiceView_r_voice_step_angle, stepAngle)
        lineCount =
            array.getInt(R.styleable.VoiceView_r_voice_line_count, lineCount)
        startAngle =
            array.getInt(R.styleable.VoiceView_r_voice_start_angle, startAngle.toInt()).toFloat()
        array.recycle()

        paint.color = color
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY) {
            //wrap_content unspecified
            //widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(measureDrawWidth(widthSize, widthMode), View.MeasureSpec.EXACTLY);
            widthSize =
                paddingLeft + paddingRight + lineWidth * 2 * (lineCount + 2) + lineSpace * (lineCount - 1)
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            //wrap_content unspecified
            //heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(measureDrawHeight(heightSize, heightMode), View.MeasureSpec.EXACTLY);
            heightSize = paddingTop + paddingBottom + lineWidth * lineCount * 4
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val drawHeight = measuredHeight - paddingTop - paddingBottom
        val cr: Float = lineWidth.toFloat()
        val cx: Float = paddingLeft + cr
        val cy: Float = (paddingTop + drawHeight / 2).toFloat()

        //最小的圆点
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 0f
        canvas.drawCircle(cx, cy, cr, paint)

        //扇形
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = lineWidth.toFloat()

        for (i in 1..(if (drawCount >= 0) drawCount else lineCount)) {
            tempRectF.set(
                cx - cr - lineSpace * i - lineWidth / 2 * i - lineWidth * (i - 1),
                cy - cr - stepHeight * i,
                cx + cr + lineSpace * i + lineWidth / 2 * i + lineWidth * (i - 1),
                cy + cr + stepHeight * i
            )
            canvas.drawArc(
                tempRectF,
                -startAngle - stepAngle * i,
                startAngle * 2 + stepAngle * 2 * i,
                false,
                paint
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //play()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != VISIBLE) {
            stop()
        }
    }

    private val playRunnable: Runnable by lazy {
        Runnable {
            isPlaying = true

            if (drawCount < 0 || drawCount >= lineCount) {
                drawCount = -1
            }

            drawCount++

            postInvalidateOnAnimation()
            postDelayed(playRunnable, 300)
        }
    }

    private var isPlaying = false

    /**
     * 开始播放动画
     * */
    fun play() {
        if (isPlaying) {
            return
        }
        stop()
        isPlaying = true
        post(playRunnable)
    }

    /**
     * 停止动画
     * */
    fun stop() {
        isPlaying = false
        removeCallbacks(playRunnable)
        drawCount = -1
        postInvalidateOnAnimation()
    }
}