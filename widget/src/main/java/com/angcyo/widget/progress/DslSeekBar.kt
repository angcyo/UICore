package com.angcyo.widget.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import com.angcyo.drawable.base.DslGradientDrawable
import com.angcyo.drawable.text.DslTextDrawable
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.getColor
import com.angcyo.library.ex.setBounds
import com.angcyo.widget.R
import com.angcyo.widget.base.*
import kotlin.math.abs
import kotlin.math.floor
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

    //浮子文本绘制
    val _thumbTextDrawable: DslTextDrawable = DslTextDrawable()
        get() {
            with(field) {
                textBgDrawable = thumbTextBgDrawable
                textSize = progressTextSize
                textColor = progressTextColor
                textOffsetX = seekThumbTextOffsetX
                textOffsetY = seekThumbTextOffsetY
                text = progressTextFormatAction()
            }
            return field
        }

    override val _progressBound: Rect
        get() = super._progressBound.apply {

            val drawHeight = measuredHeight - paddingTop - paddingBottom
            val pTop = paddingTop + (drawHeight - progressHeight) / 2
            top = pTop
            bottom = top + progressHeight
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
        seekThumbTouchHaloDrawable =
            typedArray.getDrawable(R.styleable.DslSeekBar_seek_thumb_touch_halo_drawable)
        thumbTextBgDrawable =
            typedArray.getDrawable(R.styleable.DslSeekBar_seek_thumb_text_bg_drawable)
        showThumbText =
            typedArray.getBoolean(R.styleable.DslSeekBar_seek_show_thumb_text, showThumbText)

        val thumbSolidColor = typedArray.getColor(
            R.styleable.DslSeekBar_seek_thumb_solid_color,
            getColor(R.color.colorAccent)
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

            val radius = typedArray.getDimensionPixelOffset(
                R.styleable.DslSeekBar_seek_thumb_radius,
                45 * dpi
            )

            _dslGradientDrawable.fillRadii(radius)

            _dslGradientDrawable.updateOriginDrawable()
            seekThumbDrawable = _dslGradientDrawable
        }

        //未设置[seekThumbTouchDrawable]时, 使用默认样式
        val touchThumbSize: Int = (progressHeight + seekThumbOverHeight) * 3
        if (seekThumbTouchHaloDrawable == null && typedArray.hasValue(R.styleable.DslSeekBar_seek_thumb_touch_halo_gradient_colors)) {
            var colors =
                typedArray.getString(R.styleable.DslSeekBar_seek_thumb_touch_halo_gradient_colors)

            if (colors?.split(",")?.size ?: 0 <= 1) {
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
            seekThumbTouchHaloDrawable = DslGradientDrawable().run {
                setBounds(touchThumbSize, touchThumbSize)
                gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientRadius = touchThumbSize / 2f
                gradientColors = intArrayOf(thumbSolidColor.alpha(188), thumbSolidColor.alpha(0))
                updateOriginDrawable()
            }
        }

        typedArray.recycle()

        val w = progressHeight / 2 + seekThumbOverHeight
        val h = touchThumbSize / 2 + progressHeight / 2

        val paddingLeft = if (paddingLeft <= 0) w else paddingLeft
        val paddingRight = if (paddingRight <= 0) w else paddingRight
        val paddingTop = if (paddingTop <= 0) h else paddingTop
        val paddingBottom = if (paddingBottom <= 0) h - progressHeight / 2 else paddingBottom

        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (heightMeasureSpec.getMode() != MeasureSpec.EXACTLY) {
            super.onMeasure(
                widthMeasureSpec,
                atMost(progressHeight + seekThumbOverHeight * 2 + paddingTop + paddingBottom)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawThumb(canvas)
        drawThumbText(canvas)
    }

    //绘制浮子
    open fun drawThumb(canvas: Canvas) {
        if (_drawTouchThumbDrawable || isInEditMode) {
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

        seekThumbDrawable?.apply {
            bounds = _thumbBound
            draw(canvas)
        }
    }

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

    //<editor-fold desc="Touch事件">

    //手势检测
    val _gestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                _onTouchMoveTo(e.x, e.y)
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
                    _onTouchMoveTo(e2.x, e2.y)
                    handle = true
                }
                return handle
            }
        })
    }

    //是否在浮子处按下
    var _isTouchDownInThumb = false
    var _drawTouchThumbDrawable: Boolean by InvalidateProperty(false)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        if (event.isTouchDown()) {
            _isTouchDownInThumb = _thumbBound.contains(event.x.toInt(), event.y.toInt())
            _drawTouchThumbDrawable = true
            isTouchDown = true
            if (_isTouchDownInThumb) {
                seekThumbDrawable?.state = intArrayOf(android.R.attr.state_pressed)
            }
        } else if (event.isTouchFinish()) {
            _isTouchDownInThumb = false
            isTouchDown = false
            _drawTouchThumbDrawable = false
            seekThumbDrawable?.state = intArrayOf()
            parent.requestDisallowInterceptTouchEvent(false)

            onSeekBarConfig?.apply { onSeekTouchEnd(progressValue, _progressFraction) }
        }

        _gestureDetector.onTouchEvent(event)

        return true
    }

    /**手指移动*/
    open fun _onTouchMoveTo(x: Float, y: Float) {
        val progress: Int =
            floor(((x - paddingLeft) / _progressBound.width() * progressMaxValue).toDouble()).toInt()

        progressValue = validProgress(progress)

        onSeekBarConfig?.apply { onSeekChanged(progressValue, _progressFraction, true) }
    }


    override fun setProgress(progress: Int, fromProgress: Int, animDuration: Long) {
        super.setProgress(progress, fromProgress, animDuration)
        onSeekBarConfig?.apply { onSeekChanged(validProgress(progress), _progressFraction, false) }
    }

    fun config(action: SeekBarConfig.() -> Unit) {
        if (onSeekBarConfig == null) {
            onSeekBarConfig = SeekBarConfig()
        }
        onSeekBarConfig?.action()
    }

    //</editor-fold desc="Touch事件">
}

open class SeekBarConfig {
    /**进度改变回调,
     * [value] 进度值
     * [fraction] 进度比例
     * [fromUser] 是否是用户触发*/
    var onSeekChanged: (value: Int, fraction: Float, fromUser: Boolean) -> Unit = { _, _, _ -> }

    /**Touch结束后的回调*/
    var onSeekTouchEnd: (value: Int, fraction: Float) -> Unit = { _, _ -> }
}