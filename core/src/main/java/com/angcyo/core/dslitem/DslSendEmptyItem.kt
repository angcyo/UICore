package com.angcyo.core.dslitem

import android.graphics.drawable.Drawable
import com.angcyo.core.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._drawable
import com.angcyo.widget.DslViewHolder

/**
 * 没有发送的文件时, 空提示的item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/10
 */
class DslSendEmptyItem : DslAdapterItem() {

    /**空提示的图片*/
    var itemEmptyImage: Drawable? = _drawable(R.drawable.core_file_empty_state)

    /**空提示的文本*/
    var itemEmptyTip: CharSequence? = "请在第三方应用选中文件后,选择分享/发送给本应用"

    init {
        itemLayoutId = R.layout.dsl_send_empty_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.img(R.id.lib_image_view)?.setImageDrawable(itemEmptyImage)
        itemHolder.tv(R.id.lib_text_view)?.text = itemEmptyTip
    }

}