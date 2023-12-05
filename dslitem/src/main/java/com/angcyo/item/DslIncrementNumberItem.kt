package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.keyboard.numberKeyboardDialog
import com.angcyo.item.style.IIncrementItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.item.style.StepAdjustItemConfig
import com.angcyo.item.style.itemLabelText
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt

/**
 * 加减自增item, 支持- + 按钮.
 * 支持label
 * 支持数字键盘输入
 * 不支持手动输入
 *
 * [DslIncrementItem]
 * [DslIncrementNumberItem]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/12/05
 */
class DslIncrementNumberItem : DslAdapterItem(), ILabelItem, IIncrementItem {
    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

    override var stepAdjustItemConfig: StepAdjustItemConfig = StepAdjustItemConfig()

    init {
        itemLayoutId = R.layout.dsl_increment_number_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(stepAdjustItemConfig.itemStepAdjustValueViewId)?.apply {
            clickIt {
                it.context.numberKeyboardDialog {
                    dialogTitle = itemLabelText
                    numberValue = stepAdjustItemConfig.itemStepAdjustValue
                    numberMinValue = stepAdjustItemConfig.itemAdjustMinValue
                    numberMaxValue = stepAdjustItemConfig.itemAdjustMaxValue

                    onNumberResultAction = { value ->
                        value?.let {
                            updateStepAdjustValue(itemHolder, adapterItem, value, true)
                        }
                        false
                    }
                }
            }
        }
    }
}