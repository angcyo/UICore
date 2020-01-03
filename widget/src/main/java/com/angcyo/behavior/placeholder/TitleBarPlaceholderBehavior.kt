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

    override fun getContentExcludeHeight(behavior: BaseDependsBehavior<*>): Int {
        return childView.measuredHeight
    }

    override fun getContentOffsetTop(behavior: BaseDependsBehavior<*>): Int {
        return childView.measuredHeight
    }
}

interface ITitleBarPlaceholderBehavior {

    /**获取内容布局需要排除的高度*/
    fun getContentExcludeHeight(behavior: BaseDependsBehavior<*>): Int

    /**获取内容布局开始布局的位置*/
    fun getContentOffsetTop(behavior: BaseDependsBehavior<*>): Int
}