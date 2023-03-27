package com.angcyo.canvas.render.core.component

import com.angcyo.canvas.render.data.SmartAssistantReferenceValue

/**
 * 智能推荐, 参考值提供器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/27
 */
interface ISmartAssistantValueProvider {

    /**获取平移操作时的推荐值*/
    fun getTranslateXRefValue(): List<SmartAssistantReferenceValue>

    fun getTranslateYRefValue(): List<SmartAssistantReferenceValue>

    /**获取旋转操作时的推荐值*/
    fun getRotateRefValue(): List<SmartAssistantReferenceValue>

}