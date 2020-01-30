package com.angcyo.widget.progress

import android.animation.Animator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.math.MathUtils
import com.angcyo.drawable.base.DslGradientDrawable
import com.angcyo.drawable.text.DslTextDrawable
import com.angcyo.library.ex.toDp
import com.angcyo.library.ex.toDpi
import com.angcyo.widget.R
import com.angcyo.widget.base.anim
import com.angcyo.widget.base.getColor
import kotlin.math.max

/**
 * 提供一个高度可定义的标准进度条
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslProgressBar(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    /**进度条背景*/
    var progressBgDrawable: Drawable? = null
    /**进度条*/
    var progressTrackDrawable: Drawable? = null

    /**最大进度*/
    var progressMaxValue: Int = 100

    /**当前的进度*/
    var progressValue: Int = 0
        set(value) {
            field = validProgress(value)
            postInvalidate()
        }

    /**圆角大小, 未强制指定[progressBgDrawable] [progressTrackDrawable]的情况下生效*/
    var progressRadius: Int = 0

    /**是否显示进度提示文本, 在右边显示进度文本*/
    var showProgressText = false

    /**进度文本格式*/
    var progressTextFormat = "%s%%"
    /**文本大小*/
    var progressTextSize = 14.toDp()
    var progressTextMinWidth = 40.toDp()

    /**文本颜色*/
    var progressTextColor = Color.parseColor("#333333")
    /**文本距离进度偏移的距离*/
    var progressTextOffset = 10.toDpi()

    val _progressTextDrawable: DslTextDrawable = DslTextDrawable()
        get() {
            with(field) {
                textSize = progressTextSize
                textColor = progressTextColor
                text = progressTextFormat.format("${(_progressFraction * 100).toInt()}")
            }
            return field
        }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DslProgressBar)

        progressBgDrawable = typedArray.getDrawable(R.styleable.DslProgressBar_progress_bg_drawable)
        progressTrackDrawable =
            typedArray.getDrawable(R.styleable.DslProgressBar_progress_track_drawable)

        progressRadius = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_radius,
            progressRadius
        )
        progressTextOffset = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_text_offset,
            progressTextOffset
        )

        //检测是否需要渐变, 渐变至少需要2个色值
        if ((progressBgDrawable is ColorDrawable || progressBgDrawable == null) &&
            typedArray.hasValue(R.styleable.DslProgressBar_progress_bg_gradient_colors)
        ) {
            var colors =
                typedArray.getString(R.styleable.DslProgressBar_progress_bg_gradient_colors)

            if (progressBgDrawable is ColorDrawable) {
                val startColor = (progressBgDrawable as ColorDrawable).color
                colors = "$startColor,$colors"
            } else if (colors?.split(",")?.size ?: 0 <= 1) {
                colors = "$colors,$colors"
            }

            setBgGradientColors(colors)
        }

        if ((progressTrackDrawable is ColorDrawable || progressTrackDrawable == null) &&
            typedArray.hasValue(R.styleable.DslProgressBar_progress_track_drawable_gradient_colors)
        ) {
            var colors =
                typedArray.getString(R.styleable.DslProgressBar_progress_track_drawable_gradient_colors)

            if (progressTrackDrawable is ColorDrawable) {
                val startColor = (progressTrackDrawable as ColorDrawable).color
                colors = "$startColor,$colors"
            } else if (colors?.split(",")?.size ?: 0 <= 1) {
                colors = "$colors,$colors"
            }

            setTrackGradientColors(colors)
        } else if (progressTrackDrawable == null) {
            setTrackGradientColors("${getColor(R.color.colorPrimaryDark)},${getColor(R.color.colorPrimary)}")
        }
        //----end

        //提示文本
        showProgressText =
            typedArray.getBoolean(R.styleable.DslProgressBar_progress_show_text, showProgressText)
        progressTextSize = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_text_size,
            progressTextSize.toInt()
        ).toFloat()
        progressTextMinWidth = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_text_min_width,
            progressTextMinWidth.toInt()
        ).toFloat()
        progressTextColor =
            typedArray.getColor(R.styleable.DslProgressBar_progress_text_color, progressTextColor)
        progressTextFormat = typedArray.getString(R.styleable.DslProgressBar_progress_text_format)
            ?: progressTextFormat
        //---end

        progressValue = typedArray.getInt(
            R.styleable.DslProgressBar_progress_value,
            if (isInEditMode) 50 else progressValue
        )
        progressMaxValue =
            typedArray.getInt(R.styleable.DslProgressBar_progress_max_value, progressMaxValue)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBg(canvas)
        drawTrack(canvas)
        drawProgressText(canvas)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    //如果文本的宽度不固定, 进度会出现抖动的情况
    val _textWidth: Float
        get() = max(_progressTextDrawable.textWidth, progressTextMinWidth)

    open val _progressBound = Rect()
        get() {
            val right: Int = if (showProgressText) {
                (measuredWidth - paddingRight - progressTextOffset - _textWidth).toInt()
            } else {
                measuredWidth - paddingRight
            }

            field.set(paddingLeft, paddingTop, right, measuredHeight - paddingBottom)
            return field
        }

    //绘制背景
    open fun drawBg(canvas: Canvas) {
        progressBgDrawable?.apply {
            bounds = _progressBound
            draw(canvas)
        }
    }

    //0.1 0.8 0.9进度
    val _progressFraction: Float
        get() = progressValue * 1f / progressMaxValue

    //绘制进度轨道
    open fun drawTrack(canvas: Canvas) {
        val pBound = _progressBound
        progressTrackDrawable?.apply {

            setBounds(
                pBound.left,
                pBound.top,
                (pBound.left + pBound.width() * _progressFraction).toInt(),
                pBound.bottom
            )

            draw(canvas)
        }
    }

    open fun drawProgressText(canvas: Canvas) {
        if (showProgressText) {
            with(_progressTextDrawable) {
                val pBound = _progressBound
                setBounds(
                    pBound.right + progressTextOffset,
                    pBound.top,
                    measuredWidth - paddingRight,
                    pBound.bottom
                )
                draw(canvas)
            }
        }
    }

    fun setBgGradientColors(color: String?) {
        DslGradientDrawable().apply {
            gradientColors = _fillColor(color)
            _fillRadii(gradientRadii, progressRadius)
            updateOriginDrawable()
            progressBgDrawable = originDrawable
        }
    }

    fun setTrackGradientColors(color: String?) {
        DslGradientDrawable().apply {
            gradientColors = _fillColor(color)
            _fillRadii(gradientRadii, progressRadius)
            updateOriginDrawable()
            progressTrackDrawable = originDrawable
        }
    }

    var _animtor: Animator? = null

    /**限制设置的非法进度值*/
    fun validProgress(progress: Int): Int {
        return MathUtils.clamp(progress, 0, progressMaxValue)
    }

    /**
     * 设置进度
     * @param fromProgress 动画开始的进度, 默认是当前进度
     * @param animDuration 动画时长, 小于0, 不开启动画
     * */
    open fun setProgress(
        progress: Int,
        fromProgress: Int = progressValue,
        animDuration: Long = 300
    ) {
        _animtor?.cancel()
        _animtor = null
        val p = validProgress(progress)
        if (animDuration >= 0) {
            _animtor = anim(fromProgress, p) {
                onAnimatorConfig = {
                    it.duration = animDuration
                }
                onAnimatorUpdateValue = { value, _ ->
                    this@DslProgressBar.progressValue = value as Int
                }
            }
        } else {
            this.progressValue = p
        }
    }
}