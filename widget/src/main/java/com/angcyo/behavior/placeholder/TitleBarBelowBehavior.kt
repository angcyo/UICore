package com.angcyo.behavior.placeholder

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.BaseDependsBehavior
import com.angcyo.behavior.ITitleBarBehavior
import com.angcyo.library.ex.mH
import com.angcyo.library.ex.offsetTopTo
import com.angcyo.widget.base.behavior

/**
 * 布局在标题栏下的行为, 可以覆盖在内容上, 也可以被内容覆盖
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/01
 */

open class TitleBarBelowBehavior(
    context: Context? = null,
    attrs: AttributeSet? = null
) : BaseDependsBehavior<View>(context, attrs) {

    /**标题栏的行为, 用于布局在标题栏bottom里面*/
    var titleBarPlaceholderBehavior: ITitleBarBehavior? = null

    /**[child]需要排除多少高度*/
    val _excludeHeight get() = titleBarPlaceholderBehavior?.getContentExcludeHeight(this) ?: 0

    var _titleBarView: View? = null

    val _titleBarHeight get() = _titleBarView?.mH(0) ?: 0

    /**是否固定位置, 不跟随[ITitleBarBehavior]的偏移而偏移*/
    var fixed: Boolean = false

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {

        val behavior = dependency.behavior()
        behavior?.let {
            if (it is ITitleBarBehavior) {
                titleBarPlaceholderBehavior = it
                _titleBarView = dependency
            }
        }

        if (titleBarPlaceholderBehavior != null) {
            //去掉布局的layout_anchor属性, 否则会循环触发[onDependentViewChanged]
            val layoutParams = child.layoutParams
            if (layoutParams is CoordinatorLayout.LayoutParams) {
                if (layoutParams.anchorId != View.NO_ID) {
                    layoutParams.anchorId = View.NO_ID
                    child.layoutParams = layoutParams
                }
            }
        }

        super.layoutDependsOn(parent, child, dependency)

        return behavior is ITitleBarBehavior
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        val result = super.onDependentViewChanged(parent, child, dependency)
        return if (fixed) {
            child.offsetTopTo(_titleBarHeight)
            result
        } else {
            val top = dependency.bottom
            child.layout(child.left, top, child.right, top + child.measuredHeight)
            false
        }
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

        if (fixed) {
            parent.onMeasureChild(
                child,
                parentWidthMeasureSpec,
                widthUsed,
                parentHeightMeasureSpec,
                heightUsed + _titleBarHeight
            )
        } else {
            parent.onMeasureChild(
                child,
                parentWidthMeasureSpec,
                widthUsed,
                parentHeightMeasureSpec,
                heightUsed + _excludeHeight
            )
        }
        return true
    }

    override fun onLayoutChildAfter(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        super.onLayoutChildAfter(parent, child, layoutDirection)
        if (fixed) {
            child.offsetTopTo(_titleBarHeight)
        } else {
            child.offsetTopTo(_titleBarView?.bottom ?: 0)
        }
    }
}