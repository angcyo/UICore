package com.angcyo.widget.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import com.angcyo.drawable.base.DslGradientDrawable
import com.angcyo.drawable.dpi
import com.angcyo.drawable.text.DslTextDrawable
import com.angcyo.widget.R
import com.angcyo.widget.base.getColor
import com.angcyo.widget.base.isTouchDown
import com.angcyo.widget.base.isTouchFinish
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

    /**进度条的高度*/
    var progressHeight = 15 * dpi

    /**浮子超过进度条的高度*/
    var seekThumbOverHeight = 6 * dpi

    /**回调监听*/
    var onSeekBarConfig: SeekBarConfig? = null

    /**显示提示文本*/
    var showThumbText = false
    var seekThumbTextOffsetX = 0
    var seekThumbTextOffsetY = 0

    var thumbTextBgDrawable: Drawable? = null

    //如果未强制指定[seekThumbDrawable], 则用属性构建一个
    val _dslGradientDrawable = DslGradientDrawable()

    //浮子文本绘制
    val _thumbTextDrawable: DslTextDrawable = DslTextDrawable()
        get() {
            with(field) {
                textBgDrawable = thumbTextBgDrawable
                textSize = progressTextSize
                textColor = progressTextColor
                textOffsetX = seekThumbTextOffsetX
                textOffsetY = seekThumbTextOffsetY
                text = progressTextFormat.format("${(_progressFraction * 100).toInt()}")
            }
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
        thumbTextBgDrawable =
            typedArray.getDrawable(R.styleable.DslSeekBar_seek_thumb_text_bg_drawable)
        showThumbText =
            typedArray.getBoolean(R.styleable.DslSeekBar_seek_show_thumb_text, showThumbText)

        if (seekThumbDrawable == null) {

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

            _dslGradientDrawable.gradientSolidColor =
                typedArray.getColor(
                    R.styleable.DslSeekBar_seek_thumb_solid_color,
                    getColor(R.color.colorAccent)
                )

            _dslGradientDrawable.fillRadii(45 * dpi)

            _dslGradientDrawable.updateOriginDrawable()
            seekThumbDrawable = _dslGradientDrawable
        }

        typedArray.recycle()

        if (paddingBottom <= 0) {
            setPadding(paddingLeft, paddingTop, paddingRight, seekThumbOverHeight)
        }
    }

    override val _progressBound: Rect
        get() = super._progressBound.apply {
            top = bottom - progressHeight
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawThumb(canvas)
        drawThumbText(canvas)
    }

    //绘制浮子
    open fun drawThumb(canvas: Canvas) {
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
    val _gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        if (event.isTouchDown()) {
            _isTouchDownInThumb = _thumbBound.contains(event.x.toInt(), event.y.toInt())
        } else if (event.isTouchFinish()) {
            _isTouchDownInThumb = false
            parent.requestDisallowInterceptTouchEvent(false)
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
    /**进度改变回调*/
    var onSeekChanged: (value: Int, fraction: Float, fromUser: Boolean) -> Unit = { _, _, _ -> }
}