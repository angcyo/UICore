package com.angcyo.item.keyboard

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.LongTouchListener

/**
 * 画布数字自增/自减输入item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/14
 */
class KeyboardNumberIncrementItem : DslAdapterItem() {

    /**是否显示正负号按钮*/
    var itemShowPlusMinus: Boolean = false

    /**自增/自减 回调*/
    var itemIncrementAction: (plus: Boolean, longPress: Boolean) -> Unit = { _, _ ->

    }

    /**正负切换回调 回调*/
    var itemPlusMinusAction: () -> Unit = {

    }

    init {
        itemClickThrottleInterval = 0
        itemLayoutId = R.layout.lib_keyboard_number_increment_item_layout
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.visible(R.id.plus_minus_view, itemShowPlusMinus)
        itemHolder.longTouch(R.id.minus_increment_view, true) { view, event, eventType ->
            eventType?.let {
                itemIncrementAction(false, it == LongTouchListener.EVENT_TYPE_LONG_PRESS)
            }
            true
        }
        itemHolder.longTouch(R.id.plus_increment_view, true) { view, event, eventType ->
            eventType?.let {
                itemIncrementAction(true, it == LongTouchListener.EVENT_TYPE_LONG_PRESS)
            }
            true
        }
        itemHolder.click(R.id.plus_minus_view) {
            itemPlusMinusAction()
        }
    }
}