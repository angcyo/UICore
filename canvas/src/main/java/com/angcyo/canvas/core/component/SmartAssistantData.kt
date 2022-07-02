package com.angcyo.canvas.core.component

import com.angcyo.canvas.items.renderer.IItemRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/25
 */
data class AssistantData(
    /**原始值, 未改变之前的值*/
    val originValue: Float,
    /**请求值, 改变之后的值*/
    val requestValue: Float,
    /**查找到的最优值, 如果有*/
    var resultValue: Float? = null,
    /**参考比较的值,通常情况下会会等于[resultValue], 用来临时存储*/
    var refValue: Float? = null,
    /**参考的[IItemRenderer], 如果有*/
    var refRenderer: IItemRenderer<*>? = null
)

fun AssistantData.isChanged(): Boolean {
    if (resultValue == null) {
        return false
    }
    return requestValue != resultValue
}

fun AssistantData.isChanged(last: AssistantData? = null): Boolean {
    if (last == null) {
        return true
    }

    return if (last.resultValue == null) {
        resultValue != requestValue
    } else {
        resultValue != last.resultValue
    }

}