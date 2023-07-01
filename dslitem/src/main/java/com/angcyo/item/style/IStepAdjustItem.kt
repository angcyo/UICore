package com.angcyo.item.style

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.toStr
import com.angcyo.widget.DslViewHolder
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
            if (eventType != null) {
                val stepAdjustStep = if (eventType == DslViewHolder.EVENT_TYPE_LONG_PRESS) {
                    stepAdjustItemConfig.itemStepAdjustLongStep
                } else {
                    stepAdjustItemConfig.itemStepAdjustStep
                }
                if (stepAdjustItemConfig.isIntValueType) {
                    val value = itemStepAdjustValueView?.string()?.toIntOrNull() ?: 0
                    val step = stepAdjustStep.toString().toIntOrNull() ?: 1
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), -step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(itemHolder, adapterItem, (value - step).toStr())
                    }
                } else {
                    val value = itemStepAdjustValueView?.string()?.toFloatOrNull() ?: 0f
                    val step = stepAdjustStep.toString().toFloatOrNull() ?: 1f
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), -step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(itemHolder, adapterItem, (value - step).toStr())
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
            if (eventType != null) {
                val stepAdjustStep = if (eventType == DslViewHolder.EVENT_TYPE_LONG_PRESS) {
                    stepAdjustItemConfig.itemStepAdjustLongStep
                } else {
                    stepAdjustItemConfig.itemStepAdjustStep
                }
                if (stepAdjustItemConfig.isIntValueType) {
                    val value = itemStepAdjustValueView?.string()?.toIntOrNull() ?: 0
                    val step = stepAdjustStep.toString().toIntOrNull() ?: 1
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), +step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(itemHolder, adapterItem, (value + step).toStr())
                    }
                } else {
                    val value = itemStepAdjustValueView?.string()?.toFloatOrNull() ?: 0f
                    val step = stepAdjustStep.toString().toFloatOrNull() ?: 1f
                    if (stepAdjustItemConfig.itemStepAdjustAction(value.toString(), +step)) {
                        //被拦截
                    } else {
                        updateStepAdjustValue(itemHolder, adapterItem, (value + step).toStr())
                    }
                }
            }
            true
        }
    }

    /**更新值*/
    @CallPoint
    fun updateStepAdjustValue(
        itemHolder: DslViewHolder,
        adapterItem: DslAdapterItem,
        value: CharSequence?
    ) {
        val itemStepAdjustValueView = itemHolder.tv(stepAdjustItemConfig.itemStepAdjustValueViewId)
        stepAdjustItemConfig.itemStepAdjustValue =
            stepAdjustItemConfig.itemStepAdjustChangedAction(value?.toStr())
        itemStepAdjustValueView?.text = stepAdjustItemConfig.itemStepAdjustValue

        stepAdjustItemConfig.itemStepAdjustValue = value
        adapterItem.itemChanged = true
    }
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

    /**每次调整的步长
     * 自动识别[Int] [Float]类型*/
    var itemStepAdjustStep: Any = 1

    /**长按时的步长*/
    var itemStepAdjustLongStep: Any = 10

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