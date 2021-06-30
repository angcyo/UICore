package com.angcyo.core.behavior

import android.content.Context
import android.util.AttributeSet
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.refresh.IRefreshBehavior.Companion.STATUS_FINISH
import com.angcyo.behavior.refresh.IRefreshBehavior.Companion.STATUS_NORMAL
import com.angcyo.behavior.refresh.IRefreshBehavior.Companion.STATUS_REFRESH
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

        //_refreshEffectConfig.enableBottomOver = false
    }

    override fun onContentScrollTo(
        contentBehavior: BaseScrollBehavior<*>,
        x: Int,
        y: Int,
        scrollType: Int
    ) {
        super.onContentScrollTo(contentBehavior, x, y, scrollType)

        if (_refreshBehaviorStatus == STATUS_NORMAL) {
            childView.find<ArcLoadingView>(R.id.lib_arc_loading_view)?.apply {
                val bHeight = parentMeasuredHeight() - bottom
                if (y >= bHeight) {
                    val progress = (((y - bHeight) * 1f / (childView.mH())) * 100).toInt()
                    this.progress = min(progress, 51)
                }
            }
        }
    }

    override fun onRefreshStatusChange(contentBehavior: BaseScrollBehavior<*>, from: Int, to: Int) {
        super.onRefreshStatusChange(contentBehavior, from, to)

        //ui
        childView.find<ArcLoadingView>(R.id.lib_arc_loading_view)?.apply {
            if (to == STATUS_REFRESH) {
                startLoading()
            } else {
                endLoading()
                if (to >= STATUS_FINISH) {
                    progress = 50
                }
            }
        }
    }
}