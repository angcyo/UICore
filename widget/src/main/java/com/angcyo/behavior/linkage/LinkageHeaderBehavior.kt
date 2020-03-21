package com.angcyo.behavior.linkage

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.NestedScrollingChild
import com.angcyo.behavior.ITitleBarBehavior
import com.angcyo.behavior.refresh.IRefreshBehavior
import com.angcyo.behavior.refresh.RefreshEffectConfig
import com.angcyo.widget.R
import com.angcyo.widget.base.*
import kotlin.math.abs
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
        get() {
            val headerHeight = headerView.mH() - usedHeight
            val otherHeight = footerView.mH() + stickyView.mH()
            return -min(headerHeight, otherHeight)
        }

    val maxScroll: Int
        get() = 0

    /**使用掉的高度, Footer计算高度时, 需要减去此高度*/
    val usedHeight: Int
        get() {
            var result = 0
            if (fixTitleBar && titleBarBehavior != null) {
                result += titleBarBehavior?.getContentExcludeHeight(this) ?: 0
            } else if (fixStatusBar) {
                result += childView?.getStatusBarHeight() ?: 0
            }
            result += fixScrollTopOffset
            return result
        }

    /**不管Footer是否可以滚动, 都优先滚动Header*/
    var priorityHeader = false //优先滚动头部

    /**滚动最小值, 要考虑标题栏的高度*/
    var fixTitleBar: Boolean = true

    /**滚动最小值, 要考虑状态栏的高度*/
    var fixStatusBar: Boolean = true

    /**滚动最小值, 额外要考虑的距离*/
    var fixScrollTopOffset: Int = 0

    /**激活顶部Over效果. 当滚动到顶的时候, 可以继续滚动*/
    var enableTopOverScroll: Boolean = true

    /**标题栏*/
    var titleBarBehavior: ITitleBarBehavior? = null

    init {
        showLog = false

        val array =
            context.obtainStyledAttributes(attributeSet, R.styleable.LinkageHeaderBehavior_Layout)
        fixTitleBar = array.getBoolean(
            R.styleable.LinkageHeaderBehavior_Layout_layout_fix_title_bar,
            fixTitleBar
        )
        fixStatusBar = array.getBoolean(
            R.styleable.LinkageHeaderBehavior_Layout_layout_fix_status_bar,
            fixStatusBar
        )
        enableTopOverScroll = array.getBoolean(
            R.styleable.LinkageHeaderBehavior_Layout_layout_enable_top_over_scroll,
            enableTopOverScroll
        )
        fixScrollTopOffset = array.getDimensionPixelOffset(
            R.styleable.LinkageHeaderBehavior_Layout_layout_scroll_top_offset,
            fixScrollTopOffset
        )
        array.recycle()

        onScrollTo = { x, y ->
            //L.w("scrollTo:$y")
            childView?.offsetTopTo(y)
        }
    }

    //<editor-fold desc="内嵌滚动处理">

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        headerView = child

        dependency.behavior()?.apply {
            if (this is ITitleBarBehavior) {
                titleBarBehavior = this
            }
        }

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
        } else if (target == headerScrollView) {
            if (scrollY > maxScroll && enableTopOverScroll) {

            } else if (scrollY != 0) {
                //内容产生过偏移, 那么此次的内嵌滚动肯定是需要消耗的
                consumedScrollVertical(dy, consumed)
            }
        } else {
            //其他位置发生的内嵌滚动, 比如 Sticky
            onNestedPreScrollOther(target, dy, consumed)
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
        onHeaderOverScroll(target, -dyUnconsumed)
    }

    //</editor-fold desc="内嵌滚动处理">

    //<editor-fold desc="其他滚动处理">

    /**其他位置发生的内嵌滚动处理, 比如Sticky*/
    fun onNestedPreScrollOther(target: View?, dy: Int, consumed: IntArray) {
        //当无内嵌滚动的view访问, 此时发生了滚动的情况下.
        //优先处理footer滚动, 其次处理header滚动
        val nestedScrollingChild = footerScrollView
        val footerView: View? = if (nestedScrollingChild is View) {
            nestedScrollingChild
        } else {
            null
        }

        if (dy > 0) {
            //手指向上滑动
            consumedScrollVertical(
                dy,
                scrollY,
                minScroll,
                maxScroll,
                consumed
            )
            if (consumed[1] == 0) {
                //不需要消耗了
                footerView?.scrollBy(0, dy)
            }
        } else {
            //手指向下滚动
            if (footerView.topCanScroll()) {
                footerView?.scrollBy(0, dy)
                consumed[1] = dy
            } else {
                if (_nestedScrollView == null) {
                    //没有内嵌滚动访问, Touch事件导致的滑动, 就偏移Header
                    onHeaderOverScroll(target, -dy)
                }
            }
        }
    }

    //over阻尼效果
    var _overScrollEffect: IRefreshBehavior = RefreshEffectConfig()

    /**头部到达边界的滚动处理*/
    fun onHeaderOverScroll(target: View?, dy: Int) {
        var isOverScroll = false

        if (enableTopOverScroll) {
            isOverScroll = scrollY >= maxScroll
                    && dy > 0
                    && !headerScrollView.topCanScroll()
                    && !footerScrollView.topCanScroll()
                    && !stickyScrollView.topCanScroll()
        }

        if (isOverScroll) {
            if (isTouchHold) {
                _overScrollEffect.onContentOverScroll(this, 0, -dy)
            } else {
                //fling, 数值放大3倍
                _overScrollEffect.onContentOverScroll(this, 0, -dy * 4)
            }
            if (!isTouchHold || !_overScroller.isFinished) {
                target?.stopScroll()
            }
            _nestedFlingView.stopScroll()
            _nestedFlingView = null
        } else {
            val scroll = MathUtils.clamp(scrollY + dy, minScroll, maxScroll)
            scrollTo(scrollX, scroll)
        }
    }

    /**over归位*/
    fun resetOverScroll() {
        if (enableTopOverScroll && !isTouchHold) {
            if (scrollY > maxScroll) {
                startScrollTo(0, 0)
                //L.e("恢复位置.")
            }
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)

        if (target == childScrollView || scrollY > maxScroll) {
            resetOverScroll()
        }
    }

    //</editor-fold desc="其他滚动处理">

    //<editor-fold desc="非内嵌滚动处理">

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val absX = abs(velocityX)
        val absY = abs(velocityY)
        if (_nestedScrollView == null &&
            _linkageFlingScrollView == null &&
            absY > absX && absY > minFlingVelocity
        ) {
            val delegateScrollView: NestedScrollingChild? = footerScrollView ?: headerScrollView
            delegateScrollView?.apply {
                setFlingView(this)
                val vY = -velocityY.toInt()
                (footerView ?: headerView)?.behavior()?.apply {
                    if (this is LinkageFooterBehavior) {
                        //这一点很重要, 因为是模拟出来的fling操作
                        this._nestedPreFling = true
                        this._nestedFlingDirection = vY
                    }
                }
                fling(0, velocity(vY))
            }
            //L.i("fling $velocityY")
            return true
        }
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    val _scrollConsumed = intArrayOf(0, 0)

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val absX = abs(distanceX)
        val absY = abs(distanceY)

        if (_nestedScrollView == null) {
            stopNestedScroll()
        }

        if (_nestedScrollView == null && absY > absX && absY > touchSlop && e1 != null && e2 != null) {
            //L.i("scroll $distanceY")
            onNestedPreScrollOther(childView, distanceY.toInt(), _scrollConsumed)
            return true
        }
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    //</editor-fold desc="非内嵌滚动处理">

}