package com.angcyo.widget.progress

import android.animation.Animator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.core.graphics.withSave
import androidx.core.math.MathUtils
import com.angcyo.drawable.base.DslGradientDrawable
import com.angcyo.drawable.text.DslTextDrawable
import com.angcyo.library.ex.*
import com.angcyo.widget.R
import com.angcyo.widget.base.atMost
import com.angcyo.widget.base.getMode
import kotlin.math.max
import kotlin.text.format

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

    /**第二进度*/
    var progressSecondDrawable: Drawable? = null

    /**进度条*/
    var progressTrackDrawable: Drawable? = null

    /**最大进度*/
    var progressMaxValue: Int = 100

    /**当前的进度
     * [0~100]*/
    var progressValue: Int = 0
        set(value) {
            val old = field
            field = validProgress(value)

            if (enableShowHideProgress) {
                if (old <= 0 && field > 0) {
                    animate().translationY(0f).setDuration(Anim.ANIM_DURATION).start()
                } else if (field >= progressMaxValue) {
                    animate().translationY((-measuredHeight).toFloat())
                        .setDuration(Anim.ANIM_DURATION).start()
                }
            }
            postInvalidate()
        }

    /**第二进度*/
    var progressSecondValue: Int = 0
        set(value) {
            field = validProgress(value)
            postInvalidate()
        }

    /**是否激活流光进度效果*/
    var enableProgressFlowMode: Boolean = false

    /**圆角大小, 未强制指定[progressBgDrawable] [progressTrackDrawable]的情况下生效*/
    var progressRadius: Int = 5 * dpi

    /**Wrap_Content时的最小高度*/
    var progressMinHeight = 8 * dpi

    /**绘制进度Drawable时, 是否使用clip模式. 这样可以实现左右是圆角, 中间不是圆角的情况*/
    var progressClipMode = false

    //<editor-fold desc="最右边文本配置">

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

    var progressTextFormatAction: (DslProgressBar) -> String = {
        progressTextFormat.format("${(_progressFraction * 100).toInt()}")
    }

    var progressCenterTextFormatAction: (DslProgressBar) -> String = {
        progressCenterTextFormat.format("${(_progressFraction * 100).toInt()}")
    }

    //</editor-fold desc="最右边文本配置">

    //<editor-fold desc="居中文本配置">

    var showProgressCenterText = false
    var progressCenterTextFormat = "%s%%"
    var progressCenterTextSize = 14.toDp()
    var progressCenterTextColor = Color.parseColor("#333333")

    //在进度上clip时的文本颜色
    var progressCenterTextClipColor = Color.WHITE

    //</editor-fold desc="居中文本配置">

    /**激活有进度和满进度时的进场/退场动画*/
    var enableShowHideProgress: Boolean = false

    val _progressTextDrawable: DslTextDrawable = DslTextDrawable()
        get() {
            with(field) {
                textSize = progressTextSize
                textColor = progressTextColor
                text = progressTextFormatAction(this@DslProgressBar)
            }
            return field
        }

    val _progressCenterTextDrawable: DslTextDrawable = DslTextDrawable()
        get() {
            with(field) {
                textGravity = Gravity.CENTER
                textSize = progressCenterTextSize
                text = progressCenterTextFormatAction(this@DslProgressBar)
            }
            return field
        }

    //流光进度
    var _progressFlowValue: Int = 0

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DslProgressBar)

        progressBgDrawable = typedArray.getDrawable(R.styleable.DslProgressBar_progress_bg_drawable)
        progressSecondDrawable =
            typedArray.getDrawable(R.styleable.DslProgressBar_progress_second_drawable)
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
        } else if (progressBgDrawable == null) {
            if (isInEditMode) {
                setBgGradientColors("${getColor(R.color.lib_progress_bg_color)},${getColor(R.color.lib_progress_bg_color)}")
            }
        }

        //第二进度Drawable
        if ((progressSecondDrawable is ColorDrawable || progressSecondDrawable == null) &&
            typedArray.hasValue(R.styleable.DslProgressBar_progress_second_gradient_colors)
        ) {
            var colors =
                typedArray.getString(R.styleable.DslProgressBar_progress_second_gradient_colors)

            if (progressSecondDrawable is ColorDrawable) {
                val startColor = (progressSecondDrawable as ColorDrawable).color
                colors = "$startColor,$colors"
            } else if (colors?.split(",")?.size ?: 0 <= 1) {
                colors = "$colors,$colors"
            }

            setSecondGradientColors(colors)
        } else if (progressSecondDrawable == null) {
            setSecondGradientColors("${getColor(R.color.lib_progress_second_bg_color)},${getColor(R.color.lib_progress_second_bg_color)}")
        }

        //进度轨道Drawable
        if ((progressTrackDrawable is ColorDrawable || progressTrackDrawable == null) &&
            typedArray.hasValue(R.styleable.DslProgressBar_progress_track_gradient_colors)
        ) {
            var colors =
                typedArray.getString(R.styleable.DslProgressBar_progress_track_gradient_colors)

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

        progressMaxValue =
            typedArray.getInt(R.styleable.DslProgressBar_progress_max_value, progressMaxValue)

        progressValue = typedArray.getInt(
            R.styleable.DslProgressBar_progress_value,
            if (isInEditMode) 50 else progressValue
        )
        progressSecondValue = typedArray.getInt(
            R.styleable.DslProgressBar_progress_second_value,
            if (isInEditMode) 70 else progressSecondValue
        )

        enableShowHideProgress = typedArray.getBoolean(
            R.styleable.DslProgressBar_progress_enable_show_hide_progress,
            enableShowHideProgress
        )

        progressClipMode =
            typedArray.getBoolean(R.styleable.DslProgressBar_progress_clip_mode, progressClipMode)
        enableProgressFlowMode = typedArray.getBoolean(
            R.styleable.DslProgressBar_enable_progress_flow_mode,
            enableProgressFlowMode
        )
        if (enableProgressFlowMode && isInEditMode) {
            _progressFlowValue = 50
        }

        //居中提示文本
        showProgressCenterText = typedArray.getBoolean(
            R.styleable.DslProgressBar_progress_show_center_text,
            showProgressCenterText
        )
        progressCenterTextSize = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_center_text_size,
            progressCenterTextSize.toInt()
        ).toFloat()
        progressCenterTextColor = typedArray.getColor(
            R.styleable.DslProgressBar_progress_center_text_color,
            progressCenterTextColor
        )
        progressCenterTextFormat =
            typedArray.getString(R.styleable.DslProgressBar_progress_center_text_format)
                ?: progressCenterTextFormat
        //---end

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (heightMeasureSpec.getMode() != MeasureSpec.EXACTLY) {
            super.onMeasure(
                widthMeasureSpec,
                atMost(progressMinHeight + paddingTop + paddingBottom)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (enableShowHideProgress) {
            if (progressValue <= 0) {
                translationY = (-measuredHeight).toFloat()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBg(canvas)
        drawSecondProgress(canvas)
        drawTrack(canvas) //进度
        drawProgressText(canvas)
        drawCenterProgressText(canvas)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    //如果文本的宽度不固定, 进度会出现抖动的情况
    val _textWidth: Float
        get() = max(_progressTextDrawable.textWidth, progressTextMinWidth)

    val _textHeight: Float
        get() = _progressTextDrawable.textHeight

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

    val _progressSecondFraction: Float
        get() = progressSecondValue * 1f / progressMaxValue

    //绘制第二进度
    open fun drawSecondProgress(canvas: Canvas) {
        val pBound = _progressBound
        progressSecondDrawable?.apply {

            setBounds(
                pBound.left,
                pBound.top,
                (pBound.left + pBound.width() * _progressSecondFraction).toInt(),
                pBound.bottom
            )

            draw(canvas)
        }
    }

    //绘制进度轨道
    open fun drawTrack(canvas: Canvas) {
        val pBound = _progressBound
        progressTrackDrawable?.apply {

            var progressRight = (pBound.left + pBound.width() * _progressFraction).toInt()

            val right = if (progressClipMode) {
                pBound.right
            } else {
                progressRight
            }

            setBounds(
                pBound.left,
                pBound.top,
                right,
                pBound.bottom
            )

            if (progressClipMode) {
                canvas.withSave {
                    clipRect(
                        pBound.left,
                        pBound.top,
                        progressRight,
                        pBound.bottom
                    )
                    draw(canvas)
                }
            } else {
                draw(canvas)
            }

            //流光效果
            if (enableProgressFlowMode) {
                val progressFlowRatio = _progressFlowValue / 100f
                setBounds(
                    pBound.left,
                    pBound.top,
                    (right * progressFlowRatio).toInt(),
                    pBound.bottom
                )
                progressRight = (progressRight * progressFlowRatio).toInt()
                if (progressClipMode) {
                    canvas.withSave {
                        clipRect(
                            pBound.left,
                            pBound.top,
                            progressRight,
                            pBound.bottom
                        )
                        draw(canvas)
                    }
                } else {
                    draw(canvas)
                }
                if (_progressFlowValue >= 100) {
                    _progressFlowValue = 0
                } else {
                    _progressFlowValue++
                }
                invalidate()
            }
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

    open fun drawCenterProgressText(canvas: Canvas) {
        if (showProgressCenterText) {
            val pBound = _progressBound
            val progressRight = (pBound.left + pBound.width() * _progressFraction).toInt()

            with(_progressCenterTextDrawable) {
                bounds = pBound
                //绘制左半边进度文本
                canvas.withSave {
                    textColor = progressCenterTextClipColor
                    clipRect(
                        pBound.left,
                        pBound.top,
                        progressRight,
                        pBound.bottom
                    )
                    draw(canvas)
                }
                //绘制右半边进度文本
                canvas.withSave {
                    textColor = progressCenterTextColor
                    clipRect(
                        progressRight,
                        pBound.top,
                        pBound.right,
                        pBound.bottom
                    )
                    draw(canvas)
                }
            }
        }
    }

    /**设置背景渐变的颜色*/
    fun setBgGradientColors(color: String?) {
        DslGradientDrawable().apply {
            if (color?.contains(",") == true) {
                gradientColors = _fillColor(color)
            } else if (!color.isNullOrEmpty()) {
                gradientSolidColor = if (color.startsWith("#")) {
                    Color.parseColor(color)
                } else {
                    color.toInt()
                }
            }
            _fillRadii(gradientRadii, progressRadius)
            updateOriginDrawable()
            progressBgDrawable = originDrawable
        }
        postInvalidate()
    }

    /**设置进度条渐变的颜色*/
    fun setTrackGradientColors(color: String?) {
        DslGradientDrawable().apply {
            if (color?.contains(",") == true) {
                gradientColors = _fillColor(color)
            } else if (!color.isNullOrEmpty()) {
                gradientSolidColor = if (color.startsWith("#")) {
                    Color.parseColor(color)
                } else {
                    color.toInt()
                }
            }
            _fillRadii(gradientRadii, progressRadius)
            updateOriginDrawable()
            progressTrackDrawable = originDrawable
        }
    }

    /**设置第二进度渐变的颜色*/
    fun setSecondGradientColors(color: String?) {
        DslGradientDrawable().apply {
            if (color?.contains(",") == true) {
                gradientColors = _fillColor(color)
            } else if (!color.isNullOrEmpty()) {
                gradientSolidColor = if (color.startsWith("#")) {
                    Color.parseColor(color)
                } else {
                    color.toInt()
                }
            }
            _fillRadii(gradientRadii, progressRadius)
            updateOriginDrawable()
            progressSecondDrawable = originDrawable
        }
    }

    var _animtor: Animator? = null

    /**限制设置的非法进度值*/
    fun validProgress(progress: Int): Int {
        return MathUtils.clamp(progress, 0, progressMaxValue)
    }

    /**
     * 设置进度
     * @param progress [0-100]]
     * @param fromProgress 动画开始的进度, 默认是当前进度
     * @param animDuration 动画时长, 小于0, 不开启动画
     * */
    open fun setProgress(
        progress: Int,
        fromProgress: Int = progressValue,
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
                    this@DslProgressBar.progressValue = value as Int
                }
            }
        } else {
            this.progressValue = p
        }
    }
}