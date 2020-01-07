package com.angcyo.behavior.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.IContentBehavior
import com.angcyo.widget.base.behavior

/**
 * 刷新头部行为, 仅控制布局的位置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/07
 */

open class RefreshHeaderBehavior(context: Context, attributeSet: AttributeSet? = null) :
    BaseScrollBehavior<View>(context, attributeSet), IRefreshBehavior {

    //阻尼效果
    var refreshEffectConfig = RefreshEffectConfig()

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        super.layoutDependsOn(parent, child, dependency)
        return enableDependsOn && dependency.behavior() is IContentBehavior
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        offsetTop = dependency.top - child.measuredHeight
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onContentOverScroll(behavior: RefreshBehavior, dx: Int, dy: Int) {
        refreshEffectConfig.onContentOverScroll(behavior, dx, dy)
    }
}