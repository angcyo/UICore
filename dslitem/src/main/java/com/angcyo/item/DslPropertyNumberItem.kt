package com.angcyo.item

import android.widget.TextView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig.Companion.STYLE_DECIMAL
import com.angcyo.item.keyboard.keyboardNumberWindow
import com.angcyo.widget.DslViewHolder

/**
 * 数字键盘属性
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022-10-18
 */
open class DslPropertyNumberItem : DslBasePropertyItem() {

    /**属性数值*/
    var itemPropertyNumber: Int? = null

    init {
        itemLayoutId = R.layout.dsl_property_number_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.tv(R.id.lib_text_view)?.text = "${itemPropertyNumber ?: ""}"

        //
        itemHolder.click(R.id.lib_text_view) {
            it.context.keyboardNumberWindow(it) {
                keyboardBindTextView = it as? TextView
                bindPendingDelay = -1 //关闭限流输入
                removeKeyboardStyle(STYLE_DECIMAL)
                onNumberResultAction = { number ->
                    itemPropertyNumber = number.toInt()
                    itemChanging = true
                }
            }
        }
    }
}