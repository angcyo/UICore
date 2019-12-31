package com.angcyo.widget.base

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun anim(from: Int, to: Int, config: AnimatorConfig.() -> Unit = {}): ValueAnimator {
    return _animator(ValueAnimator.ofInt(from, to), config)
}

fun anim(from: Float, to: Float, config: AnimatorConfig.() -> Unit = {}): ValueAnimator {
    return _animator(ValueAnimator.ofFloat(from, to), config)
}

fun _animator(animator: ValueAnimator, config: AnimatorConfig.() -> Unit = {}): ValueAnimator {
    val animatorConfig = AnimatorConfig()

    animator.duration = 300
    animator.interpolator = LinearInterpolator()
    animator.addUpdateListener {
        animatorConfig.onAnimatorUpdateValue(it.animatedValue, it.animatedFraction)
    }
    animator.addListener(RAnimatorListener().apply {
        onAnimatorFinish = { _, _ ->
            animatorConfig.onAnimatorEnd(animator)
        }
    })

    animatorConfig.config()
    animatorConfig.onAnimatorConfig(animator)

    animator.start()
    return animator
}

class AnimatorConfig {
    var onAnimatorConfig: (animator: ValueAnimator) -> Unit = {}

    var onAnimatorUpdateValue: (value: Any, fraction: Float) -> Unit = { _, _ -> }

    var onAnimatorEnd: (animator: ValueAnimator) -> Unit = {}
}

/**缩放属性动画*/
fun View.scale(
    from: Float,
    to: Float,
    duration: Long = 300,
    interpolator: Interpolator = LinearInterpolator(),
    onEnd: () -> Unit = {}
): ValueAnimator {
    return anim(from, to) {
        onAnimatorUpdateValue = { value, _ ->
            scaleX = value as Float
            scaleY = scaleX
        }

        onAnimatorConfig = {
            it.duration = duration
            it.interpolator = interpolator
            onAnimatorEnd = { _ -> onEnd() }
        }
    }
}

/**平移属性动画*/
fun View.translationX(
    from: Float,
    to: Float,
    duration: Long = 300,
    interpolator: Interpolator = LinearInterpolator(),
    onEnd: () -> Unit = {}
): ValueAnimator {
    return anim(from, to) {
        onAnimatorUpdateValue = { value, _ ->
            translationX = value as Float
        }

        onAnimatorConfig = {
            it.duration = duration
            it.interpolator = interpolator
            onAnimatorEnd = { _ -> onEnd() }
        }
    }
}

fun View.translationY(
    from: Float,
    to: Float,
    duration: Long = 300,
    interpolator: Interpolator = LinearInterpolator(),
    onEnd: () -> Unit = {}
): ValueAnimator {
    return anim(from, to) {
        onAnimatorUpdateValue = { value, _ ->
            translationY = value as Float
        }

        onAnimatorConfig = {
            it.duration = duration
            it.interpolator = interpolator
            onAnimatorEnd = { _ -> onEnd() }
        }
    }
}