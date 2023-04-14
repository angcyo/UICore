package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog.dslitem.DslDialogTextItem
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class ItemDialogConfig(context: Context? = null) : RecyclerDialogConfig(context) {

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)
    }

    override fun initRecyclerConfig(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initRecyclerConfig(dialog, dialogViewHolder)
    }

    override fun addItem(item: DslAdapterItem) {
        super.addItem(item)
    }

    override fun addDialogItem(action: DslDialogTextItem.() -> Unit) {
        super.addDialogItem(action)
    }
}