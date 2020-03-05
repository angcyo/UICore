package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog.dslitem.DslDialogTextItem
import com.angcyo.library.ex._color
import com.angcyo.library.ex._dimen
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class ItemDialogConfig(context: Context? = null) : BaseRecyclerDialogConfig(context) {

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)
    }

    /**添加Item*/
    fun addDialogItem(action: DslDialogTextItem.() -> Unit) {
        adapterItemList.add(DslDialogTextItem().apply {
            itemTopInsert = _dimen(R.dimen.lib_line_px)
            itemDecorationColor = _color(R.color.dialog_line)
            onItemClick = {
                onDialogItemClick(this, it)
            }
            action()
        })
    }
}