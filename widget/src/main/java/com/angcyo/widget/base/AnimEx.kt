package com.angcyo.widget.base

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.*
import com.angcyo.library.ex.c
import com.angcyo.widget.base.Anim.ANIM_DURATION

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

object Anim {
    /**动画默认时长*/
    const val ANIM_DURATION = 300L
}

fun anim(from: Int, to: Int, config: AnimatorConfig.() -> Unit = {}): ValueAnimator {
    return _animator(ValueAnimator.ofInt(from, to), config)
}

fun anim(from: Float, to: Float, config: AnimatorConfig.() -> Unit = {}): ValueAnimator {
    return _animator(ValueAnimator.ofFloat(from, to), config)
}

fun _animator(animator: ValueAnimator, config: AnimatorConfig.() -> Unit = {}): ValueAnimator {
    val animatorConfig = AnimatorConfig()

    animator.duration = ANIM_DURATION
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
    duration: Long = ANIM_DURATION,
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
    duration: Long = ANIM_DURATION,
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
    duration: Long = ANIM_DURATION,
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

/**补间动画*/
fun View.rotateAnimation(
    fromDegrees: Float = 0f,
    toDegrees: Float = 360f,
    duration: Long = ANIM_DURATION,
    interpolator: Interpolator = LinearInterpolator(),
    config: RotateAnimation.() -> Unit = {},
    onEnd: (animation: Animation) -> Unit = {}
): RotateAnimation {
    return RotateAnimation(
        fromDegrees,
        toDegrees,
        RotateAnimation.RELATIVE_TO_SELF,
        0.5f,
        RotateAnimation.RELATIVE_TO_SELF,
        0.5f
    ).apply {
        this.duration = duration
        this.interpolator = interpolator
        setAnimationListener(object : RAnimationListener() {
            override fun onAnimationEnd(animation: Animation) {
                onEnd(animation)
            }
        })
        config()
        this@rotateAnimation.startAnimation(this)
    }
}


/**
 * 揭露动画
 * https://developer.android.com/training/animation/reveal-or-hide-view#Reveal
 * */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun View.reveal(action: RevealConfig.() -> Unit = {}) {
    this.doOnPreDraw {
        val config = RevealConfig()
        config.centerX = this.measuredWidth / 2
        config.centerY = this.measuredHeight / 2
        config.endRadius = c(config.centerX.toDouble(), config.centerY.toDouble()).toFloat()

        //第一次获取基础数据
        config.action()

        ViewAnimationUtils.createCircularReveal(
            this,
            config.centerX,
            config.centerY,
            config.startRadius,
            config.endRadius
        ).apply {
            duration = 240

            config.animator = this
            //第二次获取动画数据
            config.action()
            start()
        }
    }
}

data class RevealConfig(
    var animator: Animator? = null,

    //默认是视图的中心
    var centerX: Int = 0,
    var centerY: Int = 0,

    var startRadius: Float = 0f,
    //默认是视图的对角半径
    var endRadius: Float = 0f
)

fun View.bgColorAnimator(
    from: Int,
    to: Int,
    interpolator: Interpolator = LinearInterpolator(),
    duration: Long = ANIM_DURATION,
    onEnd: (cancel: Boolean) -> Unit = {},
    config: ValueAnimator.() -> Unit = {}
) {
    //背景动画
    val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), from, to)
    colorAnimator.addUpdateListener { animation ->
        val color = animation.animatedValue as Int
        setBackgroundColor(color)
    }
    colorAnimator.addListener(object : RAnimatorListener() {
        override fun _onAnimatorFinish(animator: Animator, fromCancel: Boolean) {
            super._onAnimatorFinish(animator, fromCancel)
            onEnd(fromCancel)
        }
    })
    colorAnimator.interpolator = interpolator
    colorAnimator.duration = duration
    colorAnimator.config()
    colorAnimator.start()
}

/**
 * 抖动 放大缩小
 */
fun View.scaleAnimator(
    from: Float = 0.5f,
    to: Float = 1f,
    interpolator: Interpolator = BounceInterpolator(),
    onEnd: () -> Unit = {}
) {
    scaleAnimator(from, from, to, to, interpolator, onEnd)
}

fun View.scaleAnimator(
    fromX: Float = 0.5f,
    fromY: Float = 0.5f,
    toX: Float = 1f,
    toY: Float = 1f,
    interpolator: Interpolator = BounceInterpolator(),
    onEnd: () -> Unit = {}
) {
    scaleX = fromX
    scaleY = fromY
    animate()
        .scaleX(toX)
        .scaleY(toY)
        .setInterpolator(interpolator)
        .setDuration(ANIM_DURATION)
        .withEndAction { onEnd() }
        .start()
}