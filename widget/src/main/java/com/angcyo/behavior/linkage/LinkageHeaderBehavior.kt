package com.angcyo.behavior.linkage

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import com.angcyo.library.L
import com.angcyo.widget.base.mH
import com.angcyo.widget.base.offsetTopTo
import kotlin.math.min

/**
 * 头/悬浮/尾 联动滚动, 头部的行为
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class LinkageHeaderBehavior(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseLinkageBehavior(context, attributeSet) {

    val minScroll: Int
        get() = -(stickyView.mH() + min(headerView.mH(), footerView.mH()))

    val maxScroll: Int
        get() = 0

    /**不管Footer是否可以滚动, 都优先滚动Header*/
    var priorityHeader = false //优先滚动头部

    init {
        showLog = false
        onScrollTo = { x, y ->
            //L.w("scrollTo:$y")
            childView?.offsetTopTo(y)
        }
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        headerView = child
        return super.layoutDependsOn(parent, child, dependency)
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

        if (target == footerScrollView) {
            //如果是底部传来的内嵌滚动
            if (priorityHeader || (scrollY != minScroll && scrollY != maxScroll) /*防止头部滚动一半的情况*/) {
                consumedScrollVertical(dy, scrollY, minScroll, maxScroll, consumed)
            } else {
                //这里处理Footer不能滚动时, 再滚动
                if (dy > 0 && scrollY != maxScroll) {
                    //手指向上滑动
                    consumedScrollVertical(dy, scrollY, minScroll, maxScroll, consumed)
                } else if (scrollY != minScroll) {
                    consumedScrollVertical(dy, scrollY, minScroll, maxScroll, consumed)
                }
            }
        } else if (scrollY != 0 && target == headerScrollView) {
            //内容产生过偏移, 那么此次的内嵌滚动肯定是需要消耗的
            consumedScrollVertical(dy, consumed)
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
        onHeaderOverScroll(-dyUnconsumed)
    }

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        super.onMeasureChild(
            parent,
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed
        )
        //parent.onMeasureChild()
        return false
    }

    /**头部到达边界的滚动处理*/
    fun onHeaderOverScroll(dy: Int) {
        val scroll = MathUtils.clamp(scrollY + dy, minScroll, maxScroll)
        scrollTo(scrollX, scroll)
    }
}