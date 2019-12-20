package com.angcyo.widget.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.angcyo.drawable.base.DslGradientDrawable
import com.angcyo.widget.R

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
    var maxProgressValue: Int = 100

    /**当前的进度*/
    var progressValue: Int = 0

    /**圆角大小, 未强制指定[progressBgDrawable] [progressTrackDrawable]的情况下生效*/
    var progressRadius: Int = 0

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DslProgressBar)

        progressBgDrawable = typedArray.getDrawable(R.styleable.DslProgressBar_progress_bg_drawable)
        progressTrackDrawable =
            typedArray.getDrawable(R.styleable.DslProgressBar_progress_track_drawable)

        progressRadius = typedArray.getDimensionPixelOffset(R.styleable.DslProgressBar_progress_radius, progressRadius)

        //检测是否需要渐变, 渐变至少需要2个色值
        if (progressBgDrawable is ColorDrawable ||
            progressBgDrawable == null &&
            typedArray.hasValue(R.styleable.DslProgressBar_progress_bg_gradient_colors)
        ) {
            var colors =
                typedArray.getString(R.styleable.DslProgressBar_progress_bg_gradient_colors)

            if (progressBgDrawable is ColorDrawable) {
                val startColor = (progressBgDrawable as ColorDrawable).color
                colors = "$startColor,$colors"
            } else if (colors?.split(",")?.size ?: 0 <= 1) {
                colors = "0,$colors"
            }

            setBgGradientColors(colors)
        }

        if (progressTrackDrawable is ColorDrawable ||
            progressTrackDrawable == null &&
            typedArray.hasValue(R.styleable.DslProgressBar_progress_track_drawable_gradient_colors)
        ) {
            var colors =
                typedArray.getString(R.styleable.DslProgressBar_progress_track_drawable_gradient_colors)

            if (progressTrackDrawable is ColorDrawable) {
                val startColor = (progressTrackDrawable as ColorDrawable).color
                colors = "$startColor,$colors"
            } else if (colors?.split(",")?.size ?: 0 <= 1) {
                colors = "0,$colors"
            }

            setTrackGradientColors(colors)
        }
        //----end

        progressValue = typedArray.getInt(R.styleable.DslProgressBar_progress_value, progressValue)
        maxProgressValue =
            typedArray.getInt(R.styleable.DslProgressBar_progress_max_value, maxProgressValue)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBg(canvas)
        drawTrack(canvas)
    }

    //绘制背景
    open fun drawBg(canvas: Canvas) {
        progressBgDrawable?.apply {
            setBounds(
                paddingLeft,
                paddingTop,
                measuredWidth - paddingRight,
                measuredHeight - paddingBottom
            )
            draw(canvas)
        }
    }

    //绘制进度轨道
    open fun drawTrack(canvas: Canvas) {
        canvas.save()
        canvas.scale(progressValue * 1f / maxProgressValue, 1f, 0f, 0f)
        progressTrackDrawable?.apply {
            setBounds(
                paddingLeft,
                paddingTop,
                measuredWidth - paddingRight,
                measuredHeight - paddingBottom
            )
            draw(canvas)
        }
        canvas.restore()
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
}