package com.angcyo.widget.progress

import android.animation.Animator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.angcyo.library.ex.*
import com.angcyo.widget.R
import com.angcyo.widget.base.InvalidateProperty
import kotlin.math.min

/**
 * 王者荣耀loading动画控件
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/04/17
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class CircleLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }

    /** 进度弧形的 宽度 */
    var width = 3 * dp
        set(value) {
            field = value
            paint.strokeWidth = value

            resetRect()
        }

    val drawRectF: RectF by lazy {
        RectF()
    }

    /** 每帧增加的速率 */
    var rotateStep = 4

    /** 颜色, 透明到此颜色的渐变, 进度颜色 */
    var loadingColor: Int = -1 //Color.parseColor("#3965D6")
        set(value) {
            field = value
            singleShader = createSingleShader(loadingStartColor, value)
            multiShader = createMultiShader(loadingStartColor, value)
            postInvalidate()
        }

    /**不确定进度时有效*/
    var loadingStartColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            singleShader = createSingleShader(value, loadingColor)
            multiShader = createMultiShader(value, loadingColor)
            postInvalidate()
        }

    /** 单条时候的颜色渐变 [Shader], 不确定进度时有效*/
    var singleShader: Shader? = null

    /** 双条的时候颜色渐变 [Shader], 不确定进度时有效*/
    var multiShader: Shader? = null

    /** 双龙戏珠模式 */
    var isMultiMode = false

    /** 是否绘制背景, 背景是一个圆形色块 */
    var drawLoadingBackground = false

    /** 背景颜色 */
    var loadingBackgroundColor = Color.WHITE

    /**是否是不确定的进度*/
    var isIndeterminate: Boolean by InvalidateProperty(true)

    /**确定进度下的进度值[0-100]*/
    var progress: Int = 0
        set(value) {
            field = validProgress(value)
            postInvalidate()
        }

    var progressMaxValue = 100

    val tempRectF: RectF by lazy {
        RectF()
    }

    val drawTempRectF: RectF by lazy {
        RectF()
    }

    //当前旋转的角度
    var _rotateDegrees = 0f

    //绘制开始时的角度
    var startAngle = 0f

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleLoadingView)
        width = typedArray.getDimensionPixelOffset(
            R.styleable.CircleLoadingView_r_circle_load_width,
            width.toInt()
        ).toFloat()
        rotateStep =
            typedArray.getInt(R.styleable.CircleLoadingView_r_circle_load_rotate_step, rotateStep)
        loadingColor =
            typedArray.getColor(
                R.styleable.CircleLoadingView_r_circle_load_color,
                getColor(R.color.colorAccent)
            )
        loadingStartColor =
            typedArray.getColor(
                R.styleable.CircleLoadingView_r_circle_load_start_color,
                loadingStartColor
            )
        loadingBackgroundColor =
            typedArray.getColor(
                R.styleable.CircleLoadingView_r_circle_load_draw_bg_color,
                loadingBackgroundColor
            )
        isMultiMode = typedArray.getBoolean(
            R.styleable.CircleLoadingView_r_circle_load_is_multi_mode,
            isMultiMode
        )
        drawLoadingBackground =
            typedArray.getBoolean(
                R.styleable.CircleLoadingView_r_circle_load_draw_bg,
                drawLoadingBackground
            )

        isIndeterminate = typedArray.getBoolean(
            R.styleable.CircleLoadingView_r_circle_load_is_indeterminate,
            isIndeterminate
        )
        progress = typedArray.getInt(R.styleable.CircleLoadingView_r_circle_load_progress, progress)
        startAngle =
            typedArray.getFloat(R.styleable.CircleLoadingView_r_circle_load_start_angle, startAngle)

        typedArray.recycle()
    }

    private fun createSingleShader(startColor: Int, endColor: Int): SweepGradient {
        return SweepGradient(
            0f, 0f,
            intArrayOf(startColor, endColor),
            floatArrayOf(0.2f, 1f)
        )
    }

    private fun createMultiShader(startColor: Int, endColor: Int): SweepGradient {
        return SweepGradient(
            0f, 0f,
            intArrayOf(startColor, endColor),
            floatArrayOf(0.6f, 1f)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetRect()
    }

    private fun resetRect() {
        drawRectF.set(
            paddingLeft.toFloat(), paddingTop.toFloat(),
            (measuredWidth - paddingRight).toFloat(), (measuredHeight - paddingBottom).toFloat()
        )
        tempRectF.set(drawRectF)
        tempRectF.inset(width / 2, width / 2)

        val size = min(tempRectF.width(), tempRectF.height())
        drawTempRectF.set(
            -size / 2, -size / 2,
            size / 2, size / 2
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        canvas.translate(tempRectF.centerX(), tempRectF.centerY())

        //圆形背景
        if (drawLoadingBackground) {
            paint.shader = null
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.color = loadingBackgroundColor

            val size: Float = min(drawTempRectF.width(), drawTempRectF.height())

            canvas.drawCircle(0f, 0f, size / 2, paint)
        }

        paint.style = Paint.Style.STROKE

        //旋转的角度
        canvas.rotate(_rotateDegrees)

        if (isIndeterminate) {
            //不确定的进度

            _rotateDegrees += rotateStep
            if (_rotateDegrees > 360) {
                _rotateDegrees = -1f
            }

            if (isMultiMode) {
                //双线模式
                paint.shader = multiShader
                canvas.drawArc(drawTempRectF, startAngle, 360f, false, paint)
                canvas.rotate(180f)
                canvas.drawArc(drawTempRectF, startAngle, 360f, false, paint)
            } else {
                //单线模式
                paint.shader = singleShader
                canvas.drawArc(drawTempRectF, startAngle, 360f, false, paint)
            }

            if (visibility == VISIBLE) {
                postInvalidate()
            }
        } else {
            paint.shader = null
            paint.color = loadingColor

            if (isMultiMode) {
                //双线模式
                val sweepAngle = 180f * progress / 100
                canvas.drawArc(drawTempRectF, startAngle, sweepAngle, false, paint)
                canvas.rotate(180f)
                canvas.drawArc(drawTempRectF, startAngle, sweepAngle, false, paint)
            } else {
                //单线模式
                val sweepAngle = 360f * progress / 100
                canvas.drawArc(drawTempRectF, startAngle, sweepAngle, false, paint)
            }
        }

        canvas.restore()
    }

    //<editor-fold desc="进度动画控制">

    var _animtor: Animator? = null

    /**限制设置的非法进度值*/
    fun validProgress(progress: Int): Int {
        return clamp(progress, 0, progressMaxValue)
    }

    /**
     * 设置进度
     * @param progress [0-100]]
     * @param fromProgress 动画开始的进度, 默认是当前进度
     * @param animDuration 动画时长, 小于0, 不开启动画
     * */
    open fun setProgress(
        progress: Int,
        fromProgress: Int = progress,
        animDuration: Long = Anim.ANIM_DURATION
    ) {
        _animtor?.cancel()
        _animtor = null
        val p = validProgress(progress)
        if (animDuration >= 0 && fromProgress != p) {
            _animtor = anim(fromProgress, p) {
                onAnimatorConfig = {
                    it.duration = animDuration
                }
                onAnimatorUpdateValue = { value, _ ->
                    this@CircleLoadingView.progress = value as Int
                }
            }
        } else {
            this.progress = p
        }
    }

    //</editor-fold desc="进度动画控制">
}