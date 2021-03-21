package com.angcyo.transition

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/16
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class AlphaTransition : Transition() {
    companion object {
        private const val KEY = "android:AlphaTransition:alpha"
    }

    override fun captureStartValues(values: TransitionValues) {
        captureValues(values)
    }

    override fun captureEndValues(values: TransitionValues) {
        captureValues(values)
    }

    private fun captureValues(values: TransitionValues) {
        val view = values.view
        values.values[KEY] = view.alpha
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues?.values?.isNotEmpty() == true && endValues?.values?.isNotEmpty() == true) {
            val startAlpha: Float = startValues.values[KEY] as Float
            val endAlpha: Float = endValues.values[KEY] as Float

            if (startAlpha != endAlpha) {
                val animator = ValueAnimator.ofFloat(startAlpha, endAlpha)
                animator.addUpdateListener { animation ->
                    val value = animation.animatedValue as Float
                    endValues.view.alpha = value
                }
                //animator.interpolator = interpolator
                //animator.duration = duration
                return animator
            }
        }
        return null
    }
}