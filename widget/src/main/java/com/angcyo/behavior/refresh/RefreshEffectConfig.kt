package com.angcyo.behavior.refresh

import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.BehaviorInterpolator
import com.angcyo.widget.base.mH
import kotlin.math.absoluteValue

/**
 * 刷新行为处理类, 默认只有刷新效果, 没有刷新回调触发
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/07
 */

open class RefreshEffectConfig : IRefreshBehavior {

    /**顶部over效果*/
    var enableTopOver: Boolean = true

    /**底部over效果*/
    var enableBottomOver: Boolean = true

    /**最大的分母*/
    var maxEffectHeight: Int = -1

    /**输入dy, 输出修正后的dy*/
    var behaviorInterpolator: BehaviorInterpolator = object : BehaviorInterpolator {
        override fun getInterpolation(scroll: Int, input: Int, max: Int): Int {
            return getScrollInterpolation(scroll, input, max)
        }
    }

    fun getScrollInterpolation(scroll: Int, input: Int, max: Int): Int {
        val f = scroll.absoluteValue * 1f / max
        return when {
            f < 0.1f -> input
            f < 0.2f -> (input * 0.7f).toInt()
            f < 0.3f -> (input * 0.3f).toInt()
            f < 0.4f -> (input * 0.1f).toInt()
            else -> 0
        }
    }

    fun getContentOverScrollValue(scroll: Int, maxScroll: Int, value: Int): Int {
        val result = if (scroll > 0) {
            if (value < 0) {
                //继续下拉, 才需要阻尼, 反向不需要
                behaviorInterpolator.getInterpolation(scroll, -value, maxScroll)
            } else {
                -value
            }
        } else {
            if (value > 0) {
                //继续上拉, 才需要阻尼, 反向不需要
                behaviorInterpolator.getInterpolation(scroll, -value, maxScroll)
            } else {
                -value
            }
        }
        return result
    }

    override fun onContentOverScroll(behavior: BaseScrollBehavior<*>, dx: Int, dy: Int) {
        if (dy > 0) {
            if (!enableBottomOver) {
                return
            }
        } else {
            if (!enableTopOver) {
                return
            }
        }

        val scrollY = behavior.scrollY

        val maxScroll = if (maxEffectHeight > 0) {
            maxEffectHeight
        } else {
            behavior.childView.mH()
        }

        val result = if (scrollY > 0) {
            if (dy < 0) {
                //继续下拉, 才需要阻尼, 反向不需要
                behaviorInterpolator.getInterpolation(scrollY, -dy, maxScroll)
            } else {
                -dy
            }
        } else {
            if (dy > 0) {
                //继续上拉, 才需要阻尼, 反向不需要
                behaviorInterpolator.getInterpolation(scrollY, -dy, maxScroll)
            } else {
                -dy
            }
        }

        behavior.scrollBy(0, result)
    }
}