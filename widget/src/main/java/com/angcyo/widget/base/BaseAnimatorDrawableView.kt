package com.angcyo.widget.base

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/10
 */

abstract class BaseAnimatorDrawableView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    /**配置动画*/
    var onConfigAnimator: (animator: ValueAnimator) -> Unit = {}

    /**动画更新*/
    var onAnimatorUpdate: (value: Float) -> Unit = {}

    var autoStartAnimator: Boolean = false

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            startAnimatorInner()
        } else {
            stopAnimator()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimatorInner()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimator()
    }

    var valueAnimator: ValueAnimator? = null

    open fun startAnimatorInner() {
        //判读是否需要自动开始动画
        if (autoStartAnimator) {
            startAnimator()
        }
    }

    /**开始进度动画*/
    open fun startAnimator() {
        //开始动画
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 300
                addUpdateListener { animation ->
                    val animatedValue: Float = animation.animatedValue as Float
                    //animation.animatedFraction
                    onAnimatorUpdate(animatedValue)
                    postInvalidateOnAnimation()
                }
                onConfigAnimator(this)
                start()
            }
        }
    }

    /**停止进度动画*/
    open fun stopAnimator() {
        //结束动画
        valueAnimator?.cancel()
        valueAnimator = null
        postInvalidateOnAnimation()
    }
}
