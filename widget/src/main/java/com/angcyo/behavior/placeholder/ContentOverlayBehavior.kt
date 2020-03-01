package com.angcyo.behavior.placeholder

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.BaseDependsBehavior
import com.angcyo.behavior.ITitleBarBehavior
import com.angcyo.widget.base.behavior

/**
 * 覆盖在内容上, 布局在标题栏下的行为
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/01
 */

open class ContentOverlayBehavior(
    context: Context? = null,
    attrs: AttributeSet? = null
) : BaseDependsBehavior<View>(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        super.layoutDependsOn(parent, child, dependency)
        return dependency.behavior() is ITitleBarBehavior
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        super.onDependentViewChanged(parent, child, dependency)
        val top = dependency.bottom
        child.layout(child.left, top, child.right, top + child.measuredHeight)
        return false
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        return super.onLayoutChild(parent, child, layoutDirection)
    }
}