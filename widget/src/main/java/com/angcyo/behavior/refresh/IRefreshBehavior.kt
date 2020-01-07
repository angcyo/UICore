package com.angcyo.behavior.refresh

import com.angcyo.widget.base.offsetTopTo

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/07
 */

interface IRefreshBehavior {

    /**当内容滚动时, 界面需要处理的回调*/
    fun onContentScrollTo(behavior: RefreshBehavior, x: Int, y: Int) {
        behavior.childView.offsetTopTo(y + behavior.offsetTop)
    }

    /**当内容over滚动时回调*/
    fun onContentOverScroll(behavior: RefreshBehavior, dx: Int, dy: Int) {
        behavior.scrollBy(0, -dy)
    }

    /**内容停止了滚动, 此时需要恢复界面*/
    fun onContentStopScroll(behavior: RefreshBehavior) {
        if (behavior.scrollY != 0) {
            behavior.startScrollTo(0, 0)
        }
    }

    /**刷新状态改变*/
    fun onRefreshStatusChange(behavior: RefreshBehavior, from: Int, to: Int) {
        if (to == RefreshBehavior.STATUS_REFRESH) {
            behavior.onRefresh(behavior)
        } else if (to == RefreshBehavior.STATUS_NORMAL) {
            behavior.startScrollTo(0, 0)
        }
    }
}