package com.angcyo.core.component.manage

import com.angcyo.core.R
import com.angcyo.core.dslitem.DslFileSelectorItem
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import java.io.File

/**
 * 内部文件管理界面的[DslAdapterItem]模型
 *
 * [com.angcyo.core.dslitem.DslFileSelectorItem]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/07
 */
class DslInnerFileItem : DslAdapterItem() {

    /**对应的文件对象*/
    val itemFile: File?
        get() = itemData as? File

    init {
        itemLayoutId = R.layout.item_inner_file
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        val fileName = itemFile?.name
        itemHolder.tv(R.id.lib_text_view)?.text = fileName
        itemHolder.img(R.id.lib_image_view)?.apply {
            setImageResource(DslFileSelectorItem.getFileIconRes(fileName))
        }

        itemHolder.selected(R.id.lib_choose_view, itemIsSelected)
    }

}