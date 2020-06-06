package com.angcyo.widget.progress

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
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

        arcLoadingDrawable.arcColor = getColor(R.color.colorAccent)

        //色调（H） 0°～360° 从红色开始按逆时针方向计算，红色为0°，绿色为120°,蓝色为240°。它们的补色是：黄色为60°，青色为180°,紫色为300°
        //饱和度（S） 0%～100%，值越大，颜色越饱和
        //明度（V） 0%（黑）到100%（白）。
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(arcLoadingDrawable.arcColor, hsv)

        //调高饱和度
        hsv[1] = 1f
        //调高亮度
        hsv[2] = 1f
        arcLoadingDrawable.arcColor = Color.HSVToColor(hsv)

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

        if (isInEditMode) {
            progress = 51
        }
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
        if (isInEditMode) {
            return
        }
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