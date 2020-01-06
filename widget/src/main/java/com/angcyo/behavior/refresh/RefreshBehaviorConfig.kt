package com.angcyo.behavior.refresh

import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.BehaviorInterpolator
import com.angcyo.widget.base.offsetTopTo
import kotlin.math.absoluteValue

/**
 * 刷新行为处理类, 默认只有刷新效果, 没有刷新回调触发
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/06
 */

open class RefreshBehaviorConfig {

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

    /**当内容滚动时, 界面需要处理的回调*/
    var onContentScrollTo: (behavior: RefreshBehavior, x: Int, y: Int) -> Unit =
        { behavior, _, y ->
            behavior.childView.offsetTopTo(y + behavior.offsetTop)
        }

    /**当内容over滚动时回调*/
    var onContentOverScroll: (behavior: RefreshBehavior, dx: Int, dy: Int) -> Unit =
        { behavior, _, dy ->
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

    /**内容停止了滚动, 此时需要恢复界面*/
    var onContentStopScroll: (behavior: RefreshBehavior) -> Unit = {
        if (it.scrollY != 0) {
            it.startScrollTo(0, 0)
        }
    }
}