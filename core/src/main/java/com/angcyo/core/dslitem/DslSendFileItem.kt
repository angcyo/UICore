package com.angcyo.core.dslitem

import android.net.Uri
import com.angcyo.core.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.*
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.progress.DslProgressBar
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

    /**发送的进度[0~100]*/
    var itemSendProgress: Int = 0

    /**错误的原因*/
    var itemErrorThrowable: Throwable? = null

    /**上传耗时, 毫秒*/
    var itemDuration = 0L

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

    /**缓存数据大小*/
    private var itemSendSize: Int? = null

    init {
        itemLayoutId = R.layout.dsl_send_file_item
        itemClick = {
            itemSendUri?.open(it.context)
        }
    }

    override fun onSelfSetItemData(data: Any?) {
        super.onSelfSetItemData(data)
        itemSendSize = itemSendUri?.inputStream()?.available()
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
        itemHolder.tv(R.id.lib_file_des_view)?.text = "${itemSendUri ?: ""}".decode()
        itemHolder.tv(R.id.lib_file_name_view)?.text = span {
            append(_itemSendUriName ?: "")
            append(" ")
            append(itemSendSize?.toSizeString()) {
                fontSize = 9 * dpi
                foregroundColor = _color(R.color.text_sub_color)
            }
            if (itemDuration > 0) {
                append(" 耗时:")
                append(itemDuration.toMsTime()) {
                    fontSize = 9 * dpi
                    foregroundColor = _color(R.color.text_sub_color)
                }
            }
            if (itemSendState == -1) {
                appendln()
                append(itemErrorThrowable?.message) {
                    foregroundColor = _color(R.color.error)
                }
            }
        }
        itemHolder.v<DslProgressBar>(R.id.lib_progress_view)?.apply {
            enableProgressFlowMode = itemSendProgress < 100
            setProgress(itemSendProgress)
        }
        //根据类型显示不同图标
        itemHolder.img(R.id.lib_file_icon_view)?.apply {
            if (_itemSendUriName.isImageType()) {
                setImageURI(itemSendUri)
            } else {
                setImageResource(DslFileSelectorItem.getFileIconRes(_itemSendUriName))
            }
        }
    }
}