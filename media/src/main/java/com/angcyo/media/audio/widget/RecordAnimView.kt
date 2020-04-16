package com.angcyo.media.audio.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.angcyo.media.R
import com.angcyo.widget.base.getColor
import java.util.*

/**
 * 语音录制, 音频振幅提示控件
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/04/13
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class RecordAnimView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }

    var meterColor = Color.WHITE

    var meterDarkColor = getColor(R.color.text_sub_color)

    val dp: Float by lazy {
        resources.displayMetrics.density
    }

    val temptRect: RectF by lazy {
        RectF()
    }

    val temptPath: Path by lazy {
        Path()
    }

    //振幅的数量
    var count = 7
        set(value) {
            field = value
            postInvalidate()
        }

    //振幅之间的间隔
    var space = 6 * dp

    //var drawHeight = measuredHeight - paddingTop - paddingBottom
    //振幅的高度
    var lineHeight = 8 * dp //(drawHeight - space * (count - 1)) / count

    //振幅宽度递增的量
    var widthStep = 6 * dp

    //缺口的宽度
    var gapWidth = 5 * dp

    //最小振幅的宽度
    var minWidth = 20 * dp

    var drawCount = 1
        set(value) {
            field = value
            postInvalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val left = paddingLeft
        var bottom = measuredHeight - paddingBottom
        for (i in 0 until count) {
            temptRect.set(
                left.toFloat(),
                bottom - lineHeight,
                left + minWidth + i * widthStep,
                bottom.toFloat()
            )

            bottom = (bottom - lineHeight - space).toInt()

            temptPath.reset()
            temptPath.moveTo(temptRect.left, temptRect.top)
            temptPath.lineTo(temptRect.right, temptRect.top)
            temptPath.lineTo(temptRect.right - gapWidth, temptRect.bottom)
            temptPath.lineTo(temptRect.left, temptRect.bottom)

            //canvas.drawRect(temptRect, paint)
            if (i < drawCount) {
                paint.color = meterColor
            } else {
                paint.color = meterDarkColor
            }
            canvas.drawPath(temptPath, paint)
        }
    }

    val random: Random by lazy {
        Random(SystemClock.elapsedRealtimeNanos())
    }

    val animation: ValueAnimator by lazy {
        ObjectAnimator.ofInt(0, 360).apply {
            duration = 1000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART

            addUpdateListener {
                drawCount = random.nextInt(count - 1) + 1
                postInvalidateOnAnimation()
            }
        }
    }

    val randomTime = 300L

    val runnable: Runnable by lazy {
        Runnable {
            drawCount = random.nextInt(count - 1) + 1
            postInvalidateOnAnimation()

            if (drawCount > count - 1) {
                postDelayed(runnable, randomTime / 6)
            } else if (drawCount > count - 2) {
                postDelayed(runnable, randomTime / 5)
            } else if (drawCount > count - 3) {
                postDelayed(runnable, randomTime / 3)
            } else if (drawCount > count - 4) {
                postDelayed(runnable, randomTime / 2)
            } else {
                postDelayed(runnable, randomTime)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //postDelayed(runnable, randomTime)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //removeCallbacks(runnable)
    }

}