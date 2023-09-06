package com.angcyo.item

import android.widget.TextView
import com.angcyo.dialog.TargetWindow
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig
import com.angcyo.item.keyboard.keyboardNumberWindow
import com.angcyo.item.style.IStepAdjustItem
import com.angcyo.item.style.StepAdjustItemConfig
import com.angcyo.library.ex.string
import com.angcyo.library.ex.toStr
import com.angcyo.widget.DslViewHolder
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * 步长调整item, 支持- + 按钮.
 * 默认只支持+-调整, 输入调整, 键盘调整, 请继承
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/01
 */
open class DslStepAdjustItem : DslAdapterItem(), IStepAdjustItem {

    override var stepAdjustItemConfig: StepAdjustItemConfig = StepAdjustItemConfig()

    init {
        itemLayoutId = R.layout.dsl_step_adjust_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        bindStepAdjustValue(itemHolder)
    }

    /**键盘弹窗*/
    protected open fun bindStepAdjustValue(itemHolder: DslViewHolder) {
        itemHolder.click(stepAdjustItemConfig.itemStepAdjustValueViewId) {
            itemHolder.context.keyboardNumberWindow(it) {
                onDismiss = this@DslStepAdjustItem::onPopupDismiss
                keyboardBindTextView = it as? TextView
                incrementStep =
                    stepAdjustItemConfig.itemStepAdjustStep.toString().toFloatOrNull() ?: 1f
                longIncrementStep = incrementStep * 10
                if (stepAdjustItemConfig.isIntValueType || stepAdjustItemConfig.isLongValueType) {
                    removeKeyboardStyle(NumberKeyboardPopupConfig.STYLE_DECIMAL)
                }
                onNumberResultAction = { value ->
                    if (stepAdjustItemConfig.itemStepAdjustAction(
                            keyboardBindTextView.string(),
                            value
                        )
                    ) {
                        //被拦截
                    } else {
                        if (stepAdjustItemConfig.isLongValueType) {
                            updateStepAdjustValue(
                                itemHolder,
                                this@DslStepAdjustItem,
                                value.roundToLong().toStr()
                            )
                        } else if (stepAdjustItemConfig.isIntValueType) {
                            updateStepAdjustValue(
                                itemHolder,
                                this@DslStepAdjustItem,
                                value.roundToInt().toStr()
                            )
                        } else {
                            updateStepAdjustValue(itemHolder, this@DslStepAdjustItem, value.toStr())
                        }
                    }
                }
            }
        }
    }

    /**popup销毁后, 刷新item*/
    protected open fun onPopupDismiss(window: TargetWindow): Boolean {
        updateAdapterItem()
        return false
    }
}