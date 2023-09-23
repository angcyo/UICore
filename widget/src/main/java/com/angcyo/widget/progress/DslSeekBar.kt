package com.angcyo.widget.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import com.angcyo.drawable.base.DslGradientDrawable
import com.angcyo.drawable.text.DslTextDrawable
import com.angcyo.library.component.RegionTouchDetector
import com.angcyo.library.ex.*
import com.angcyo.widget.R
import com.angcyo.widget.base.*
import kotlin.math.min

/**
 * 单浮子 seek bar
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslSeekBar(context: Context, attributeSet: AttributeSet? = null) :
    DslProgressBar(context, attributeSet) {

    /**浮子, 不受padding属性的影响, 根据[progress]的Y轴中点定位*/
    var seekThumbDrawable: Drawable? = null

    /**[seekThumbDrawable]禁用时绘制*/
    var seekThumbDisableDrawable: Drawable? = null

    /**是否激活光晕效果*/
    var enableHalo: Boolean = true

    /**是否激活光晕效果下的进度矩形插入*/
    var enableHaloInset: Boolean = true

    /**按下状态时, 光晕效果, [seekThumbDrawable] 后面额外绘制的[Drawable] 用于提示 [TouchDown] 状态, bounds 需要自带宽高属性*/
    var seekThumbTouchHaloDrawable: Drawable? = null

    /**进度条的高度*/
    var progressHeight = 8 * dpi

    /**浮子超过进度条的高度*/
    var seekThumbOverHeight = 8 * dpi

    /**回调监听*/
    var onSeekBarConfig: SeekBarConfig? = null

    /**显示提示文本*/
    var showThumbText = false
    var seekThumbTextOffsetX = 0
    var seekThumbTextOffsetY = 0

    var thumbTextBgDrawable: Drawable? = null

    //如果未强制指定[seekThumbDrawable], 则用属性构建一个
    val _dslGradientDrawable = DslGradientDrawable()

    /**是否Touch按下*/
    var isTouchDown = false

    /**是否在中点进度上绘制提示圆圈*/
    var showCenterProgressTip: Boolean = false

    val centerProgressTipPaint: Paint = createPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    //浮子文本绘制
    val _thumbTextDrawable: DslTextDrawable = DslTextDrawable()
        get() {
            with(field) {
                textBgDrawable = thumbTextBgDrawable
                textSize = progressTextSize
                textColor = progressTextColor
                textOffsetX = seekThumbTextOffsetX
                textOffsetY = seekThumbTextOffsetY
                text = progressTextFormatAction(this@DslSeekBar)
            }
            return field
        }

    override val _progressBound: Rect
        get() = super._progressBound.apply {
            val drawHeight = measuredHeight - paddingTop - paddingBottom
            val pTop = paddingTop + (drawHeight - progressHeight) / 2
            top = pTop
            bottom = top + progressHeight

            if (showThumbText) {
                offset(0, _thumbTextDrawable.textHeight.toInt() / 2 + seekThumbTextOffsetY)
            }
            if (enableHalo && enableHaloInset) {
                inset(haloThumbSize / 2, 0)
            }
        }

    //浮子绘制范围
    val _thumbBound: Rect = Rect()
        get() {
            val pBound = _progressBound
            val centerX = pBound.left + pBound.width() * _progressFraction
            val centerY = pBound.top + pBound.height() / 2

            val thumbWidth: Int
            val thumbHeight: Int
            if (seekThumbDrawable == _dslGradientDrawable) {
                thumbWidth = min(pBound.width(), pBound.height()) + seekThumbOverHeight * 2
                thumbHeight = thumbWidth
            } else {
                //自定义的seekThumbDrawable, 需要手动指定 width height
                thumbWidth = seekThumbDrawable?.minimumWidth ?: 0
                thumbHeight = seekThumbDrawable?.minimumHeight ?: 0
            }

            field.set(
                (centerX - thumbWidth / 2).toInt(),
                centerY - thumbHeight / 2,
                (centerX + thumbWidth / 2).toInt(),
                centerY + thumbHeight / 2
            )

            return field
        }

    //是否在浮子处按下
    var _isTouchDownInThumb = false
    var _drawTouchThumbDrawable: Boolean by InvalidateProperty(false)

    //光晕的大小, 宽高
    val haloThumbSize: Int
        get() = (progressHeight + seekThumbOverHeight) * 3

    init {
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.DslSeekBar)

        progressHeight = typedArray.getDimensionPixelOffset(
            R.styleable.DslSeekBar_progress_height,
            progressHeight
        )
        seekThumbOverHeight = typedArray.getDimensionPixelOffset(
            R.styleable.DslSeekBar_seek_thumb_over_height,
            seekThumbOverHeight
        )
        seekThumbTextOffsetX = typedArray.getDimensionPixelOffset(
            R.styleable.DslSeekBar_seek_thumb_text_offset_x,
            seekThumbTextOffsetX
        )
        seekThumbTextOffsetY = typedArray.getDimensionPixelOffset(
            R.styleable.DslSeekBar_seek_thumb_text_offset_y,
            seekThumbTextOffsetY
        )

        seekThumbDrawable = typedArray.getDrawable(R.styleable.DslSeekBar_seek_thumb_drawable)
        seekThumbDisableDrawable =
            typedArray.getDrawable(R.styleable.DslSeekBar_seek_thumb_disable_drawable)
        seekThumbTouchHaloDrawable =
            typedArray.getDrawable(R.styleable.DslSeekBar_seek_thumb_touch_halo_drawable)
        thumbTextBgDrawable =
            typedArray.getDrawable(R.styleable.DslSeekBar_seek_thumb_text_bg_drawable)
        showThumbText =
            typedArray.getBoolean(R.styleable.DslSeekBar_seek_show_thumb_text, showThumbText)
        enableHalo =
            typedArray.getBoolean(R.styleable.DslSeekBar_seek_enable_halo, enableHalo)
        enableHaloInset =
            typedArray.getBoolean(R.styleable.DslSeekBar_seek_enable_halo_inset, enableHaloInset)

        val thumbSolidColor = typedArray.getColor(
            R.styleable.DslSeekBar_seek_thumb_solid_color,
            getColor(R.color.colorAccent)
        )

        val radius = typedArray.getDimensionPixelOffset(
            R.styleable.DslSeekBar_seek_thumb_radius,
            45 * dpi
        )

        //未设置[seekThumbDrawable]时, 使用默认样式
        if (seekThumbDrawable == null && !typedArray.hasValue(R.styleable.DslSeekBar_seek_thumb_drawable)) {

            _dslGradientDrawable.gradientStrokeWidth =
                typedArray.getDimensionPixelOffset(
                    R.styleable.DslSeekBar_seek_thumb_stroke_width,
                    3 * dpi
                )

            _dslGradientDrawable.gradientStrokeColor =
                typedArray.getColor(
                    R.styleable.DslSeekBar_seek_thumb_stroke_color,
                    Color.WHITE
                )

            _dslGradientDrawable.gradientSolidColor = thumbSolidColor

            _dslGradientDrawable.fillRadii(radius)

            _dslGradientDrawable.updateOriginDrawable()
            seekThumbDrawable = _dslGradientDrawable
        }

        if (seekThumbDisableDrawable == null) {
            seekThumbDisableDrawable = DslGradientDrawable().apply {
                gradientSolidColor = _color(R.color.lib_disable_bg_color)
                fillRadii(radius)
                updateOriginDrawable()
            }
        }

        //未设置[seekThumbTouchDrawable]时, 使用默认样式
        val touchThumbSize: Int = haloThumbSize
        if (seekThumbTouchHaloDrawable == null && typedArray.hasValue(R.styleable.DslSeekBar_seek_thumb_touch_halo_gradient_colors)) {
            var colors =
                typedArray.getString(R.styleable.DslSeekBar_seek_thumb_touch_halo_gradient_colors)

            if ((colors?.split(",")?.size ?: 0) <= 1) {
                colors = "$colors,$colors"
            }

            seekThumbTouchHaloDrawable = DslGradientDrawable().run {
                setBounds(touchThumbSize, touchThumbSize)
                gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientRadius = touchThumbSize / 2f
                gradientColors = _fillColor(colors)
                updateOriginDrawable()
            }
        } else if (seekThumbTouchHaloDrawable == null) {
            updateThumbHaloDrawable(thumbSolidColor)
        }

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (heightMeasureSpec.getMode() != MeasureSpec.EXACTLY) {
            var wrapHeight = if (enableHalo) {
                haloThumbSize
            } else {
                progressHeight + seekThumbOverHeight * 2
            }
            wrapHeight += paddingTop + paddingBottom
            if (showThumbText) {
                wrapHeight += _thumbTextDrawable.textHeight.toInt() + seekThumbTextOffsetY
            }
            setMeasuredDimension(
                getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
                getDefaultSize(suggestedMinimumHeight, atMost(wrapHeight))
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCenterProgressTip(canvas)
        drawThumb(canvas)
        drawThumbText(canvas)
    }

    /**中点提示*/
    open fun drawCenterProgressTip(canvas: Canvas) {
        if (showCenterProgressTip) {
            val bounds = _progressBound
            val cx = bounds.centerX()
            val cy = bounds.centerY()
            val size = min(bounds.width(), bounds.height()) / 2

            //外圈
            if (_progressFraction >= 0.5) {
                //超过1半的进度
                seekThumbDrawable?.let {
                    if (it is DslGradientDrawable) {
                        centerProgressTipPaint.color = it.gradientSolidColor
                    }
                }
            } else {
                centerProgressTipPaint.color = Color.GRAY
            }
            canvas.drawCircle(
                cx.toFloat(),
                cy.toFloat(),
                size + 2 * dp,
                centerProgressTipPaint
            )
            //内圈
            centerProgressTipPaint.color = Color.WHITE
            canvas.drawCircle(
                cx.toFloat(),
                cy.toFloat(),
                size.toFloat(),
                centerProgressTipPaint
            )
        }
    }

    /**绘制浮子*/
    open fun drawThumb(canvas: Canvas) {
        if (enableHalo && (_drawTouchThumbDrawable || isInEditMode)) {
            //光晕绘制
            seekThumbTouchHaloDrawable?.apply {
                canvas.save()
                canvas.translate(
                    (_thumbBound.centerX() - bounds.width() / 2).toFloat(),
                    (_thumbBound.centerY() - bounds.height() / 2).toFloat()
                )
                draw(canvas)
                canvas.restore()
            }
        }

        //浮子
        if (isEnabled) {
            seekThumbDrawable
        } else {
            seekThumbDisableDrawable ?: seekThumbDrawable
        }?.apply {
            bounds = _thumbBound
            draw(canvas)
        }
    }

    /**浮子上的文本绘制*/
    open fun drawThumbText(canvas: Canvas) {
        if (showThumbText) {
            with(_thumbTextDrawable) {
                val tBound = _thumbBound
                setBounds(
                    tBound.centerX() - minimumWidth / 2,
                    tBound.top - progressTextOffset - minimumHeight,
                    tBound.centerX() + minimumWidth / 2,
                    tBound.top - progressTextOffset
                )
                draw(canvas)
            }
        }
    }

    /**更新浮子的颜色*/
    open fun updateThumbColor(thumbSolidColor: Int) {
        _dslGradientDrawable.gradientSolidColor = thumbSolidColor
        _dslGradientDrawable.updateOriginDrawable()
        seekThumbDrawable = _dslGradientDrawable
        updateThumbHaloDrawable(thumbSolidColor)
    }

    /**更新光晕的颜色*/
    open fun updateThumbHaloDrawable(thumbSolidColor: Int) {
        seekThumbTouchHaloDrawable = DslGradientDrawable().run {
            setBounds(haloThumbSize, haloThumbSize)
            gradientType = GradientDrawable.RADIAL_GRADIENT
            gradientRadius = haloThumbSize / 2f
            gradientColors = intArrayOf(thumbSolidColor.alpha(188), thumbSolidColor.alpha(0))
            updateOriginDrawable()
        }
    }

    //<editor-fold desc="Touch事件">

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnabled) {
            super.onTouchEvent(event)
            if (showProgressText && regionTouchDetector.onTouchEvent(this, event)) {
                //no op
                return true
            }
            if (event.isTouchDown()) {
                _isTouchDownInThumb = _thumbBound.contains(event.x.toInt(), event.y.toInt())
                _drawTouchThumbDrawable = true
                isTouchDown = true
                if (_isTouchDownInThumb) {
                    seekThumbDrawable?.state = intArrayOf(android.R.attr.state_pressed)
                }
            } else if (event.isTouchMove()) {
                parent.requestDisallowInterceptTouchEvent(true)
                _onTouchMoveTo(event.x, event.y, false)
            } else if (event.isTouchFinish()) {
                _isTouchDownInThumb = false
                isTouchDown = false
                _drawTouchThumbDrawable = false
                seekThumbDrawable?.state = intArrayOf()
                parent.requestDisallowInterceptTouchEvent(false)

                onSeekBarConfig?.apply { onSeekTouchEnd(progressValue, _progressFraction) }
            }
            seekTouchListener?.onTouch(this, event)
            return true
        } else {
            return super.onTouchEvent(event)
        }
    }

    /**进度文本区域点击探测*/
    protected val regionTouchDetector = RegionTouchDetector({
        _progressTextBound.run {
            top = 0
            bottom = measuredHeight
            contains(it.x.toInt(), it.y.toInt())
        }
    }) {
        progressValueTouchAction?.invoke(it)
    }

    var progressValueTouchAction: ((touchType: Int) -> Unit)? = null

    /**手势事件*/
    var seekTouchListener: OnTouchListener? = null
    override fun setOnTouchListener(l: OnTouchListener?) {
        //super.setOnTouchListener(l) //不能调用super的否则[onTouchEvent]可能不会触发
        seekTouchListener = l
    }

    /**手指移动*/
    open fun _onTouchMoveTo(x: Float, y: Float, isFinish: Boolean) {
        val fraction = (x - _progressBound.left) / _progressBound.width()
        val progress = progressMinValue + fraction * progressValidMaxValue

        progressValue = validProgress(progress)

        onSeekBarConfig?.apply { onSeekChanged(progressValue, _progressFraction, true) }
    }

    //</editor-fold desc="Touch事件">

    override fun setProgress(progress: Float, fromProgress: Float, animDuration: Long) {
        super.setProgress(progress, fromProgress, animDuration)
        val validProgress = validProgress(progress)
        onSeekBarConfig?.apply {
            onSeekChanged(validProgress, validProgress / progressValidMaxValue, false)
        }
    }

    fun config(action: SeekBarConfig.() -> Unit) {
        if (onSeekBarConfig == null) {
            onSeekBarConfig = SeekBarConfig()
        }
        onSeekBarConfig?.action()
    }
}

open class SeekBarConfig {

    /**进度改变回调,
     * [value] 进度值[0~100] [progressMinValue, progressMaxValue] [com.angcyo.widget.progress.DslProgressBar.progressValue]
     * [fraction] 进度比例[0~1f]
     * [fromUser] 是否是用户触发*/
    var onSeekChanged: (value: Float, fraction: Float, fromUser: Boolean) -> Unit = { _, _, _ -> }

    /**Touch结束后的回调
     * [value] 进度值[0~100] [progressMinValue, progressMaxValue] [com.angcyo.widget.progress.DslProgressBar.progressValue]
     * [fraction] 进度比例[0~1f]
     * */
    var onSeekTouchEnd: (value: Float, fraction: Float) -> Unit = { _, _ -> }
}