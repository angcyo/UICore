package com.angcyo.pager.dslitem

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._dimen
import com.angcyo.media.dslitem.DslTextureVideoItem
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.updateMarginParams

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/07
 */
class DslPickerTextureVideoItem : DslTextureVideoItem() {
    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.view(R.id.bottom_wrap_layout)?.updateMarginParams {
            bottomMargin = _dimen(R.dimen.picker_media_progress_margin_bottom)
        }
    }
}