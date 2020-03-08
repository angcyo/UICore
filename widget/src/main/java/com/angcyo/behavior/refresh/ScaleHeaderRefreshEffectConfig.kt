package com.angcyo.behavior.refresh

import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.library.L
import com.angcyo.widget.base.setHeight

/**
 * 当[Content]over滚动的时候, 缩放指定的[View].
 *
 * 不要太频繁调用requestLayout
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/08
 */

open class ScaleHeaderRefreshEffectConfig : RefreshEffectConfig() {

    /**目标view, 在content ViewGroup中的index*/
    var targetViewIndexInContent: Int = 0

    /**获取目标View*/
    var onGetTargetView: (behavior: RefreshBehavior) -> View? = { behavior ->
        if (behavior.childView is ViewGroup) {
            (behavior.childView as ViewGroup).getChildAt(targetViewIndexInContent)
        } else {
            null
        }
    }

    var _defaultLayoutParams: ViewGroup.LayoutParams? = null
    var _defaultTargetHeight: Int = -1
    override fun onContentLayout(
        behavior: RefreshBehavior,
        parent: CoordinatorLayout,
        child: View
    ) {
        super.onContentLayout(behavior, parent, child)
        onGetTargetView(behavior)?.apply {
            if (behavior.scrollY == 0 && _defaultLayoutParams == null) {
                _defaultLayoutParams = layoutParams
                _defaultTargetHeight = measuredHeight
            }
        }
    }

    override fun onContentScrollTo(behavior: RefreshBehavior, x: Int, y: Int) {
        if (y > 0) {
            //L.i("$_defaultTargetHeight $y")
            //当内容需要向下滚动时, 改变目标view的高度
            onGetTargetView(behavior)?.apply {
                setHeight(_defaultTargetHeight + y)
            }
        } /*else if (y == 0) {
            onGetTargetView(behavior)?.apply {
                _defaultLayoutParams?.also {
                    layoutParams = it
                }
                _defaultLayoutParams = null
            }
        } */ else {
            super.onContentScrollTo(behavior, x, y)
        }
    }
}