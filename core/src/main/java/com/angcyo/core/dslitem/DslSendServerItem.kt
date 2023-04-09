package com.angcyo.core.dslitem

import com.angcyo.component.hawkInstallAndRestore
import com.angcyo.core.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.http.base.readString
import com.angcyo.http.base.toTextBody
import com.angcyo.http.postBody2Body
import com.angcyo.http.rx.observe
import com.angcyo.library.ex.*
import com.angcyo.library.toastQQ
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.string
import com.angcyo.widget.span.span

/**
 * 要发送到的服务器http地址: http://xxx.xxx:xxx
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/09
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DslSendServerItem : DslAdapterItem() {

    /**发送文件触发的回调*/
    var itemSendFileAction: (url: String) -> Unit = {}

    /**是否发送完成*/
    var itemIsSendFinish: Boolean = true

    /**服务端的地址, 自动获取的*/
    var itemAddress: String? = null

    init {
        itemLayoutId = R.layout.dsl_send_server_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.hawkInstallAndRestore("SendServer_")

        itemAddress?.let {
            itemHolder.tv(R.id.lib_address_edit_view)?.apply {
                text = it
                setTextColor(_color(R.color.error))
                longFeedback()
            }
        }

        itemHolder.focused(R.id.lib_send_file_button)
        itemHolder.enable(R.id.lib_send_file_button, itemIsSendFinish)

        itemHolder.click(R.id.lib_send_file_button) {
            it.isEnabled = false
            itemIsSendFinish = false
            itemSendFileAction(
                itemHolder.tv(R.id.lib_address_edit_view).string(true).connectUrl("/uploadFile")
            )
        }

        itemHolder.click(R.id.lib_send_body_button) {
            it.isEnabled = false
            postBody2Body(itemHolder.tv(R.id.lib_content_edit_view).string(false).toTextBody()) {
                url = itemHolder.tv(R.id.lib_address_edit_view).string(true).connectUrl("/body")
            }.observe { data, error ->
                it.isEnabled = true
                error?.let {
                    toastQQ(it.message)
                }
                data?.let {
                    itemHolder.tv(R.id.lib_result_view)?.text = span {
                        append(nowTimeString()) {
                            foregroundColor = randomColor()
                        }
                        appendln()
                        append(it.body().readString())
                    }
                }
            }
        }
    }

}