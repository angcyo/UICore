package com.angcyo.widget.progress

import android.animation.Animator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.math.MathUtils
import com.angcyo.drawable.base.DslGradientDrawable
import com.angcyo.drawable.text.DslTextDrawable
import com.angcyo.library.ex.Anim
import com.angcyo.library.ex._color
import com.angcyo.library.ex.anim
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.evaluateColor
import com.angcyo.library.ex.getColor
import com.angcyo.library.ex.progressValueFraction
import com.angcyo.library.ex.size
import com.angcyo.library.ex.toDp
import com.angcyo.library.ex.toDpi
import com.angcyo.widget.R
import com.angcyo.widget.base.atMost
import com.angcyo.widget.base.getMode
import kotlin.math.max
import kotlin.math.min

/**
 * 提供一个高度可定义的标准进度条
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslProgressBar(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    companion object {

        /**
         * 获取渐变颜色指定进度对应的颜色值
         * [progress] 进度[0~1]
         * [colors] 颜色渐变的值
         * [colorStops] 渐变颜色分段比例, 不指定默认平分
         *
         */
        fun getGradientColor(
            progress: Float, colors: List<Int>, colorStops: List<Float>? = null
        ): Int {
            //debugger();

            val stops = mutableListOf<Float>()
            if (colorStops == null) {
                val size = colors.size()
                for (i in 0 until size) {
                    stops.add(i * (1f / (size - 1)))
                }
            } else {
                stops.addAll(colorStops)
            }

            //计算输出的颜色
            var color = colors.first()
            for (i in 0 until stops.size()) {
                if (progress <= stops[i]) {
                    val index = max(0, i - 1)
                    val startColor = colors[index]
                    val endColor = colors[min(index + 1, colors.size())]
                    val startProgress = stops[index]
                    val endProgress = stops[min(index + 1, stops.size())]

                    val t = (progress - startProgress) / (endProgress - startProgress)

                    color = evaluateColor(t, startColor, endColor)
                    break
                }
            }


            return color
        }
    }


    /**进度条背景*/
    var progressBgDrawable: Drawable? = null

    /**第二进度*/
    var progressSecondDrawable: Drawable? = null

    /**进度条*/
    var progressTrackDrawable: Drawable? = null

    /**进度条禁用时的绘制对象*/
    var progressTrackDisableDrawable: Drawable? = null

    //进度分母的最大值
    val progressValidMaxValue: Float
        get() = progressMaxValue - progressMinValue

    /**当前的进度
     * [progressMinValue]~[progressMaxValue]*/
    var progressValue: Float = 0f
        set(value) {
            val old = field
            field = validProgress(value)

            if (enableShowHideProgress) {
                //自动显示隐藏动画
                if (old <= 0 && field > 0) {
                    animate().translationY(0f).setDuration(Anim.ANIM_DURATION).start()
                } else if (field >= progressMaxValue) {
                    animate().translationY((-measuredHeight).toFloat())
                        .setDuration(Anim.ANIM_DURATION).start()
                }
            }
            postInvalidate()
        }

    /**
     * [0~1]的比例
     * [progressValue]
     * [_progressFraction]
     * [_progressSecondFraction]
     * */
    val progressFraction: Float
        get() = progressValueFraction(
            progressValue, progressMinValue, progressMaxValue
        )!!

    /**第二进度*/
    var progressSecondValue: Float = 0f
        set(value) {
            field = validProgress(value)
            postInvalidate()
        }

    /**最小进度*/
    var progressMinValue: Float = 0f
        set(value) {
            field = value
            if (progressValue == 0f) {
                progressValue = value
            }
            if (progressSecondValue == 0f) {
                progressSecondValue = value
            }
        }

    /**最大进度*/
    var progressMaxValue: Float = 100f

    //进度有效的值
    val progressValidValue: Float
        get() = progressValue - progressMinValue

    //进度有效的值
    val progressSecondValidValue: Float
        get() = progressSecondValue - progressMinValue

    /**是否激活流光进度效果*/
    var enableProgressFlowMode: Boolean = false

    /**圆角大小, 未强制指定[progressBgDrawable] [progressTrackDrawable]的情况下生效*/
    var progressRadius = 5 * dp

    /**Wrap_Content时的最小高度*/
    var progressMinHeight = 8 * dpi

    /**绘制进度Drawable时, 是否使用clip模式. 这样可以实现左右是圆角, 中间不是圆角的情况*/
    var progressClipMode = false

    //<editor-fold desc="最右边文本配置">

    /**是否显示进度提示文本, 在右边显示进度文本
     * [showProgressCenterText]
     *
     * [com.angcyo.widget.progress.DslProgressBar.drawProgressText]
     * */
    var showProgressText = false

    /**进度文本格式*/
    var progressTextFormat = "%s%%"

    /**文本大小*/
    var progressTextSize = 14.toDp()
    var progressTextMinWidth = 40.toDp()

    /**文本颜色*/
    var progressTextColor = _color(R.color.lib_text_color)

    /**文本距离进度偏移的距离*/
    var progressTextOffset = 10.toDpi()

    var progressTextFormatAction: (DslProgressBar) -> String = {
        progressTextFormat.format("${(_progressFraction * 100).toInt()}")
    }

    var progressCenterTextFormatAction: (DslProgressBar) -> String = {
        progressCenterTextFormat.format("${(_progressFraction * 100).toInt()}")
    }

    /**
     * 右边进度文本的绘制区域
     * [showProgressText]
     * [_progressBound]*/
    open val _progressTextBound = Rect()
        get() {
            val pBound = _progressBound
            field.set(
                pBound.right + progressTextOffset,
                pBound.top,
                measuredWidth - paddingRight,
                pBound.bottom
            )
            return field
        }

    //</editor-fold desc="最右边文本配置">

    //<editor-fold desc="居中文本配置">

    /**
     * 是否显示居中的进度文件
     * [showProgressText]
     * [com.angcyo.widget.progress.DslProgressBar.drawCenterProgressText]
     * */
    var showProgressCenterText = false
    var progressCenterTextFormat = "%s%%"
    var progressCenterTextSize = 14.toDp()
    var progressCenterTextColor = _color(R.color.lib_text_color)

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

        //使用progress_bg_drawable属性, 则圆角属性无效
        progressBgDrawable = typedArray.getDrawable(R.styleable.DslProgressBar_progress_bg_drawable)
        progressSecondDrawable =
            typedArray.getDrawable(R.styleable.DslProgressBar_progress_second_drawable)
        //进度条轨道
        progressTrackDrawable =
            typedArray.getDrawable(R.styleable.DslProgressBar_progress_track_drawable)
        progressTrackDisableDrawable =
            typedArray.getDrawable(R.styleable.DslProgressBar_progress_track_disable_drawable)

        progressRadius = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_radius, progressRadius.toInt()
        ).toFloat()
        progressTextOffset = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_text_offset, progressTextOffset
        )

        //检测是否需要渐变, 渐变至少需要2个色值
        if ((progressBgDrawable is ColorDrawable || progressBgDrawable == null) && typedArray.hasValue(
                R.styleable.DslProgressBar_progress_bg_gradient_colors
            )
        ) {
            var colors =
                typedArray.getString(R.styleable.DslProgressBar_progress_bg_gradient_colors)

            if (progressBgDrawable is ColorDrawable) {
                val startColor = (progressBgDrawable as ColorDrawable).color
                colors = "$startColor,$colors"
            } else if ((colors?.split(",")?.size ?: 0) <= 1) {
                colors = "$colors,$colors"
            }

            setBgGradientColors(colors)
        } else if (progressBgDrawable == null) {
            if (isInEditMode) {
                setBgGradientColors("${getColor(R.color.lib_progress_bg_color)},${getColor(R.color.lib_progress_bg_color)}")
            }
        }

        //第二进度Drawable
        if ((progressSecondDrawable is ColorDrawable || progressSecondDrawable == null) && typedArray.hasValue(
                R.styleable.DslProgressBar_progress_second_gradient_colors
            )
        ) {
            var colors =
                typedArray.getString(R.styleable.DslProgressBar_progress_second_gradient_colors)

            if (progressSecondDrawable is ColorDrawable) {
                val startColor = (progressSecondDrawable as ColorDrawable).color
                colors = "$startColor,$colors"
            } else if ((colors?.split(",")?.size ?: 0) <= 1) {
                colors = "$colors,$colors"
            }

            setSecondGradientColors(colors)
        } else if (progressSecondDrawable == null) {
            setSecondGradientColors("${getColor(R.color.lib_progress_second_bg_color)},${getColor(R.color.lib_progress_second_bg_color)}")
        }

        //进度条轨道Drawable
        if ((progressTrackDrawable is ColorDrawable || progressTrackDrawable == null) && typedArray.hasValue(
                R.styleable.DslProgressBar_progress_track_gradient_colors
            )
        ) {
            var colors =
                typedArray.getString(R.styleable.DslProgressBar_progress_track_gradient_colors)

            if (progressTrackDrawable is ColorDrawable) {
                val startColor = (progressTrackDrawable as ColorDrawable).color
                colors = "$startColor,$colors"
            } else if ((colors?.split(",")?.size ?: 0) <= 1) {
                colors = "$colors,$colors"
            }

            setTrackGradientColors(colors)
        } else if (progressTrackDrawable == null) {
            setTrackGradientColors("${getColor(R.color.colorPrimary)},${getColor(R.color.colorPrimaryDark)}")
        }

        if (progressTrackDisableDrawable == null) {
            progressTrackDisableDrawable = ColorDrawable(_color(R.color.lib_disable_bg_color))
        }
        //----end

        //提示文本
        showProgressText =
            typedArray.getBoolean(R.styleable.DslProgressBar_progress_show_text, showProgressText)
        progressTextSize = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_text_size, progressTextSize.toInt()
        ).toFloat()
        progressTextMinWidth = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_text_min_width, progressTextMinWidth.toInt()
        ).toFloat()
        progressTextColor =
            typedArray.getColor(R.styleable.DslProgressBar_progress_text_color, progressTextColor)
        progressTextFormat = typedArray.getString(R.styleable.DslProgressBar_progress_text_format)
            ?: progressTextFormat
        //---end

        progressMaxValue =
            typedArray.getFloat(R.styleable.DslProgressBar_progress_max_value, progressMaxValue)
        progressMinValue =
            typedArray.getFloat(R.styleable.DslProgressBar_progress_min_value, progressMinValue)

        progressValue = clamp(
            typedArray.getFloat(
                R.styleable.DslProgressBar_progress_value, if (isInEditMode) 50f else progressValue
            ), progressMinValue, progressMaxValue
        )
        progressSecondValue = clamp(
            typedArray.getFloat(
                R.styleable.DslProgressBar_progress_second_value,
                if (isInEditMode) 70f else progressSecondValue
            ), progressMinValue, progressMaxValue
        )

        enableShowHideProgress = typedArray.getBoolean(
            R.styleable.DslProgressBar_progress_enable_show_hide_progress, enableShowHideProgress
        )

        progressClipMode =
            typedArray.getBoolean(R.styleable.DslProgressBar_progress_clip_mode, progressClipMode)
        enableProgressFlowMode = typedArray.getBoolean(
            R.styleable.DslProgressBar_enable_progress_flow_mode, enableProgressFlowMode
        )
        if (enableProgressFlowMode && isInEditMode) {
            _progressFlowValue = 50
        }

        //居中提示文本
        showProgressCenterText = typedArray.getBoolean(
            R.styleable.DslProgressBar_progress_show_center_text, showProgressCenterText
        )
        progressCenterTextSize = typedArray.getDimensionPixelOffset(
            R.styleable.DslProgressBar_progress_center_text_size, progressCenterTextSize.toInt()
        ).toFloat()
        progressCenterTextColor = typedArray.getColor(
            R.styleable.DslProgressBar_progress_center_text_color, progressCenterTextColor
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
                widthMeasureSpec, atMost(progressMinHeight + paddingTop + paddingBottom)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (enableShowHideProgress) {
            if (progressValue <= progressMinValue) {
                translationY = (-measuredHeight).toFloat()
            }
        }
    }

    /**进度圆角的clip路径*/
    val progressClipPath: Path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (progressRadius > 0) {
            progressClipPath.rewind()
            progressClipPath.addRoundRect(
                _progressBound.left.toFloat(),
                _progressBound.top.toFloat(),
                _progressBound.right.toFloat(),
                _progressBound.bottom.toFloat(),
                progressRadius,
                progressRadius,
                Path.Direction.CW
            )
            canvas.withClip(progressClipPath) {
                drawBg(canvas)
                drawSecondProgress(canvas)
                drawTrack(canvas) //进度
            }
        } else {
            drawBg(canvas)
            drawSecondProgress(canvas)
            drawTrack(canvas) //进度
        }
        drawProgressText(canvas) //右边的进度文本
        drawCenterProgressText(canvas) //居中的进度文本
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    //如果文本的宽度不固定, 进度会出现抖动的情况
    val _textWidth: Float
        get() = max(_progressTextDrawable.textWidth, progressTextMinWidth)

    val _textHeight: Float
        get() = _progressTextDrawable.textHeight

    /**进度条整个绘制的区域*/
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
        get() = if (progressValidMaxValue == 0f) 0f else progressValidValue / progressValidMaxValue

    val _progressSecondFraction: Float
        get() = if (progressValidMaxValue == 0f) 0f else progressSecondValidValue / progressValidMaxValue

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

        if (isEnabled) {
            progressTrackDrawable
        } else {
            progressTrackDisableDrawable ?: progressTrackDrawable
        }?.apply {

            var progressRight = (pBound.left + pBound.width() * _progressFraction).toInt()

            val right = if (progressClipMode) {
                pBound.right
            } else {
                progressRight
            }

            setBounds(
                pBound.left, pBound.top, right, pBound.bottom
            )

            if (progressClipMode) {
                canvas.withSave {
                    clipRect(
                        pBound.left, pBound.top, progressRight, pBound.bottom
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
                    pBound.left, pBound.top, (right * progressFlowRatio).toInt(), pBound.bottom
                )
                progressRight = (progressRight * progressFlowRatio).toInt()
                if (progressClipMode) {
                    canvas.withSave {
                        clipRect(
                            pBound.left, pBound.top, progressRight, pBound.bottom
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
                bounds = _progressTextBound
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
                        pBound.left, pBound.top, progressRight, pBound.bottom
                    )
                    draw(canvas)
                }
                //绘制右半边进度文本
                canvas.withSave {
                    textColor = progressCenterTextColor
                    clipRect(
                        progressRight, pBound.top, pBound.right, pBound.bottom
                    )
                    draw(canvas)
                }
            }
        }
    }

    /**设置背景渐变的颜色
     * [color] 支持多个颜色的int值和hex值*/
    fun setBgGradientColors(color: String?): IntArray? {
        val drawable = DslGradientDrawable().apply {
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
        return drawable.gradientColors
    }

    /**设置进度条渐变的颜色*/
    fun setTrackGradientColors(color: String?): IntArray? {
        val drawable = DslGradientDrawable().apply {
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
        return drawable.gradientColors
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

    /**限制设置的非法进度值, [progress]非[0~1]*/
    fun validProgress(progress: Float): Float {
        return MathUtils.clamp(progress, progressMinValue, progressMaxValue)
    }

    open fun setProgress(
        progress: Int,
        fromProgress: Float = progressValidValue,
        animDuration: Long = Anim.ANIM_DURATION
    ) {
        setProgress(progress.toFloat(), fromProgress, animDuration)
    }

    /**
     * 设置进度
     * @param progress [0-100]]
     * @param fromProgress 动画开始的进度, 默认是当前进度
     * @param animDuration 动画时长, 小于等于0, 不开启动画
     * */
    open fun setProgress(
        progress: Float,
        fromProgress: Float = progressValue,
        animDuration: Long = Anim.ANIM_DURATION
    ) {
        _animtor?.cancel()
        _animtor = null
        val p = validProgress(progress)
        if (animDuration > 0 && fromProgress != p) {
            _animtor = anim(fromProgress, p) {
                onAnimatorConfig = {
                    it.duration = animDuration
                }
                onAnimatorUpdateValue = { value, _ ->
                    this@DslProgressBar.progressValue = value as Float
                }
            }
        } else {
            this.progressValue = p
        }
    }

    /**获取一个值[value], 在指定范围[minValue]~[maxValue]中的比例
     * 返回的比例是[0~100f]的值
     *
     * @return 返回进度比例*/
    fun getProgress(
        value: Float = progressValue,
        minValue: Float = progressMinValue,
        maxValue: Float = progressMaxValue
    ): Float {
        return (value - minValue) / (maxValue - minValue) * 100
    }

    /**获取一个进度[progress], 在指定范围[minValue]~[maxValue]中的值
     * [progress] 是[0~100f]的进度
     *
     * @return 返回的值是[minValue]~[maxValue]的值
     * */
    fun getValue(
        progress: Float = _progressFraction * 100,
        minValue: Float = progressMinValue,
        maxValue: Float = progressMaxValue
    ): Float {
        val fraction = progress / 100
        return minValue + (maxValue - minValue) * fraction
    }
}