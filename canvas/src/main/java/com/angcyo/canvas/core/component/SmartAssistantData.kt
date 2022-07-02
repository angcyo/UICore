package com.angcyo.canvas.core.component

import android.graphics.RectF

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/25
 */
data class SmartAssistantData(

    /**从那个值, 计算出来的推荐值*/
    val fromValue: Float = -1f,

    /**推荐值*/
    var smartValue: SmartAssistantValueData,

    /**[forward]正向查找or负向查找
     * [smartValue] 是正向查找出来的值, 还是负向
     * */
    var forward: Boolean = true,

    /**绘制提示的矩形*/
    var drawRect: RectF? = null,

    )

/*
fun SmartAssistantData.isChanged(): Boolean {
    if (resultValue == null) {
        return false
    }
    return requestValue != resultValue
}

fun SmartAssistantData.isChanged(last: SmartAssistantData? = null): Boolean {
    if (last == null) {
        return true
    }

    return if (last.resultValue == null) {
        resultValue != requestValue
    } else {
        resultValue != last.resultValue
    }
}*/
