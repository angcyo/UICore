package com.angcyo.widget.progress

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import com.angcyo.drawable.loading.ArcLoadingDrawable
import com.angcyo.widget.R
import com.angcyo.widget.base.*
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class ArcLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    var arcLoadingDrawable = ArcLoadingDrawable()

    var duration: Long = 2000

    var autoStart = true

    var progress: Int
        set(value) {
            arcLoadingDrawable.progress = value
        }
        get() = arcLoadingDrawable.progress

    init {

        val array: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.ArcLoadingView)

        if (isInEditMode) {
            arcLoadingDrawable.progress = 51
        }

        arcLoadingDrawable.arcColor = getColor(R.color.colorAccent)

        arcLoadingDrawable.arcColor =
            array.getColor(R.styleable.ArcLoadingView_arc_color, arcLoadingDrawable.arcColor)

        arcLoadingDrawable.strokeWidth = array.getDimensionPixelOffset(
            R.styleable.ArcLoadingView_arc_width,
            arcLoadingDrawable.strokeWidth.toInt()
        ).toFloat()

        duration =
            array.getInt(R.styleable.ArcLoadingView_arc_duration, duration.toInt()).toLong()

        arcLoadingDrawable.progress =
            array.getInt(R.styleable.ArcLoadingView_arc_progress, arcLoadingDrawable.progress)

        autoStart = array.getBoolean(R.styleable.ArcLoadingView_arc_auto_start, autoStart)

        array.recycle()

        arcLoadingDrawable.callback = this
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who == arcLoadingDrawable
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        arcLoadingDrawable.apply {
            val size = min(drawWidth, drawHeight)
            setBounds(
                drawCenterX - size / 2,
                drawCenterY - size / 2,
                drawCenterX + size / 2,
                drawCenterY + size / 2
            )
            draw(canvas)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        checkStart()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        endLoading()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        checkStart()
    }

    fun checkStart() {
        if (autoStart) {
            if (ViewCompat.isAttachedToWindow(this) && isVisible()) {
                startLoading()
            } else {
                endLoading()
            }
        }
    }

    var _animator: ValueAnimator? = null
    fun startLoading() {
        if (_animator?.isStarted == true) {
            return
        }

        endLoading()
        _animator = anim(0f, 1f) {
            onAnimatorConfig = {
                it.duration = duration
                it.repeatCount = ValueAnimator.INFINITE
                it.repeatMode = ValueAnimator.RESTART
            }
            onAnimatorUpdateValue = { _, fraction ->
                progress = (fraction * 100).toInt()
            }
        }
    }

    fun endLoading() {
        _animator?.cancel()
    }
}