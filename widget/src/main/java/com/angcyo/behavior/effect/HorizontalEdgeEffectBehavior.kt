package com.angcyo.behavior.effect

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.refresh.RefreshEffectConfig
import com.angcyo.widget.base.mW
import com.angcyo.widget.layout.isEnableCoordinator

/**
 * 横向刷新效果的行为, 只有效果, 不触发回调.
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021-6-28
 */

open class HorizontalEdgeEffectBehavior(context: Context, attributeSet: AttributeSet? = null) :
    BaseScrollBehavior<View>(context, attributeSet) {

    //为了阻尼效果的算法
    var _refreshEffectConfig = RefreshEffectConfig()

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
        return axes.isAxisHorizontal() && coordinatorLayout.isEnableCoordinator
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        if (behaviorScrollX != 0 && dx != 0) {
            //内容产生过偏移, 那么此次的内嵌滚动肯定是需要消耗的
            consumedScrollHorizontal(dx, consumed)
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
        if (type.isTouch()) {
            val scrollInterpolation = _refreshEffectConfig.getScrollInterpolation(
                behaviorScrollX,
                -dxUnconsumed,
                child.mW()
            )
            scrollBy(scrollInterpolation, 0, SCROLL_TYPE_NESTED)
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        startScrollTo(0, 0)
    }
}