package com.angcyo.widget.base

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun anim(from: Int, to: Int, config: AnimatorConfig.() -> Unit = {}): ValueAnimator {
    return _anim(ValueAnimator.ofInt(from, to), config)
}

fun anim(from: Float, to: Float, config: AnimatorConfig.() -> Unit = {}): ValueAnimator {
    return _anim(ValueAnimator.ofFloat(from, to), config)
}

fun _anim(animator: ValueAnimator, config: AnimatorConfig.() -> Unit = {}): ValueAnimator {
    val animatorConfig = AnimatorConfig()

    animator.duration = 3000
    animator.interpolator = LinearInterpolator()
    animator.addUpdateListener {
        animatorConfig.onAnimatorUpdateValue(it.animatedValue, it.animatedFraction)
    }

    animatorConfig.config()
    animatorConfig.onAnimatorConfig(animator)

    animator.start()
    return animator
}

class AnimatorConfig {
    var onAnimatorConfig: (animator: ValueAnimator) -> Unit = {}

    var onAnimatorUpdateValue: (value: Any, fraction: Float) -> Unit = { _, _ -> }
}