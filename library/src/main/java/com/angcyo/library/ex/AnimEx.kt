package com.angcyo.library.ex

import android.animation.*
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Matrix
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.*
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.core.view.doOnPreDraw
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.component.MatrixEvaluator
import com.angcyo.library.component.RAnimationListener
import com.angcyo.library.component.RAnimatorListener
import com.angcyo.library.ex.Anim.ANIM_DURATION

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

object Anim {
    /**动画默认时长*/
    var ANIM_DURATION = 300L
}

/**从指定资源id中, 加载动画[Animation]*/
fun animationOf(context: Context = app(), @AnimRes id: Int): Animation? {
    try {
        if (id == 0 || id == -1) {
            return null
        }
        return AnimationUtils.loadAnimation(context, id)
    } catch (e: Exception) {
        L.w(e)
        return null
    }
}

/**从指定资源id中, 加载动画[Animator]*/
fun animatorOf(context: Context = app(), @AnimatorRes id: Int): Animator? {
    try {
        if (id == 0 || id == -1) {
            return null
        }
        return AnimatorInflater.loadAnimator(context, id)
    } catch (e: Exception) {
        L.w(e)
        return null
    }
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

/**颜色渐变动画*/
fun colorAnimator(
    fromColor: Int,
    toColor: Int,
    infinite: Boolean = false,
    interpolator: Interpolator = LinearInterpolator(),
    duration: Long = ANIM_DURATION,
    onEnd: (cancel: Boolean) -> Unit = {},
    config: ValueAnimator.() -> Unit = {},
    onUpdate: (animator: ValueAnimator, color: Int) -> Unit
): ValueAnimator {
    //颜色动画
    val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
    colorAnimator.addUpdateListener { animation ->
        val color = animation.animatedValue as Int
        onUpdate(animation, color)
    }
    colorAnimator.addListener(object : RAnimatorListener() {
        override fun _onAnimatorFinish(animator: Animator, fromCancel: Boolean) {
            super._onAnimatorFinish(animator, fromCancel)
            onEnd(fromCancel)
        }
    })
    colorAnimator.interpolator = interpolator
    colorAnimator.duration = duration
    if (infinite) {
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.repeatMode = ValueAnimator.REVERSE
    }
    colorAnimator.config()
    colorAnimator.start()
    return colorAnimator
}

/**一组颜色变化的动画*/
fun colorListAnimator(
    colorList: List<Int>,
    infinite: Boolean = false,
    interpolator: Interpolator = LinearInterpolator(),
    duration: Long = colorList.size() * 1000L,
    onEnd: (cancel: Boolean) -> Unit = {},
    config: ValueAnimator.() -> Unit = {},
    onUpdate: (animator: ValueAnimator, color: Int) -> Unit
): ValueAnimator {
    //是否需要反序
    var reverse = false
    val animator = ValueAnimator.ofFloat(0f, 1f)
    animator.addUpdateListener { animation ->
        val section = colorList.size()
        if (section <= 1) {
            onUpdate(animation, colorList[0])
        } else {
            //每一段能运行的时间
            val sectionTime = duration / (section - 1)
            //当前在那一段
            val time = animation.currentPlayTime * 1f % duration //取模调整时间
            val currentStep: Int = (time / sectionTime).floor().toInt()

            //获取需要变化的颜色
            val startColor: Int
            val endColor: Int
            if (reverse) {
                val startIndex = section - currentStep - 1
                endColor = colorList[startIndex]
                startColor = colorList.getOrNull(startIndex - 1) ?: colorList.first()
            } else {
                startColor = colorList[currentStep]
                endColor = colorList.getOrNull(currentStep + 1) ?: colorList.last()
            }

            //当前的进度
            val animatedValue = animation.animatedValue as Float
            val sectionProgress = 1f / (section - 1)
            val currentProgress: Float =
                interpolator.getInterpolation(animatedValue % sectionProgress / sectionProgress)

            onUpdate(animation, evaluateColor(currentProgress, startColor, endColor))
        }
    }
    animator.addListener(object : RAnimatorListener() {

        override fun onAnimationRepeat(animation: Animator) {
            super.onAnimationRepeat(animation)
            if (animator.repeatMode == ValueAnimator.REVERSE) {
                reverse = !reverse
            }
        }

        override fun _onAnimatorFinish(animator: Animator, fromCancel: Boolean) {
            super._onAnimatorFinish(animator, fromCancel)
            onEnd(fromCancel)
        }
    })
    animator.interpolator = LinearInterpolator()
    animator.duration = duration
    if (infinite) {
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
    }
    animator.config()
    animator.start()
    return animator
}

/**背景变化动画*/
fun View.bgColorAnimator(
    fromColor: Int,
    toColor: Int,
    infinite: Boolean = false,
    interpolator: Interpolator = LinearInterpolator(),
    duration: Long = ANIM_DURATION,
    onEnd: (cancel: Boolean) -> Unit = {},
    config: ValueAnimator.() -> Unit = {}
): ValueAnimator {
    //背景动画
    return colorAnimator(
        fromColor,
        toColor,
        infinite,
        interpolator,
        duration,
        onEnd,
        config
    ) { _, color ->
        setBackgroundColor(color)
    }
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

/**Matrix动画*/
fun matrixAnimator(startMatrix: Matrix, endMatrix: Matrix, block: (Matrix) -> Unit): ValueAnimator {
    return ObjectAnimator.ofObject(MatrixEvaluator(), startMatrix, endMatrix).apply {
        duration = ANIM_DURATION
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            block(it.animatedValue as Matrix)
        }
        start()
    }
}