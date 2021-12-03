package com.angcyo.behavior.effect

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.angcyo.behavior.BaseGestureBehavior
import com.angcyo.library.ex.Action
import com.angcyo.library.ex.RSize
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.toRSize
import com.angcyo.tablayout.clamp
import com.angcyo.widget.R
import com.angcyo.widget.base.*
import com.angcyo.widget.layout.isEnableCoordinator

/**
 * 支持在竖直方向内, 任意移动的[Behavior]
 *
 * 通过[addScrollListener]监听滚动, 实现想要的效果.
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class TransitionBehavior(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseGestureBehavior<View>(context, attributeSet) {

    /**首次布局时的偏移量*/
    var defaultTransitionXAttr: RSize = null
    var defaultTransitionYAttr: RSize = null
    var defaultTransitionX: Int = 0
    var defaultTransitionY: Int = 0

    /**设置最小状态的偏移量*/
    var minTransitionXAttr: RSize = null
    var minTransitionYAttr: RSize = null
    var minTransitionX: Int = 0
    var minTransitionY: Int = 0

    /**设置最大状态的偏移量*/
    var maxTransitionXAttr: RSize = null
    var maxTransitionYAttr: RSize = "1ph"
    var maxTransitionX: Int = 0
    var maxTransitionY: Int = 0

    /**宽高消耗的大小*/
    var childWidthUsedAttr: RSize = null
    var childHeightUsedAttr: RSize = null

    /**手势放开之后, 自动根据当前偏移选择滚动到min or max*/
    var autoScrollOnResult: Boolean = true

    /**阈值*/
    var autoScrollThreshold: Float = 0.2f

    /**需要恢复到默认状态*/
    var onResetScroll: Action? = {
        if (autoScrollOnResult) {
            autoResetScroll()
        }
    }

    init {
        val array =
            context.obtainStyledAttributes(attributeSet, R.styleable.TransitionBehavior_Layout)

        defaultTransitionXAttr =
            array.getString(R.styleable.TransitionBehavior_Layout_layout_default_transition_x)
                ?: defaultTransitionXAttr
        defaultTransitionYAttr =
            array.getString(R.styleable.TransitionBehavior_Layout_layout_default_transition_y)
                ?: defaultTransitionYAttr

        minTransitionXAttr =
            array.getString(R.styleable.TransitionBehavior_Layout_layout_min_transition_x)
                ?: minTransitionXAttr
        minTransitionYAttr =
            array.getString(R.styleable.TransitionBehavior_Layout_layout_min_transition_y)
                ?: minTransitionYAttr

        maxTransitionXAttr =
            array.getString(R.styleable.TransitionBehavior_Layout_layout_max_transition_x)
                ?: maxTransitionXAttr
        maxTransitionYAttr =
            array.getString(R.styleable.TransitionBehavior_Layout_layout_max_transition_y)
                ?: maxTransitionYAttr

        childWidthUsedAttr =
            array.getString(R.styleable.TransitionBehavior_Layout_layout_child_width_used)
                ?: childWidthUsedAttr
        childHeightUsedAttr =
            array.getString(R.styleable.TransitionBehavior_Layout_layout_child_height_used)
                ?: childHeightUsedAttr

        autoScrollOnResult =
            array.getBoolean(
                R.styleable.TransitionBehavior_Layout_layout_auto_scroll_on_result,
                autoScrollOnResult
            )
        autoScrollThreshold =
            array.getFloat(
                R.styleable.TransitionBehavior_Layout_layout_auto_scroll_threshold,
                autoScrollThreshold
            )

        array.recycle()
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

        var childWidthUsed: Int = 0
        var childHeightUsed: Int = 0

        childWidthUsedAttr?.apply {
            childWidthUsed = toRSize(
                parent.mW(),
                parent.mH(),
                def = childWidthUsed,
                context = parent.context
            )
        }
        childHeightUsedAttr?.apply {
            childHeightUsed = toRSize(
                parent.mW(),
                parent.mH(),
                def = childHeightUsed,
                context = parent.context
            )
        }

        parent.onMeasureChild(
            child,
            parentWidthMeasureSpec,
            widthUsed + childWidthUsed,
            parentHeightMeasureSpec,
            heightUsed + childHeightUsed
        )

        return true
    }

    override fun onMeasureAfter(parent: CoordinatorLayout, child: View) {
        super.onMeasureAfter(parent, child)
        if (!ViewCompat.isLaidOut(child)) {
            defaultTransitionXAttr?.apply {
                behaviorScrollX = toRSize(
                    parent.mW(),
                    parent.mH(),
                    def = behaviorScrollX,
                    context = parent.context
                )
                defaultTransitionX = behaviorScrollX
            }
            defaultTransitionYAttr?.apply {
                behaviorScrollY = toRSize(
                    parent.mW(),
                    parent.mH(),
                    def = behaviorScrollY,
                    context = parent.context
                )
                defaultTransitionY = behaviorScrollY
            }
        }

        minTransitionXAttr?.apply {
            minTransitionX = toRSize(
                parent.mW(),
                parent.mH(),
                def = minTransitionX,
                context = parent.context
            )
        }
        minTransitionYAttr?.apply {
            minTransitionY = toRSize(
                parent.mW(),
                parent.mH(),
                def = minTransitionY,
                context = parent.context
            )
        }

        maxTransitionXAttr?.apply {
            maxTransitionX = toRSize(
                parent.mW(),
                parent.mH(),
                def = maxTransitionX,
                context = parent.context
            )
        }
        maxTransitionYAttr?.apply {
            maxTransitionY = toRSize(
                parent.mW(),
                parent.mH(),
                def = maxTransitionY,
                context = parent.context
            )
        }

        minConsumedScrollX = minTransitionX
        minConsumedScrollY = minTransitionY

        maxConsumedScrollX = maxTransitionX
        maxConsumedScrollY = maxTransitionY
    }

    /**限制滚动范围*/
    override fun scrollTo(x: Int, y: Int, scrollType: Int) {
        super.scrollTo(
            clamp(x, minTransitionX, maxTransitionX),
            clamp(y, minTransitionY, maxTransitionY),
            scrollType
        )
    }

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
        return coordinatorLayout.isEnableCoordinator
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

        if (dx != 0) {
            //内容产生过偏移, 那么此次的内嵌滚动肯定是需要消耗的
            if (dx > 0) {
                consumedScrollHorizontal(dx, consumed)
            } else {
                //手指向左
                if (!target.topCanScroll()) {
                    consumedScrollHorizontal(dx, consumed)
                }
            }
        }
        if (dy != 0) {
            //内容产生过偏移, 那么此次的内嵌滚动肯定是需要消耗的
            if (dy > 0) {
                //手指向上滑动
                /*if (behaviorScrollY - dy >= minTransitionY) {
                    //有空间滚动
                    consumedScrollVertical(dy, consumed)
                }*/
                consumedScrollVertical(dy, consumed)
            } else {
                //手指向下滑动
                if (!target.topCanScroll()) {  //&& behaviorScrollY - dy <= maxTransitionY
                    consumedScrollVertical(dy, consumed)
                }
            }
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

        if ((dxUnconsumed != 0 || dyUnconsumed != 0) && type.isTouch()) {
            //内嵌滚动视图已经不需要消耗滚动值了, 通常是到达了首尾两端
            scrollBy(-dxUnconsumed, -dyUnconsumed, SCROLL_TYPE_NESTED)
        }
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (_nestedScrollView == null && (distanceX != 0f || distanceY != 0f)) {
            scrollBy(-distanceX.toInt(), -distanceY.toInt(), SCROLL_TYPE_GESTURE)
            return true
        }
        return false
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        if (behaviorScrollY != minTransitionY && behaviorScrollY != maxTransitionY) {
            _resetScroll()
        }
        if (behaviorScrollX != minTransitionX && behaviorScrollX != maxTransitionX) {
            _resetScroll()
        }
        if (!isTouchHold && type.isTouch() && !isFlingAccepted) {
            //
        }
    }

    override fun onTouchFinish(parent: CoordinatorLayout, child: View, ev: MotionEvent) {
        super.onTouchFinish(parent, child, ev)
        if (!isTouchHold && !isFlingAccepted && ViewCompat.isLaidOut(child)) {
            //在非nested scroll 视图上滚动过
            if (ev.isScreenTouchIn(child)) {
                _resetScroll()
            }
        }
    }

    /**重置滚动状态*/
    fun _resetScroll() {
        onResetScroll?.invoke()
    }

    //是否不要回到default偏移的位置
    var _notDefaultFlag: Boolean = false

    /**根据偏移量, 自动滚动到最优的位置*/
    fun autoResetScroll() {
        var toX = 0
        var toY = 0

        //阈值
        val threshold = autoScrollThreshold

        if (behaviorScrollX != 0) {
            if (isFlingAccepted) {
                _notDefaultFlag = true
                toX = if (nestedFlingVelocityX < 0) {
                    maxTransitionX
                } else {
                    minTransitionX
                }
            } else if (defaultTransitionX == 0 || _notDefaultFlag) {
                //默认无偏移
                val ratio = behaviorScrollX.abs() * 1f / maxTransitionX
                toX = if (ratio >= threshold) {
                    if (_lastScrollDx > 0) {
                        minTransitionX
                    } else {
                        maxTransitionX
                    }
                } else {
                    minTransitionX
                }
            } else {
                //有偏移
                val dx = behaviorScrollX - defaultTransitionX
                val ratio = dx.abs() * 1f / (maxTransitionX - defaultTransitionX)
                toX = if (dx > 0) {
                    if (ratio >= threshold) {
                        _notDefaultFlag = true
                        maxTransitionX
                    } else {
                        defaultTransitionX
                    }
                } else {
                    if (ratio >= threshold) {
                        _notDefaultFlag = true
                        minTransitionX
                    } else {
                        defaultTransitionX
                    }
                }
            }
        }
        if (behaviorScrollY != 0) {
            if (isFlingAccepted) {
                _notDefaultFlag = true
                toY = if (nestedFlingVelocityY < 0) {
                    maxTransitionY
                } else {
                    minTransitionY
                }
            } else if (defaultTransitionY == 0 || _notDefaultFlag) {
                //默认无偏移
                val ratio = behaviorScrollY.abs() * 1f / maxTransitionY
                toY = if (ratio >= threshold) {
                    if (_lastScrollDy > 0) {
                        minTransitionY
                    } else {
                        maxTransitionY
                    }
                } else {
                    minTransitionY
                }
            } else {
                //有偏移
                val dx = behaviorScrollY - defaultTransitionY
                val ratio = dx.abs() * 1f / (maxTransitionY - defaultTransitionY)
                toY = if (dx > 0) {
                    if (ratio >= threshold) {
                        _notDefaultFlag = true
                        maxTransitionY
                    } else {
                        defaultTransitionY
                    }
                } else {
                    if (ratio >= threshold) {
                        _notDefaultFlag = true
                        minTransitionY
                    } else {
                        defaultTransitionY
                    }
                }
            }
        }
        resetScrollTo(toX, toY)
    }

    /**重置滚动到指定位置*/
    fun resetScrollTo(x: Int = 0, y: Int = 0) {
        startScrollTo(x, y)
    }
}