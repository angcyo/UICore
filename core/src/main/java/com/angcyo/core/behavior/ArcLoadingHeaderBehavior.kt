package com.angcyo.core.behavior

import android.content.Context
import android.util.AttributeSet
import com.angcyo.behavior.refresh.RefreshBehavior
import com.angcyo.behavior.refresh.RefreshHeaderBehavior
import com.angcyo.core.R
import com.angcyo.widget.base.find
import com.angcyo.widget.base.mH
import com.angcyo.widget.base.parentMeasuredHeight
import com.angcyo.widget.progress.ArcLoadingView
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/07
 */

class ArcLoadingHeaderBehavior(context: Context, attributeSet: AttributeSet? = null) :
    RefreshHeaderBehavior(context, attributeSet) {

    init {
        showLog = false

        _refreshEffectConfig.enableBottomOver = false
    }

    override fun onContentScrollTo(behavior: RefreshBehavior, x: Int, y: Int) {
        super.onContentScrollTo(behavior, x, y)

        if (behavior.refreshStatus == RefreshBehavior.STATUS_NORMAL) {
            childView.find<ArcLoadingView>(R.id.lib_arc_loading_view)?.apply {
                val bHeight = parentMeasuredHeight() - bottom
                if (y >= bHeight) {
                    val progress =
                        (((y - bHeight) * 1f / (childView.mH())) * 100).toInt()
                    this.progress = min(progress, 51)
                }
            }
        }
    }

    override fun onContentStopScroll(behavior: RefreshBehavior, touchHold: Boolean) {
        //L.i("${behavior.refreshStatus} $touchHold ${behavior.scrollY}")
        when (behavior.refreshStatus) {
            RefreshBehavior.STATUS_FINISH -> {
                //刷新完成状态, 手势放开
                behavior.refreshStatus = RefreshBehavior.STATUS_NORMAL
            }
            RefreshBehavior.STATUS_REFRESH -> {
                if (!touchHold) {
                    //刷新状态, 手势放开
                    if (behavior.scrollY >= childView.mH()) {
                        behavior.startScrollTo(0, childView.mH())
                    } else {
                        behavior.startScrollTo(0, 0)
                    }
                }
            }
            RefreshBehavior.STATUS_NORMAL -> {
                if (!touchHold) {
                    //正常状态, 手势放开
                    if (behavior.scrollY >= childView.mH()) {
                        //触发刷新, 切换刷新状态
                        behavior.refreshStatus = RefreshBehavior.STATUS_REFRESH
                    } else {
                        //滚到默认
                        behavior.startScrollTo(0, 0)
                    }
                }
            }
            else -> {
                behavior.refreshStatus = RefreshBehavior.STATUS_NORMAL
            }
        }
    }

    override fun onRefreshStatusChange(
        behavior: RefreshBehavior,
        from: Int,
        to: Int,
        touchHold: Boolean
    ) {
        when (to) {
            RefreshBehavior.STATUS_REFRESH -> {
                if (!touchHold) {
                    behavior.startScrollTo(0, childView.mH())
                }
                behavior.onRefresh(behavior)
            }
            RefreshBehavior.STATUS_FINISH -> {
                //可以提示一些UI, 然后再[Scroll]
                if (!touchHold) {
                    behavior.refreshStatus = RefreshBehavior.STATUS_NORMAL
                }
            }
            else -> {
                if (!touchHold) {
                    behavior.startScrollTo(0, 0)
                }
            }
        }

        //ui
        childView.find<ArcLoadingView>(R.id.lib_arc_loading_view)?.apply {
            if (to == RefreshBehavior.STATUS_REFRESH) {
                startLoading()
            } else {
                endLoading()
                if (to >= RefreshBehavior.STATUS_FINISH) {
                    progress = 50
                }
            }
        }
    }
}