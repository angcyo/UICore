package com.angcyo.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.animation.AccelerateInterpolator
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex._integer
import com.angcyo.library.ex.animatorOf

/**
 * [Fragment]切换动画约束
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object FragmentAnimator {

    /**显示进入动画*/
    var DEFAULT_SHOW_ENTER_ANIMATOR = R.anim.lib_translate_x_show_enter
    //R.anim.lib_translate_x_show_enter //R.anim.lib_x_show_enter_holder

    var DEFAULT_SHOW_EXIT_ANIMATOR = R.anim.lib_translate_x_show_exit
    //R.anim.lib_translate_x_show_exit //R.anim.lib_x_show_exit_holder

    var DEFAULT_REMOVE_ENTER_ANIMATOR = R.anim.lib_translate_x_remove_enter
    //R.anim.lib_translate_x_remove_enter //R.anim.lib_x_remove_enter_holder

    /**隐藏时退出动画*/
    var DEFAULT_REMOVE_EXIT_ANIMATOR = R.anim.lib_translate_x_remove_exit
    //R.anim.lib_translate_x_remove_exit //R.anim.lib_x_remove_exit_holder

    /**显示/隐藏 全平移动画
     * [translationX]*/
    fun allTranslateAnim() {
        DEFAULT_SHOW_ENTER_ANIMATOR = R.anim.lib_translate_x_show_enter
        DEFAULT_SHOW_EXIT_ANIMATOR = R.anim.lib_translate_x_show_exit

        DEFAULT_REMOVE_ENTER_ANIMATOR = R.anim.lib_translate_x_remove_enter
        DEFAULT_REMOVE_EXIT_ANIMATOR = R.anim.lib_translate_x_remove_exit
    }

    /**显示/隐藏 只有顶层平移动画, 顶层单独使用属性动画, 可以提高部分动画性能
     * [translationX]*/
    fun onlyTopAnim() {
        DEFAULT_SHOW_ENTER_ANIMATOR = R.anim.lib_x_show_enter_holder
        DEFAULT_SHOW_EXIT_ANIMATOR = 0

        DEFAULT_REMOVE_ENTER_ANIMATOR = 0
        DEFAULT_REMOVE_EXIT_ANIMATOR = R.anim.lib_x_remove_exit_holder
    }

    /**从80%开始动画, 提高动画的流畅度
     * [alpha] [translationX]*/
    fun onlyTopAnim2() {
        DEFAULT_SHOW_ENTER_ANIMATOR = R.anim.lib_x_show_enter_holder2
        DEFAULT_SHOW_EXIT_ANIMATOR = R.anim.lib_x_show_exit_holder2

        DEFAULT_REMOVE_ENTER_ANIMATOR = R.anim.lib_x_remove_enter_holder2
        DEFAULT_REMOVE_EXIT_ANIMATOR = R.anim.lib_x_remove_exit_holder2
    }

    /**顶部缩放透明属性动画
     * [scale] [alpha]*/
    fun onlyTopScale() {
        DEFAULT_SHOW_ENTER_ANIMATOR = R.anim.lib_scale_show_enter_holder
        DEFAULT_SHOW_EXIT_ANIMATOR = R.anim.lib_scale_show_exit_holder

        DEFAULT_REMOVE_ENTER_ANIMATOR = R.anim.lib_scale_remove_enter_holder
        DEFAULT_REMOVE_EXIT_ANIMATOR = R.anim.lib_scale_remove_exit_holder
    }

    /**显示/隐藏 只有顶层Y平移动画, 顶层单独使用属性动画, 可以提高部分动画性能
     * [translationY]*/
    fun onlyTopYAnim() {
        DEFAULT_SHOW_ENTER_ANIMATOR = R.anim.lib_y_show_enter_holder
        DEFAULT_SHOW_EXIT_ANIMATOR = 0

        DEFAULT_REMOVE_ENTER_ANIMATOR = 0
        DEFAULT_REMOVE_EXIT_ANIMATOR = R.anim.lib_y_remove_exit_holder
    }

    /**加载动画对象*/
    var loadAnimator: (context: Context, anim: Int) -> Animator? = { context, anim ->
        val sw = _screenWidth.toFloat()
        val sh = _screenHeight.toFloat()
        val duration = _integer(R.integer.lib_animation_duration).toLong()

        val animator = ObjectAnimator()
        animator.duration = duration

        /*将占位的动画, 翻译成属性动画*/
        when (anim) {
            R.anim.lib_y_show_enter_holder -> {
                animator.interpolator = AccelerateInterpolator()
                animator.setPropertyName("translationY")
                animator.setFloatValues(sh, 0f)
                animator
            }

            R.anim.lib_y_remove_exit_holder -> {
                animator.interpolator = AccelerateInterpolator()
                animator.setPropertyName("translationY")
                animator.setFloatValues(0f, sh)
                animator
            }

            R.anim.lib_x_show_enter_holder -> {
                animator.interpolator = AccelerateInterpolator()
                animator.setPropertyName("translationX")
                animator.setFloatValues(sw, 0f)
                animator
            }

            R.anim.lib_x_show_enter_holder2 -> {
                val set = AnimatorSet()
                set.duration = duration
                set.interpolator = AccelerateInterpolator()

                animator.setPropertyName("translationX")
                animator.setFloatValues(sw * 0.2f, 0f)

                val alpha = ObjectAnimator()
                alpha.setPropertyName("alpha")
                alpha.setFloatValues(0f, 1f)

                set.playTogether(animator, alpha)
                set
            }

            R.anim.lib_x_show_exit_holder -> {
                animator.setPropertyName("translationX")
                animator.setFloatValues(0f, -sw * 0.8f)
                animator.interpolator = AccelerateInterpolator()
                animator
            }

            R.anim.lib_x_show_exit_holder2 -> {
                animator.setPropertyName("translationX")
                animator.setFloatValues(0f, -sw * 0.1f)
                animator.interpolator = AccelerateInterpolator()
                animator
            }

            R.anim.lib_x_remove_enter_holder -> {
                animator.interpolator = AccelerateInterpolator()
                animator.setPropertyName("translationX")
                animator.setFloatValues(-sw, 0f)
                animator
            }

            R.anim.lib_x_remove_enter_holder2 -> {
                animator.interpolator = AccelerateInterpolator()
                animator.setPropertyName("translationX")
                animator.setFloatValues(-sw * 0.1f, 0f)
                animator
            }

            R.anim.lib_x_remove_exit_holder -> {
                animator.interpolator = AccelerateInterpolator()
                animator.setPropertyName("translationX")
                animator.setFloatValues(0f, sw)
                animator
            }

            R.anim.lib_x_remove_exit_holder2 -> {
                val set = AnimatorSet()
                set.duration = duration
                set.interpolator = AccelerateInterpolator()

                animator.setPropertyName("translationX")
                animator.setFloatValues(0f, sw * 0.2f)

                val alpha = ObjectAnimator()
                alpha.setPropertyName("alpha")
                alpha.setFloatValues(1f, 0f)

                set.playTogether(animator, alpha)
                set
            }

            //scale
            R.anim.lib_scale_show_enter_holder -> {
                val set = AnimatorSet()
                set.duration = duration
                set.interpolator = AccelerateInterpolator()

                val scaleX = ObjectAnimator()
                scaleX.setPropertyName("scaleX")
                scaleX.setFloatValues(0.8f, 1f)
                val scaleY = ObjectAnimator()
                scaleY.setPropertyName("scaleY")
                scaleY.setFloatValues(0.8f, 1f)

                val alpha = ObjectAnimator()
                alpha.setPropertyName("alpha")
                alpha.setFloatValues(0.8f, 1f)

                set.playTogether(scaleX, scaleY, alpha)
                set
            }

            R.anim.lib_scale_show_exit_holder -> {
                val set = AnimatorSet()
                set.duration = duration
                set.interpolator = AccelerateInterpolator()

                val scaleX = ObjectAnimator()
                scaleX.setPropertyName("scaleX")
                scaleX.setFloatValues(1f, 1.2f)
                val scaleY = ObjectAnimator()
                scaleY.setPropertyName("scaleY")
                scaleY.setFloatValues(1f, 1.2f)

                val alpha = ObjectAnimator()
                alpha.setPropertyName("alpha")
                alpha.setFloatValues(1f, 0.8f)

                set.playTogether(scaleX, scaleY, alpha)
                set
            }

            R.anim.lib_scale_remove_exit_holder -> {
                val set = AnimatorSet()
                set.duration = duration
                set.interpolator = AccelerateInterpolator()

                val scaleX = ObjectAnimator()
                scaleX.setPropertyName("scaleX")
                scaleX.setFloatValues(1f, 0.8f)
                val scaleY = ObjectAnimator()
                scaleY.setPropertyName("scaleY")
                scaleY.setFloatValues(1f, 0.8f)

                val alpha = ObjectAnimator()
                alpha.setPropertyName("alpha")
                alpha.setFloatValues(1f, 0f)

                set.playTogether(scaleX, scaleY, alpha)
                set
            }

            R.anim.lib_scale_remove_enter_holder -> {
                val set = AnimatorSet()
                set.duration = duration
                set.interpolator = AccelerateInterpolator()

                val scaleX = ObjectAnimator()
                scaleX.setPropertyName("scaleX")
                scaleX.setFloatValues(1.2f, 1f)
                val scaleY = ObjectAnimator()
                scaleY.setPropertyName("scaleY")
                scaleY.setFloatValues(1.2f, 1f)

                val alpha = ObjectAnimator()
                alpha.setPropertyName("alpha")
                alpha.setFloatValues(0.8f, 1f)

                set.playTogether(scaleX, scaleY, alpha)
                set
            }

            else -> animatorOf(context, anim)
        }
    }
}