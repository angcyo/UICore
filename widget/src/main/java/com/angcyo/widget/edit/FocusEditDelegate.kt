package com.angcyo.widget.edit

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.widget.EditText
import com.angcyo.library.ex.calcSize
import com.angcyo.widget.R
import com.angcyo.widget.base.Anim
import com.angcyo.widget.base.mH
import com.angcyo.widget.base.mW

/**
 *
 * 无焦点时: [EditText] 底部绘制一根线
 * 获取到焦点时: 底部线动态向左右2端展开.
 * 丢失焦点时: 反向动画
 *
 * 注意: 使用时, 请关闭默认的Background
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class FocusEditDelegate(val editText: EditText) {

    /**无焦点时, 绘制的Drawable*/
    var noFocusDrawable: Drawable? = null
    var focusDrawable: Drawable? = null

    /**指定[Drawable]的高度*/
    var drawableHeight: String? = "1dp"

    var drawableMarginLeft: String? = null
    var drawableMarginRight: String? = null
    var drawableMarginBottom: String? = null

    /**动画持续时长*/
    var animatorDuration = 600

    var _progress: Float = 0f
    var _valueAnimator: ValueAnimator? = null

    open fun initAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FocusEditDelegate)
        noFocusDrawable = typedArray.getDrawable(R.styleable.FocusEditDelegate_r_no_focus_drawable)
        focusDrawable = typedArray.getDrawable(R.styleable.FocusEditDelegate_r_focus_drawable)

        drawableHeight =
            typedArray.getString(R.styleable.FocusEditDelegate_r_drawable_height) ?: drawableHeight
        drawableMarginLeft =
            typedArray.getString(R.styleable.FocusEditDelegate_r_drawable_margin_left)
        drawableMarginRight =
            typedArray.getString(R.styleable.FocusEditDelegate_r_drawable_margin_right)
        drawableMarginBottom =
            typedArray.getString(R.styleable.FocusEditDelegate_r_drawable_margin_bottom)

        animatorDuration = typedArray.getInt(
            R.styleable.FocusEditDelegate_r_focus_animator_duration,
            animatorDuration
        )

        typedArray.recycle()
    }

    /**焦点改变通知*/
    open fun onFocusChanged(focused: Boolean) {
        stopAnimator()

        if (focusDrawable == null) {
            return
        }

        _valueAnimator = if (focused) {
            ValueAnimator.ofFloat(0f, 1f)
        } else {
            ValueAnimator.ofFloat(1f, 0f)
        }
        _valueAnimator?.apply {
            duration = Anim.ANIM_DURATION
            interpolator = AccelerateInterpolator()
            addUpdateListener { animation ->
                val animatedValue: Float = animation.animatedValue as Float
                //animation.animatedFraction
                _progress = animatedValue
                editText.postInvalidateOnAnimation()
            }
            start()
        }
    }

    open fun onDraw(canvas: Canvas) {
        val viewWidth = editText.mW()
        val viewHeight = editText.mH()

        val scrollX = editText.scrollX
        val scrollY = editText.scrollY

        val mLeft = editText.calcSize(drawableMarginLeft, viewWidth, viewHeight, 0, 0)
        val mRight = editText.calcSize(drawableMarginRight, viewWidth, viewHeight, 0, 0)
        val mBottom = editText.calcSize(drawableMarginBottom, viewWidth, viewHeight, 0, 0)
        val dHeight = editText.calcSize(drawableHeight, viewWidth, viewHeight)

        noFocusDrawable?.apply {
            setBounds(
                scrollX + mLeft,
                scrollY + viewHeight - dHeight - mBottom,
                scrollX + editText.measuredWidth - mRight,
                scrollY + viewHeight - mBottom
            )
            draw(canvas)
        }
        focusDrawable?.apply {
            val viewDrawWidth = viewWidth - mLeft - mRight
            val width = viewDrawWidth * _progress
            val center = scrollX + viewDrawWidth / 2 + mLeft
            setBounds(
                (center - width / 2).toInt(),
                scrollY + viewHeight - dHeight - mBottom,
                (center + width / 2).toInt(),
                scrollY + viewHeight - mBottom
            )
            draw(canvas)
        }
    }

    fun stopAnimator() {
        _valueAnimator?.cancel()
        _valueAnimator = null
    }
}