package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.itemViewHolder
import com.angcyo.item.style.IIncrementItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.IOperateEditItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.item.style.OperateEditItemConfig
import com.angcyo.item.style.StepAdjustItemConfig
import com.angcyo.item.style.itemEditText
import com.angcyo.item.style.itemIncrementValue
import com.angcyo.widget.DslViewHolder

/**
 * 加减自增item, 支持- + 按钮.
 * 支持手动输入
 * 支持label
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/05
 */
open class DslIncrementItem : DslAdapterItem(), ILabelItem, IIncrementItem, IOperateEditItem {

    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

    override var stepAdjustItemConfig: StepAdjustItemConfig = StepAdjustItemConfig()

    override var operateEditItemConfig: OperateEditItemConfig = OperateEditItemConfig().apply {
        itemEditTextViewId = stepAdjustItemConfig.itemStepAdjustValueViewId
    }

    init {
        itemLayoutId = R.layout.dsl_increment_item
    }

    override fun onItemChangeListener(item: DslAdapterItem) {
        hookOperateEditItemFocus(itemViewHolder())
        super.onItemChangeListener(item)
    }

    /**当前的文本改变, 是否是来自加减号操作*/
    protected var _isFromStepAdjust = false

    override fun onSelfOperateItemEditTextChange(itemHolder: DslViewHolder, text: CharSequence) {
        if (_isFromStepAdjust || text.isBlank()) {
            //no op
        } else if (itemEditText != itemIncrementValue) {
            updateStepAdjustValue(text)
            super.onSelfOperateItemEditTextChange(itemHolder, text)
        }
        _isFromStepAdjust = false
    }

    override fun onSelfOperateItemEditFocusChange(itemHolder: DslViewHolder, focus: Boolean) {
        if (itemEditText.isNullOrBlank() && !itemIncrementValue.isNullOrBlank()) {
            itemEditText = itemIncrementValue
            updateStepAdjustValue(itemIncrementValue)
            super.onSelfOperateItemEditTextChange(itemHolder, itemIncrementValue ?: "")
        }
    }

    override fun updateStepAdjustValue(
        itemHolder: DslViewHolder,
        adapterItem: DslAdapterItem,
        value: CharSequence?,
        notifyItemChanged: Boolean
    ) {
        //hookOperateEditItemFocus(itemViewHolder())
        //直接更新到最后
        val selectionStart = value?.length ?: 0
        val selectionEnd = selectionStart
        _isFromStepAdjust = true
        super.updateStepAdjustValue(itemHolder, adapterItem, value, notifyItemChanged)
        operateEditItemConfig._lastEditSelectionStart = selectionStart
        operateEditItemConfig._lastEditSelectionEnd = selectionEnd
        itemEditText = itemIncrementValue
    }
}