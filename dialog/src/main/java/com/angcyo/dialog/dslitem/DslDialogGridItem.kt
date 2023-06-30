package com.angcyo.dialog.dslitem

import android.graphics.drawable.Drawable
import com.angcyo.dialog.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.undefined_res
import com.angcyo.library.ex.visible
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.updateConstraintParams

/**
 * 网格对话框中的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class DslDialogGridItem : DslDialogTextItem() {

    /**网格图标*/
    var itemGridIcon: Int = undefined_res
    var itemGridDrawable: Drawable? = null

    /**图标相对于item的宽度的比例*/
    var itemGridWidthPercent: Float = 0.6f

    init {
        itemLayoutId = R.layout.item_dialog_grid
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.img(R.id.lib_image_view)?.run {
            updateConstraintParams {
                matchConstraintPercentWidth = itemGridWidthPercent
            }

            if (itemGridIcon != undefined_res) {
                setImageResource(itemGridIcon)
            } else {
                setImageDrawable(itemGridDrawable)
            }

            visible(itemGridIcon != undefined_res || itemGridDrawable != null)
        }
    }
}