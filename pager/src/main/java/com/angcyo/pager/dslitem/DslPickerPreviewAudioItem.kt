package com.angcyo.pager.dslitem

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._dimen
import com.angcyo.media.dslitem.DslPreviewAudioItem
import com.angcyo.pager.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.updateMarginParams

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/07
 */
class DslPickerPreviewAudioItem : DslPreviewAudioItem() {

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.view(R.id.bottom_wrap_layout)?.updateMarginParams {
            bottomMargin = _dimen(R.dimen.pager_media_progress_margin_bottom)
        }
    }
}