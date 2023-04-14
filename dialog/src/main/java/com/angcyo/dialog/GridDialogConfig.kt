package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog.dslitem.DslDialogTextItem
import com.angcyo.dsladapter.margin
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.recycler.resetLayoutManager

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class GridDialogConfig(context: Context? = null) : RecyclerDialogConfig(context) {

    /**网格列数*/
    var gridSpanCount = 4

    init {
        dialogLayoutId = R.layout.lib_dialog_recycler_grid_layout
        _cancelItemWrapLayoutId = R.id.cancel_wrap_layout
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)
        dialogViewHolder.rv(R.id.lib_recycler_view)?.apply {
            resetLayoutManager("GV$gridSpanCount")
        }

        dialogViewHolder.visible(_cancelItemWrapLayoutId, dialogBottomCancelItem != null)
    }

    override fun defaultCancelItem(): DslDialogTextItem {
        return super.defaultCancelItem().apply {
            margin(0)
            itemBackgroundDrawable = null
        }
    }
}