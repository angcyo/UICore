package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.UpdateByNotify
import com.angcyo.widget.DslViewHolder

/**
 * 白色背景加阴影的文本预览item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/05
 */
class DslTextPreviewItem : DslAdapterItem() {

    /**item的文本, 内容*/
    @UpdateByNotify
    var itemText: CharSequence? = null
        set(value) {
            field = value
            updateAdapterItem()
        }

    init {
        itemLayoutId = R.layout.dsl_text_preview_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.lib_text_view)?.text = itemText
    }
}