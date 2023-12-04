package com.angcyo.item.keyboard

import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.R
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.TextItemConfig
import com.angcyo.widget.DslViewHolder

/**
 * 画布数字输入item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/14
 */
class KeyboardNumberItem : DslAdapterItem(), ITextItem {

    /**[android.widget.LinearLayout.LayoutParams.weight]*/
    var itemWeight: Float? = null

    override var textItemConfig: TextItemConfig = TextItemConfig()

    init {
        itemLayoutId = R.layout.lib_keyboard_number_item_layout
        itemClickThrottleInterval = 0
        textItemConfig.itemTextStyle.textGravity = Gravity.CENTER
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemWeight?.let {
            itemHolder.itemView.updateLayoutParams<LinearLayout.LayoutParams> {
                weight = it
            }
        }
    }
}