package com.angcyo.drawable.progress

import android.animation.Animator
import android.graphics.Color
import android.graphics.Shader
import androidx.annotation.Px
import androidx.core.math.MathUtils
import com.angcyo.drawable.InvalidateDrawableProperty
import com.angcyo.drawable.base.BaseDrawable
import com.angcyo.library.ex.anim
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/03
 */
abstract class BaseValueProgressDrawable : BaseDrawable() {

    //region ---配置变量---

    /**最小和最大值*/
    var minProgressValue: Int = 0

    var maxProgressValue: Int = 100

    /**当前的进度值*/
    var currentProgressValue: Int by InvalidateDrawableProperty(0)

    /**进度的宽度*/
    @Px
    var progressWidth: Float = 30f

    /**进度的渐变颜色, 至少需要2个颜色*/
    var progressGradientColors = intArrayOf(Color.WHITE, Color.WHITE)

    /**背景的宽度*/
    @Px
    var backgroundWidth: Float = 20f

    /**背景的渐变颜色, 至少需要2个颜色*/
    var backgroundGradientColors = intArrayOf(Color.BLACK, Color.BLACK)

    //endregion ---配置变量---

    //region ---临时变量---

    /**当前进度比例, [0~1]*/
    val progressRatio: Float
        get() = min(
            1f,
            max(
                0f,
                (currentProgressValue - minProgressValue) * 1f / (maxProgressValue - minProgressValue)
            )
        )

    var _progressShader: Shader? = null
    var _backgroundShader: Shader? = null

    var _animtor: Animator? = null

    //endregion ---临时变量---

    /**限制设置的非法进度值*/
    open fun validProgressValue(progressValue: Int): Int {
        return MathUtils.clamp(progressValue, minProgressValue, maxProgressValue)
    }

    /**更新进度值, 并且支持动画控制*/
    open fun updateProgressValue(
        progressValue: Int,
        fromProgress: Int = currentProgressValue,
        animDuration: Long = 300L
    ) {
        _animtor?.cancel()
        _animtor = null
        val p = validProgressValue(progressValue)
        if (animDuration > 0 && fromProgress != p) {
            //需要动画
            _animtor = anim(fromProgress, p) {
                onAnimatorConfig = {
                    it.duration = animDuration
                }
                onAnimatorUpdateValue = { value, _ ->
                    currentProgressValue = value as Int
                    invalidateSelf()
                }
            }
        } else {
            //不需要动画
            currentProgressValue = progressValue
            invalidateSelf()
        }
    }

}