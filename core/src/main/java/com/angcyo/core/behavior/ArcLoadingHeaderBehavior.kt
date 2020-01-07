package com.angcyo.core.behavior

import android.content.Context
import android.util.AttributeSet
import com.angcyo.behavior.refresh.RefreshBehavior
import com.angcyo.behavior.refresh.RefreshHeaderBehavior
import com.angcyo.core.R
import com.angcyo.widget.base.find
import com.angcyo.widget.base.parentMeasuredHeight
import com.angcyo.widget.layout.RCoordinatorLayout
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

        refreshEffectConfig.enableBottomOver = false
    }

    override fun onContentScrollTo(behavior: RefreshBehavior, x: Int, y: Int) {
        super.onContentScrollTo(behavior, x, y)

        if (behavior.refreshStatus == RefreshBehavior.STATUS_NORMAL) {
            childView.find<ArcLoadingView>(R.id.lib_arc_loading_view)?.apply {
                val bHeight = parentMeasuredHeight() - bottom
                if (y >= bHeight) {
                    val progress =
                        (((y - bHeight) * 1f / (childView.measuredHeight)) * 100).toInt()
                    this.progress = min(progress, 51)
                }
            }
        }
    }

    override fun onContentStopScroll(behavior: RefreshBehavior) {
        if (behavior.refreshStatus == RefreshBehavior.STATUS_FINISH) {
            //刷新已完成, 但是touch还没放手
            behavior.refreshStatus = RefreshBehavior.STATUS_NORMAL
        } else if (behavior.scrollY > 0) {
            if (behavior.scrollY >= childView.measuredHeight) {
                //触发刷新
                behavior.refreshStatus = RefreshBehavior.STATUS_REFRESH
                behavior.startScrollTo(0, childView.measuredHeight)
            } else {
                super.onContentStopScroll(behavior)
            }
        } else {
            super.onContentStopScroll(behavior)
        }
    }

    override fun onRefreshStatusChange(behavior: RefreshBehavior, from: Int, to: Int) {
        if (to == RefreshBehavior.STATUS_FINISH && (parentLayout as? RCoordinatorLayout)?._isTouch != true) {
            //状态已完成, 并且手指不在滑动
            behavior.startScrollTo(0, 0)
        } else {
            super.onRefreshStatusChange(behavior, from, to)
        }
        childView.find<ArcLoadingView>(R.id.lib_arc_loading_view)?.apply {
            if (to == RefreshBehavior.STATUS_REFRESH) {
                startLoading()
            } else {
                endLoading()
            }
        }
    }
}