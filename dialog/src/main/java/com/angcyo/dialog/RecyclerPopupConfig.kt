package com.angcyo.dialog

import android.app.Activity
import android.widget.PopupWindow
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.ItemSelectorHelper
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class RecyclerPopupConfig : PopupConfig() {

    var popupSelectorModel: Int = ItemSelectorHelper.MODEL_NORMAL

    val _recyclerConfig = RecyclerConfig()

    var onPopupResult: (host: Any, itemList: List<DslAdapterItem>, indexList: List<Int>) -> Boolean =
        { _, _, _ -> false }

    init {
        layoutId = R.layout.lib_popup_recycler_layout
    }

    override fun initPopupWindow(popupWindow: PopupWindow, popupViewHolder: DslViewHolder) {
        super.initPopupWindow(popupWindow, popupViewHolder)
        initRecycler(popupWindow, popupViewHolder)
    }

    override fun initPopupActivity(activity: Activity, popupViewHolder: DslViewHolder) {
        super.initPopupActivity(activity, popupViewHolder)
        initRecycler(activity, popupViewHolder)
    }

    open fun initRecycler(host: Any, popupViewHolder: DslViewHolder) {
        _recyclerConfig.adapterSelectorModel = popupSelectorModel
        _recyclerConfig.initRecycler(popupViewHolder)
        _recyclerConfig.adapterItemClick = { dslItem, view ->
            if (onPopupResult(host, listOf(dslItem), listOf(dslItem.itemIndexPosition()))
            ) {
                //拦截
            } else {
                if (host is PopupWindow) {
                    host.dismiss()
                } else {
                    hide()
                }
            }
        }
    }
}