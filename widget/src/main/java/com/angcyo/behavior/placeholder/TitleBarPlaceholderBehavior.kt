package com.angcyo.behavior.placeholder

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.angcyo.behavior.BaseDependsBehavior

/**
 * 单纯用来标识当前[child]是什么行为
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 */
class TitleBarPlaceholderBehavior(context: Context? = null, attributeSet: AttributeSet? = null) :
    BaseDependsBehavior<View>(context, attributeSet), ITitleBarPlaceholderBehavior {

    override fun getTitleBarHeight(behavior: BaseDependsBehavior<*>): Int {
        return childView.measuredHeight
    }

    override fun getTitleBarBottom(behavior: BaseDependsBehavior<*>): Int {
        return childView.measuredHeight
    }
}

interface ITitleBarPlaceholderBehavior {

    /**获取标题栏高度*/
    fun getTitleBarHeight(behavior: BaseDependsBehavior<*>): Int

    fun getTitleBarBottom(behavior: BaseDependsBehavior<*>): Int
}