package com.angcyo.core.dslitem

import android.net.Uri
import com.angcyo.core.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.*
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.span.span

/**
 * 要发送的文件item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/09
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DslSendFileItem : DslAdapterItem() {

    /**发送文件的状态
     * 1: 成功
     * -1: 失败*/
    var itemSendState: Int = 0

    /**错误的原因*/
    var itemErrorThrowable: Throwable? = null

    //---

    /**需要发送的[Uri]数据*/
    val itemSendUri: Uri?
        get() = itemData as? Uri

    private var _itemSendUriName: String? = null
        get() {
            if (field.isNullOrEmpty()) {
                field = itemSendUri?.getShowName()
            }
            return field
        }

    init {
        itemLayoutId = R.layout.dsl_send_file_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.img(R.id.lib_state_image_view)?.setImageResource(
            when (itemSendState) {
                1 -> R.drawable.lib_ic_succeed
                -1 -> R.drawable.lib_ic_error
                else -> 0 //默认
            }
        )
        itemHolder.tv(R.id.lib_file_name_view)?.text = span {
            append(_itemSendUriName ?: "")
            append(" ")
            append(itemSendUri?.inputStream()?.available()?.toSizeString()) {
                fontSize = 9 * dpi
                foregroundColor = _color(R.color.text_sub_color)
            }
            if (itemSendState == -1) {
                appendln()
                append(itemErrorThrowable?.message) {
                    foregroundColor = _color(R.color.error)
                }
            }
        }
    }
}