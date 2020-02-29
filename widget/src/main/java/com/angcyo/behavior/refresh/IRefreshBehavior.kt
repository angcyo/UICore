package com.angcyo.behavior.refresh

import com.angcyo.widget.base.mH
import com.angcyo.widget.base.offsetTopTo

/**
 * 刷新行为[RefreshBehavior]的UI效果处理类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/07
 */

interface IRefreshBehavior {

    /**当内容滚动时, 界面需要处理的回调*/
    fun onContentScrollTo(behavior: RefreshBehavior, x: Int, y: Int) {
        behavior.childView?.offsetTopTo(y + behavior.offsetTop)
    }

    /**当内容over滚动时回调*/
    fun onContentOverScroll(behavior: RefreshBehavior, dx: Int, dy: Int) {
        behavior.scrollBy(0, -dy)
    }

    /**内容停止了滚动, 此时需要恢复界面*/
    fun onContentStopScroll(behavior: RefreshBehavior, touchHold: Boolean) {
        if (behavior.refreshStatus != RefreshBehavior.STATUS_REFRESH
            && !touchHold
        ) {
            behavior.startScrollTo(0, 0)
        }
    }

    /**刷新状态改变,[touchHold]还处于[touch]状态*/
    fun onRefreshStatusChange(behavior: RefreshBehavior, from: Int, to: Int, touchHold: Boolean) {
        when (to) {
            RefreshBehavior.STATUS_REFRESH -> {
                if (!touchHold) {
                    behavior.startScrollTo(0, behavior.childView.mH() / 2)
                }
                behavior.onRefresh(behavior)
            }
            else -> {
                if (!touchHold) {
                    behavior.startScrollTo(0, 0)
                }
            }
        }
    }
}