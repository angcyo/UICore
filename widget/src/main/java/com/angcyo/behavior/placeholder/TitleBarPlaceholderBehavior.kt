package com.angcyo.behavior.placeholder

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.BaseDependsBehavior

/**
 * 单纯用来标识当前[child]是什么行为
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 */
class TitleBarPlaceholderBehavior(context: Context? = null, attributeSet: AttributeSet? = null) :
    BaseDependsBehavior<View>(context, attributeSet), ITitleBarPlaceholderBehavior {

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        return super.onMeasureChild(
            parent,
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed
        )
    }

    override fun onMeasureAfter(parent: CoordinatorLayout, child: View) {
        super.onMeasureAfter(parent, child)
    }

    override fun getTitleBarHeight(behavior: BaseDependsBehavior<*>): Int {
        return childView.measuredHeight
    }
}

interface ITitleBarPlaceholderBehavior {

    /**获取标题栏高度*/
    fun getTitleBarHeight(behavior: BaseDependsBehavior<*>): Int
}