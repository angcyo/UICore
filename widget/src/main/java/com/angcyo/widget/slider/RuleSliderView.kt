package com.angcyo.widget.slider

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Px
import androidx.core.graphics.withClip
import androidx.core.math.MathUtils
import androidx.core.view.GestureDetectorCompat
import com.angcyo.library.ex.evaluateColor
import com.angcyo.library.ex.textHeight
import com.angcyo.tablayout.textWidth
import kotlin.math.abs

/**
 * 带刻度的滑块
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/27
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class RuleSliderView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    /**滑轨的高度*/
    @Px
    var sliderHeight: Int = -1

    /**滑轨的圆角*/
    var sliderRound: Int = -1

    /**浮子的半径*/
    @Px
    var thumbRadius: Int = -1

    /**浮子上下距离, 预留足够多的位置, 用来显示上面的刻度, 和下面的文本*/
    var thumbOffsetTop: Int = -1
    var thumbOffsetBottom: Int = -1

    /**当前进度[0~100]*/
    var sliderProgress: Int = 50

    /**格式化进度*/
    var formatProgressText: (progress: Int) -> String = { "$it" }

    /**刻度列表*/
    var ruleList = mutableListOf<RuleInfo>()

    /**滑块背景颜色*/
    var sliderBgColor = Color.DKGRAY

    /**滑轨的渐变颜色值*/
    //蓝色 #96E1FF -》#00C1FF
    //粉色 #FF6CBC -》 #FF008A
    var sliderColors = intArrayOf(
        Color.parseColor("#96E1FF"),
        Color.parseColor("#00C1FF"),
        Color.parseColor("#FF6CBC"),
        Color.parseColor("#FF008A"),
    )

    //滑轨画笔
    val sliderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    //浮子画笔
    val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    //刻度画笔
    val rulePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    init {
        val dp = context.resources.displayMetrics.density
        sliderHeight = 4 * dp.toInt()
        sliderRound = 25 * dp.toInt()
        thumbRadius = 10 * dp.toInt()
        thumbOffsetTop = 4 * dp.toInt()
        thumbOffsetBottom = 40 * dp.toInt()

        //textSize
        rulePaint.textSize = 14 * dp
        thumbPaint.textSize = 14 * dp
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSpec = heightMeasureSpec
        val heightMode = MeasureSpec.getMode(heightSpec)
        if (heightMode != MeasureSpec.EXACTLY) {
            val heightSize =
                thumbRadius * 2 + thumbOffsetTop + thumbOffsetBottom + paddingTop + paddingBottom
            heightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

    //浮子中点x坐标
    val thumbCenterX: Float
        get() = calcCenterX(sliderProgress)

    //浮子中点y坐标
    val thumbCenterY: Float
        get() = (paddingTop + thumbOffsetTop + thumbRadius).toFloat()

    val sliderRect = RectF()

    var sliderShader: Shader? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sliderRect.set(
            paddingLeft.toFloat(),
            (thumbCenterY - sliderHeight / 2),
            (measuredWidth - paddingRight).toFloat(),
            (thumbCenterY + sliderHeight / 2)
        )
        sliderShader = LinearGradient(
            sliderRect.left,
            0f,
            sliderRect.right,
            0f,
            sliderColors,
            null,
            Shader.TileMode.REPEAT
        )
    }

    /**计算指定进度, 绘制的x坐标*/
    fun calcCenterX(progress: Int): Float {
        return sliderRect.left + sliderRect.width() * progress / 100
    }

    val _ruleRect = RectF()
    val _progressRect = RectF()
    val _progressTextRect = RectF()

    fun getProgressRect(): RectF {
        _progressRect.set(sliderRect.left, sliderRect.top, thumbCenterX, sliderRect.bottom)
        return _progressRect
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //绘制滑轨
        sliderPaint.shader = null
        sliderPaint.color = sliderBgColor
        canvas.drawRoundRect(sliderRect, sliderRound.toFloat(), sliderRound.toFloat(), sliderPaint)
        sliderPaint.shader = sliderShader
        canvas.withClip(getProgressRect()) {
            canvas.drawRoundRect(
                sliderRect,
                sliderRound.toFloat(),
                sliderRound.toFloat(),
                sliderPaint
            )
        }

        //刻度
        if (isTouchDown) {
            ruleList.forEach { info ->
                rulePaint.color = info.color
                val x = calcCenterX(info.progress)
                //rect
                _ruleRect.set(
                    x - info.width / 2,
                    sliderRect.top - info.height,
                    x + info.width / 2,
                    sliderRect.top
                )
                canvas.drawRect(_ruleRect, rulePaint)
                //text
                rulePaint.color = info.textColor
                info.text?.let {
                    drawText(canvas, it, info.progress, rulePaint)
                }
            }
        }

        //绘制浮子
        thumbPaint.color = evaluateColor(sliderProgress / 100f, sliderColors, null)
        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbRadius.toFloat(), thumbPaint)

        //浮子进度
        drawProgressText(canvas)
    }

    /**在指定进度位置绘制文本*/
    fun drawText(canvas: Canvas, text: String, progress: Int, paint: Paint, offsetY: Float = 0f) {
        val x = calcCenterX(progress)
        val textWidth = paint.textWidth(text)
        val textHeight = paint.textHeight()
        canvas.drawText(
            text,
            x - textWidth / 2,
            sliderRect.bottom + textHeight + offsetY,
            paint
        )
    }

    fun drawProgressText(canvas: Canvas) {
        val x = thumbCenterX
        val progressText = formatProgressText(sliderProgress)
        val textWidth = thumbPaint.textWidth(progressText)
        val textHeight = thumbPaint.textHeight()

        val offsetY = 8
        val offsetWidth = 40
        val top = thumbCenterY + thumbRadius + offsetY
        _progressTextRect.set(
            x - textWidth / 2 - offsetWidth / 2,
            top,
            x + textWidth / 2 + offsetWidth / 2,
            top + textHeight
        )
        thumbPaint.shader = LinearGradient(
            _progressTextRect.left,
            0f,
            _progressTextRect.right,
            0f,
            sliderColors.reversed().toIntArray(),
            null,
            Shader.TileMode.REPEAT
        )
        canvas.drawRoundRect(
            _progressTextRect, sliderRound.toFloat(),
            sliderRound.toFloat(),
            thumbPaint
        )

        thumbPaint.shader = null
        thumbPaint.color = Color.WHITE
        canvas.drawText(
            progressText,
            x - textWidth / 2,
            _progressTextRect.bottom - thumbPaint.descent(),
            thumbPaint
        )
    }

    //手势检测
    val _gestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                _onTouchMoveTo(e.x, e.y, false)
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val absX = abs(distanceX)
                val absY = abs(distanceY)

                var handle = false
                if (absX > absY && e2 != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    _onTouchMoveTo(e2.x, e2.y, false)
                    handle = true
                }
                return handle
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (velocityX > 0) {
                    //向右快速滑动
                    for (info in ruleList) {
                        if (info.progress > sliderProgress) {
                            updateProgress(info.progress)
                            break
                        }
                    }
                } else {
                    //向左快速滑动
                    for (info in ruleList.reversed()) {
                        if (info.progress < sliderProgress) {
                            updateProgress(info.progress)
                            break
                        }
                    }
                }
                return true
            }
        })
    }

    /**回调监听*/
    var onSliderConfig: SliderConfig? = null

    /**是否按下*/
    var isTouchDown = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        _gestureDetector.onTouchEvent(event)

        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            isTouchDown = true
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isTouchDown = false
            parent.requestDisallowInterceptTouchEvent(false)
            onSliderConfig?.apply { onSeekTouchEnd(sliderProgress, sliderProgress / 100f) }
        }
        invalidate()
        return true
    }

    /**手指移动*/
    fun _onTouchMoveTo(x: Float, y: Float, isFinish: Boolean) {
        val progress: Int = (((x - sliderRect.left) / sliderRect.width()) * 100).toInt()
        updateProgress(progress)
    }

    fun updateProgress(progress: Int) {
        sliderProgress = validProgress(progress)
        onSliderConfig?.apply { onSeekChanged(sliderProgress, sliderProgress / 100f, true) }
        invalidate()
    }

    /**限制设置的非法进度值*/
    fun validProgress(progress: Int): Int {
        return MathUtils.clamp(progress, 0, 100)
    }

    //</editor-fold desc="Touch事件">

    fun config(action: SliderConfig.() -> Unit) {
        if (onSliderConfig == null) {
            onSliderConfig = SliderConfig()
        }
        onSliderConfig?.action()
    }

    /**刻度信息*/
    data class RuleInfo(
        //刻度的颜色
        val color: Int = Color.RED,
        //刻度的文本
        val text: String? = null,
        //刻度所在的进度
        val progress: Int = 10,
        //刻度文本的颜色
        val textColor: Int = Color.DKGRAY,
        //宽度
        @Px
        val width: Int = 4,
        //高度
        @Px
        val height: Int = 20,
    )

    open class SliderConfig {
        /**进度改变回调,
         * [value] 进度值
         * [fraction] 进度比例
         * [fromUser] 是否是用户触发*/
        var onSeekChanged: (value: Int, fraction: Float, fromUser: Boolean) -> Unit = { _, _, _ -> }

        /**Touch结束后的回调*/
        var onSeekTouchEnd: (value: Int, fraction: Float) -> Unit = { _, _ -> }
    }
}