package com.angcyo.behavior.refresh

import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.BehaviorInterpolator
import kotlin.math.absoluteValue

/**
 * 刷新行为处理类, 默认只有刷新效果, 没有刷新回调触发
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/07
 */

class RefreshEffectConfig : IRefreshBehavior {

    /**输入dy, 输出修正后的dy*/
    var behaviorInterpolator: BehaviorInterpolator = object : BehaviorInterpolator {
        override fun getInterpolation(behavior: BaseScrollBehavior<*>, input: Int, max: Int): Int {
            val f = behavior.scrollY.absoluteValue * 1f / max
            return when {
                f < 0.1f -> input
                f < 0.2f -> (input * 0.7f).toInt()
                f < 0.3f -> (input * 0.3f).toInt()
                f < 0.4f -> (input * 0.1f).toInt()
                else -> 0
            }
        }
    }

    override fun onContentOverScroll(behavior: RefreshBehavior, dx: Int, dy: Int) {
        val scrollY = behavior.scrollY
        val result: Int
        if (scrollY > 0) {
            result = if (dy < 0) {
                //继续下拉, 才需要阻尼, 反向不需要
                behaviorInterpolator.getInterpolation(
                    behavior,
                    -dy,
                    behavior.childView.measuredHeight
                )
            } else {
                -dy
            }
        } else {
            result = if (dy > 0) {
                //继续上拉, 才需要阻尼, 反向不需要
                behaviorInterpolator.getInterpolation(
                    behavior,
                    -dy,
                    behavior.childView.measuredHeight
                )
            } else {
                -dy
            }
        }

        behavior.scrollBy(0, result)
    }
}