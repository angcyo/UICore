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
import com.angcyo.library.ex.Anim
import com.angcyo.library.ex.mH
import com.angcyo.library.ex.mW

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
open class FocusEditDelegate(editText: EditText) : PatternEditDelegate(editText) {

    /**无焦点时, 绘制的Drawable*/
    var noFocusDrawable: Drawable? = null
    var focusDrawable: Drawable? = null

    /**指定[Drawable]的高度*/
    var drawableHeight: String? = "1dp"

    /**计算表达式, 支持 sh ph px dip, 正数是倍数, 负数是减去倍数的值*/
    var drawableMarginLeft: String? = null
    var drawableMarginRight: String? = null
    var drawableMarginBottom: String? = null

    /**动画持续时长*/
    var animatorDuration = 600

    var _progress: Float = 0f
    var _valueAnimator: ValueAnimator? = null

    override fun initAttribute(context: Context, attrs: AttributeSet?) {
        super.initAttribute(context, attrs)

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
    override fun onFocusChanged(focused: Boolean) {
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

    var mDrawableLeft = 0
    var mDrawableRight = 0
    var mDrawableBottom = 0
    var mDrawableHeight = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mDrawableLeft = editText.calcSize(drawableMarginLeft, w, h, 0, 0)
        mDrawableRight = editText.calcSize(drawableMarginRight, w, h, 0, 0)
        mDrawableBottom = editText.calcSize(drawableMarginBottom, w, h, 0, 0)
        mDrawableHeight = editText.calcSize(drawableHeight, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = editText.mW()
        val viewHeight = editText.mH()

        val scrollX = editText.scrollX
        val scrollY = editText.scrollY

        //focusDrawable/noFocusDrawable 定位
        noFocusDrawable?.apply {
            setBounds(
                scrollX + mDrawableLeft,
                scrollY + viewHeight - mDrawableHeight - mDrawableBottom,
                scrollX + editText.measuredWidth - mDrawableRight,
                scrollY + viewHeight - mDrawableBottom
            )
            draw(canvas)
        }
        focusDrawable?.apply {
            val viewDrawWidth = viewWidth - mDrawableLeft - mDrawableRight
            val width = viewDrawWidth * _progress
            val center = scrollX + viewDrawWidth / 2 + mDrawableLeft
            setBounds(
                (center - width / 2).toInt(),
                scrollY + viewHeight - mDrawableHeight - mDrawableBottom,
                (center + width / 2).toInt(),
                scrollY + viewHeight - mDrawableBottom
            )
            draw(canvas)
        }
    }

    fun stopAnimator() {
        _valueAnimator?.cancel()
        _valueAnimator = null
    }
}