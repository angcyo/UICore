package com.angcyo.core.component

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.angcyo.core.R
import com.angcyo.core.dslitem.DslSendEmptyItem
import com.angcyo.core.dslitem.DslSendFileItem
import com.angcyo.core.dslitem.DslSendServerItem
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.findItem
import com.angcyo.dsladapter.updateAllItemBy
import com.angcyo.getParcelableList
import com.angcyo.http.asRequestBody
import com.angcyo.http.base.getString
import com.angcyo.http.base.toFilePart
import com.angcyo.http.base.toJsonElement
import com.angcyo.http.progress.toProgressBody
import com.angcyo.http.rx.observe
import com.angcyo.http.udp.udpReceive
import com.angcyo.http.uploadFile2Body
import com.angcyo.library.ex._string
import com.angcyo.library.ex.getShowName
import com.angcyo.library.ex.inputStream
import com.angcyo.lifecycle.cancelOnDestroy

/**
 * 2023-04-09
 * 用来发送流数据到指定服务器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/09
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ShareSendFragment : BaseDslFragment() {

    companion object {

        /**需要发送的[Uri]集合放在这里*/
        const val KEY_URI_LIST = Intent.EXTRA_STREAM
    }

    init {
        fragmentTitle = _string(R.string.core_send_to_label)
    }

    /**外部传递过来, 需要发送的[Uri]数据*/
    var sendUriList: List<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendUriList = getParcelableList(KEY_URI_LIST)
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        renderDslAdapter {
            DslSendServerItem()() {
                itemSendFileAction = { url, retry ->
                    if (retry) {
                        //重试
                        updateAllItemBy {
                            if (it is DslSendFileItem) {
                                it.itemSendState = 0
                                it.itemSendProgress = 0
                                it.itemErrorThrowable = null
                                true
                            } else {
                                false
                            }
                        }
                    }
                    startUploadFile(url)
                }
                udpReceive(9999) { content, error ->
                    content?.toJsonElement()?.getString("url")?.let {
                        itemAddress = it
                        updateAdapterItem() //更新item状态
                    }
                }.cancelOnDestroy(this@ShareSendFragment)
            }
            if (sendUriList.isNullOrEmpty()) {
                DslSendEmptyItem()()
            } else {
                sendUriList?.forEach {
                    DslSendFileItem()() {
                        itemData = it
                    }
                }
            }
        }
    }

    /**开始上传文件*/
    private fun DslAdapter.startUploadFile(url: String) {
        if (isDetached) {
            return  //已经销毁
        }
        val fileItem = findItem { it is DslSendFileItem && it.itemSendState != 1 }
        if (fileItem == null || fileItem !is DslSendFileItem) {
            //上传完成
            findItem<DslSendServerItem> {
                itemIsSendFinish = true
                updateAdapterItem() //更新item状态
            }
        } else {
            fileItem.apply {
                val uri = itemSendUri
                uri?.inputStream()?.asRequestBody()?.let { body ->
                    uploadFile2Body {
                        this.url = url
                        filePart = body.toProgressBody { progressInfo, exception ->
                            itemSendProgress = progressInfo?.percent ?: 0
                            updateAdapterItem() //更新item进度
                        }
                            .toFilePart(uri.getShowName())
                    }.observe { data, error ->
                        itemSendState = if (error == null) {
                            1
                        } else {
                            -1
                        }
                        updateAdapterItem() //更新item状态
                        startUploadFile(url) //继续上传下一个文件
                    }
                }
            }
        }
    }
}