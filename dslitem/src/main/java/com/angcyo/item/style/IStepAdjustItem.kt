package com.angcyo.item.style

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.annotation.UpdateByDiff
import com.angcyo.dsladapter.annotation.UpdateByNotify
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.clampValue
import com.angcyo.library.ex.isTouchFinish
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.LongTouchListener
import com.angcyo.widget.base.string

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/01
 */
interface IStepAdjustItem : IAutoInitItem {

    /**统一样式配置*/
    var stepAdjustItemConfig: StepAdjustItemConfig

    @ItemInitEntryPoint
    fun initStepAdjustItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        val itemStepAdjustValueView = itemHolder.tv(stepAdjustItemConfig.itemStepAdjustValueViewId)
        itemStepAdjustValueView?.text = stepAdjustItemConfig.itemStepAdjustValue?.toString()

        //减
        itemHolder.longTouch(
            stepAdjustItemConfig.itemStepAdjustDecreaseViewId,
            true
        ) { view, event, eventType, longPressHappened ->
            if (eventType == null && event.isTouchFinish()) {
                //改变item
                if (!stepAdjustItemConfig.itemRealTimeUpdate) {
                    adapterItem.itemChanged = true
                }
            } else if (eventType != null) {
                val stepAdjustStep = if (eventType == LongTouchListener.EVENT_TYPE_LONG_PRESS) {
                    stepAdjustItemConfig.itemStepAdjustLongStep
                } else {
                    stepAdjustItemConfig.itemStepAdjustStep
                }
                if (stepAdjustItemConfig.isLongValueType) {
                    val value = itemStepAdjustValueView?.string()?.toLongOrNull() ?: 0
                    val step = stepAdjustStep.toString().toLongOrNull() ?: 1
                    if (stepAdjustItemConfig.itemStepAdjustAction(value, -step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            value - step,
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                } else if (stepAdjustItemConfig.isIntValueType) {
                    val value = itemStepAdjustValueView?.string()?.toIntOrNull() ?: 0
                    val step = stepAdjustStep.toString().toIntOrNull() ?: 1
                    if (stepAdjustItemConfig.itemStepAdjustAction(value, -step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            value - step,
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                } else {
                    val value = itemStepAdjustValueView?.string()?.toFloatOrNull() ?: 0f
                    val step = stepAdjustStep.toString().toFloatOrNull() ?: 1f
                    if (stepAdjustItemConfig.itemStepAdjustAction(value, -step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            value - step,
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                }
            }
            true
        }

        //加
        itemHolder.longTouch(
            stepAdjustItemConfig.itemStepAdjustIncreaseViewId,
            true
        ) { view, event, eventType, longPressHappened ->
            if (eventType == null && event.isTouchFinish()) {
                //改变item
                adapterItem.itemChanged = true
            } else if (eventType != null) {
                val stepAdjustStep = if (eventType == LongTouchListener.EVENT_TYPE_LONG_PRESS) {
                    stepAdjustItemConfig.itemStepAdjustLongStep
                } else {
                    stepAdjustItemConfig.itemStepAdjustStep
                }
                if (stepAdjustItemConfig.isLongValueType) {
                    val value = itemStepAdjustValueView?.string()?.toLongOrNull() ?: 0
                    val step = stepAdjustStep.toString().toLongOrNull() ?: 1
                    if (stepAdjustItemConfig.itemStepAdjustAction(value, +step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            value + step,
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                } else if (stepAdjustItemConfig.isIntValueType) {
                    val value = itemStepAdjustValueView?.string()?.toIntOrNull() ?: 0
                    val step = stepAdjustStep.toString().toIntOrNull() ?: 1
                    if (stepAdjustItemConfig.itemStepAdjustAction(value, +step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            value + step,
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                } else {
                    val value = itemStepAdjustValueView?.string()?.toFloatOrNull() ?: 0f
                    val step = stepAdjustStep.toString().toFloatOrNull() ?: 1f
                    if (stepAdjustItemConfig.itemStepAdjustAction(value, +step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            value + step,
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                }
            }
            true
        }
    }

    /**更新值, 并更新界面*/
    @UpdateByDiff
    @UpdateByNotify
    @CallPoint
    fun updateStepAdjustValue(
        itemHolder: DslViewHolder,
        adapterItem: DslAdapterItem,
        value: Any?,
        notifyItemChanged: Boolean = true
    ) {
        val itemStepAdjustValueView = itemHolder.tv(stepAdjustItemConfig.itemStepAdjustValueViewId)
        updateStepAdjustValue(value)
        itemStepAdjustValueView?.text = stepAdjustItemConfig.itemStepAdjustValue?.toString()

        if (notifyItemChanged) {
            adapterItem.itemChanged = true //diff 刷新界面
        }
    }

    /**仅更新值*/
    fun updateStepAdjustValue(value: Any?) {
        //值改变之前的回调
        val newValue =
            clampAdjustValue(stepAdjustItemConfig.itemAdjustChangedBeforeAction(value))
        stepAdjustItemConfig.itemStepAdjustValue = newValue

        //改变值之后的回调
        stepAdjustItemConfig.itemAdjustChangedAfterAction(newValue)
    }

    /**限制最小值/最大值*/
    fun clampAdjustValue(value: Any?): Any? {
        value ?: return null
        val minValue = stepAdjustItemConfig.itemAdjustMinValue
        val maxValue = stepAdjustItemConfig.itemAdjustMaxValue

        return clampValue(value, stepAdjustItemConfig.itemStepAdjustStep, minValue, maxValue)
    }
}

/**当前的值*/
var IStepAdjustItem.itemStepAdjustValue: Any?
    get() = stepAdjustItemConfig.itemStepAdjustValue
    set(value) {
        stepAdjustItemConfig.itemStepAdjustValue = clampAdjustValue(value)
    }

/**限制最小值*/
var IStepAdjustItem.itemStepAdjustMinValue: Any?
    get() = stepAdjustItemConfig.itemAdjustMinValue
    set(value) {
        stepAdjustItemConfig.itemAdjustMinValue = value
    }

/**限制最大值*/
var IStepAdjustItem.itemStepAdjustMaxValue: Any?
    get() = stepAdjustItemConfig.itemAdjustMaxValue
    set(value) {
        stepAdjustItemConfig.itemAdjustMaxValue = value
    }

/**步长*/
var IStepAdjustItem.itemStepAdjustStep: Any
    get() = stepAdjustItemConfig.itemStepAdjustStep
    set(value) {
        stepAdjustItemConfig.itemStepAdjustStep = value
    }

/**长按步长*/
var IStepAdjustItem.itemStepAdjustLongStep: Any
    get() = stepAdjustItemConfig.itemStepAdjustLongStep
    set(value) {
        stepAdjustItemConfig.itemStepAdjustLongStep = value
    }

/**值改变后的回调*/
var IStepAdjustItem.itemAdjustChangedAfterAction: (value: Any?) -> Unit
    get() = stepAdjustItemConfig.itemAdjustChangedAfterAction
    set(value) {
        stepAdjustItemConfig.itemAdjustChangedAfterAction = value
    }

class StepAdjustItemConfig : IDslItemConfig {

    /**关键控件id: 减*/
    var itemStepAdjustDecreaseViewId: Int = R.id.lib_step_adjust_decrease_view

    /**关键控件id: 加*/
    var itemStepAdjustIncreaseViewId: Int = R.id.lib_step_adjust_increase_view

    /**关键控件id: 值*/
    var itemStepAdjustValueViewId: Int = R.id.lib_step_adjust_value_view

    /**当前的值*/
    var itemStepAdjustValue: Any? = 0

    /**当点击/长按+-按钮时, 是否要实时更新item*/
    var itemRealTimeUpdate: Boolean = false

    /**限制最小值*/
    var itemAdjustMinValue: Any? = null

    /**限制最大值*/
    var itemAdjustMaxValue: Any? = null

    /**每次调整的步长
     * 自动识别[Int] [Float]类型*/
    var itemStepAdjustStep: Any = 1L

    /**长按时的步长*/
    var itemStepAdjustLongStep: Any = 10L

    val isLongValueType: Boolean
        get() = itemStepAdjustStep is Long

    val isIntValueType: Boolean
        get() = itemStepAdjustStep is Int

    /**点击调整按钮拦截回调, 不拦截则默认处理*/
    var itemStepAdjustAction: (value: Any?, step: Any) -> Boolean = { value, step ->
        false
    }

    /**改变后的回调, 返回值会被显示在界面上*/
    var itemAdjustChangedAfterAction: (value: Any?) -> Unit = {

    }

    /**值改变之后的回调*/
    var itemAdjustChangedBeforeAction: (value: Any?) -> Any? = {
        it
    }
}