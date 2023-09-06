package com.angcyo.item.style

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.annotation.UpdateByDiff
import com.angcyo.dsladapter.annotation.UpdateByNotify
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.isTouchFinish
import com.angcyo.library.ex.toStr
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
        itemStepAdjustValueView?.text = stepAdjustItemConfig.itemStepAdjustValue

        //减
        itemHolder.longTouch(
            stepAdjustItemConfig.itemStepAdjustDecreaseViewId,
            true
        ) { view, event, eventType ->
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
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), -step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            (value - step).toStr(),
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                } else if (stepAdjustItemConfig.isIntValueType) {
                    val value = itemStepAdjustValueView?.string()?.toIntOrNull() ?: 0
                    val step = stepAdjustStep.toString().toIntOrNull() ?: 1
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), -step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            (value - step).toStr(),
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                } else {
                    val value = itemStepAdjustValueView?.string()?.toFloatOrNull() ?: 0f
                    val step = stepAdjustStep.toString().toFloatOrNull() ?: 1f
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), -step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            (value - step).toStr(),
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
        ) { view, event, eventType ->
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
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), +step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            (value + step).toStr(),
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                } else if (stepAdjustItemConfig.isIntValueType) {
                    val value = itemStepAdjustValueView?.string()?.toIntOrNull() ?: 0
                    val step = stepAdjustStep.toString().toIntOrNull() ?: 1
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), +step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            (value + step).toStr(),
                            stepAdjustItemConfig.itemRealTimeUpdate
                        )
                    }
                } else {
                    val value = itemStepAdjustValueView?.string()?.toFloatOrNull() ?: 0f
                    val step = stepAdjustStep.toString().toFloatOrNull() ?: 1f
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), +step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(
                            itemHolder,
                            adapterItem,
                            (value + step).toStr(),
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
        value: CharSequence?,
        notifyItemChanged: Boolean = true
    ) {
        val itemStepAdjustValueView = itemHolder.tv(stepAdjustItemConfig.itemStepAdjustValueViewId)
        updateStepAdjustValue(value)
        itemStepAdjustValueView?.text = stepAdjustItemConfig.itemStepAdjustValue

        if (notifyItemChanged) {
            adapterItem.itemChanged = true //diff 刷新界面
        }
    }

    /**仅更新值*/
    fun updateStepAdjustValue(value: CharSequence?) {
        val newValue =
            clampAdjustValue(stepAdjustItemConfig.itemStepAdjustChangedAction(value?.toStr()))
        stepAdjustItemConfig.itemStepAdjustValue = newValue
    }

    /**限制最小值/最大值*/
    fun clampAdjustValue(value: CharSequence?): CharSequence? {
        value ?: return null
        val minValue = stepAdjustItemConfig.itemAdjustMinValue
        val maxValue = stepAdjustItemConfig.itemAdjustMaxValue

        if (stepAdjustItemConfig.isLongValueType) {
            val v = value.toString().toLongOrNull() ?: 0
            if (minValue != null) {
                val min = minValue.toString().toLongOrNull() ?: 0
                if (v < min) {
                    return min.toStr()
                }
            }
            if (maxValue != null) {
                val max = maxValue.toString().toLongOrNull() ?: 0
                if (v > max) {
                    return max.toStr()
                }
            }
            return value
        } else if (stepAdjustItemConfig.isIntValueType) {
            val v = value.toString().toIntOrNull() ?: 0
            if (minValue != null) {
                val min = minValue.toString().toIntOrNull() ?: 0
                if (v < min) {
                    return min.toStr()
                }
            }
            if (maxValue != null) {
                val max = maxValue.toString().toIntOrNull() ?: 0
                if (v > max) {
                    return max.toStr()
                }
            }
            return value
        } else {
            val v = value.toString().toFloatOrNull() ?: 0f
            if (minValue != null) {
                val min = minValue.toString().toFloatOrNull() ?: 0f
                if (v < min) {
                    return min.toStr()
                }
            }
            if (maxValue != null) {
                val max = maxValue.toString().toFloatOrNull() ?: 0f
                if (v > max) {
                    return max.toStr()
                }
            }
            return value
        }
    }

}

/**当前的值*/
var IStepAdjustItem.itemStepAdjustValue: CharSequence?
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

class StepAdjustItemConfig : IDslItemConfig {

    /**关键控件id: 减*/
    var itemStepAdjustDecreaseViewId: Int = R.id.lib_step_adjust_decrease_view

    /**关键控件id: 加*/
    var itemStepAdjustIncreaseViewId: Int = R.id.lib_step_adjust_increase_view

    /**关键控件id: 值*/
    var itemStepAdjustValueViewId: Int = R.id.lib_step_adjust_value_view

    /**当前的值*/
    var itemStepAdjustValue: CharSequence? = "0"

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

    /**调整拦截回调, 不拦截则默认处理*/
    var itemStepAdjustAction: (value: CharSequence?, step: Any) -> Boolean = { value, step ->
        false
    }

    /**改变后的回调, 返回值会被显示在界面上*/
    var itemStepAdjustChangedAction: (value: CharSequence?) -> CharSequence? = {
        it
    }
}