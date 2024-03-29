package com.angcyo.behavior.refresh

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.library.ex.*

/**
 * 刷新行为[RefreshContentBehavior]的UI效果处理类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/07
 */

interface IRefreshBehavior {

    companion object {
        //正常状态
        const val STATUS_NORMAL = 0

        //刷新状态
        const val STATUS_REFRESH = 1

        //刷新完成
        const val STATUS_FINISH = 10
    }

    /**刷新状态*/
    var _refreshBehaviorStatus: Int

    /**顶部over效果*/
    var enableTopOver: Boolean

    /**底部over效果*/
    var enableBottomOver: Boolean

    /**需要滚动的内容view, 可以为null*/
    var contentScrollView: View?

    /**请调用此方法设置[_refreshBehaviorStatus]状态*/
    fun onSetRefreshBehaviorStatus(contentBehavior: BaseScrollBehavior<*>, newStatus: Int) {
        val old = _refreshBehaviorStatus
        if (old != newStatus) {
            _refreshBehaviorStatus = newStatus
            onRefreshStatusChange(contentBehavior, old, newStatus)
        }
    }

    /**当内容布局后, 用于保存一些需要初始化的变量*/
    fun onContentLayout(
        contentBehavior: BaseScrollBehavior<*>,
        parent: CoordinatorLayout,
        child: View
    ) {

    }

    /**滚动内容, 默认布局的头部距离*/
    fun onGetContentScrollLayoutTop(contentBehavior: BaseScrollBehavior<*>): Int {
        return 0
    }

    /**当内容滚动时, 界面需要处理的回调*/
    fun onContentScrollTo(contentBehavior: BaseScrollBehavior<*>, x: Int, y: Int, scrollType: Int) {
        /*val min = if (enableBottomOver) Int.MIN_VALUE else 0
        val max = if (enableTopOver || this is RefreshHeaderBehavior) Int.MAX_VALUE else 0*/

        val childView = contentBehavior.childView
        val targetView = contentScrollView ?: contentBehavior.childView

        //显示最大高度
        val maxHeight = (childView?.measuredHeight ?: 0) * 3 / 4

        val min = if (enableBottomOver) -maxHeight else 0
        val max = if (enableTopOver || this is RefreshHeaderBehavior) maxHeight else 0

        val top = clamp(y, min, max)

        if (childView == targetView) {
            childView?.offsetTopTo(
                top + contentBehavior.behaviorOffsetTop +
                        onGetContentScrollLayoutTop(contentBehavior)
            )
        } else {
            if (y > 0 && !enableTopOver) {
                //未激活top over时,  向下滚动需要触发刷新布局, 所以只能移动child
                childView?.offsetTopTo(top + contentBehavior.behaviorOffsetTop)
            } else {
                //child 偏移到默认的位置
                childView?.offsetTopTo(contentBehavior.behaviorOffsetTop)
                //滚动时, 目标view的偏移
                targetView?.offsetTopTo(top + onGetContentScrollLayoutTop(contentBehavior))
            }
        }
    }

    /**当内容over滚动时回调, 同样会触发[onContentScrollTo]*/
    fun onContentOverScroll(
        contentBehavior: BaseScrollBehavior<*>,
        dx: Int,
        dy: Int,
        scrollType: Int
    ) {
        if (dy > 0) {
            if (!enableBottomOver) {
                return
            }
        } else {
            if (!enableTopOver) {
                return
            }
        }
        contentBehavior.scrollBy(0, -dy, scrollType)
    }

    /**内容停止了滚动, 此时需要恢复界面*/
    fun onContentStopScroll(contentBehavior: BaseScrollBehavior<*>) {
        if (_refreshBehaviorStatus != STATUS_REFRESH && !contentBehavior.isTouchHold) {

            val resetScrollX = 0
            val resetScrollY = if (contentBehavior is IRefreshContentBehavior) {
                contentBehavior.getRefreshResetScrollY()
            } else {
                0
            }

            contentBehavior.startScrollTo(resetScrollX, resetScrollY)
        }
    }

    /**刷新状态改变,[touchHold]还处于[touch]状态*/
    fun onRefreshStatusChange(contentBehavior: BaseScrollBehavior<*>, from: Int, to: Int) {
        if (!contentBehavior.isTouchHold) {
            val resetScrollX = 0
            val resetScrollY = if (contentBehavior is IRefreshContentBehavior) {
                contentBehavior.getRefreshResetScrollY()
            } else {
                0
            }

            contentBehavior.startScrollTo(resetScrollX, resetScrollY)
        }
    }

    /**需要触发刷新回调*/
    fun onRefreshAction(contentBehavior: BaseScrollBehavior<*>) {
        if (contentBehavior is IRefreshContentBehavior) {
            contentBehavior.refreshAction(contentBehavior)
        }
    }
}