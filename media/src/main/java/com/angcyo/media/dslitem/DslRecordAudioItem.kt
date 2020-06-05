package com.angcyo.media.dslitem

import android.app.Activity
import androidx.fragment.app.Fragment
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.updateSingleData
import com.angcyo.library.L
import com.angcyo.library.ex.*
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.loadUri
import com.angcyo.library.toastQQ
import com.angcyo.media.R
import com.angcyo.media.audio.record.RecordControl
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.dslitem.DslNestedRecyclerItem
import com.angcyo.widget.span.span

/**
 * 语音录制item, 支持多个
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslRecordAudioItem : DslNestedRecyclerItem() {

    /**输入按钮提示文本*/
    var itemRecordText: CharSequence? = span {
        drawable {
            backgroundDrawable =
                _drawable(R.drawable.media_record_voice)?.color(_color(R.color.media_audio_tip_color))
        }
        append("长按录制语音")
    }

    var recordControl: RecordControl = RecordControl()

    /**录音文件*/
    var itemLoaderMedias = mutableListOf<LoaderMedia>()

    /**允许录制的最大语音数量*/
    var itemRecordMaxLimit = 5

    /**最大录制时长 s, 默认5分钟*/
    var itemRecordTimeMaxLimit = 5 * 60L

    /**最小录制时长, 默认3秒*/
    var itemRecordTimeMinLimit = 3L

    var itemFragment: Fragment? = null
        set(value) {
            field = value
            if (itemActivity == null) {
                itemActivity = field?.activity
            }
        }

    /**用于显示录制时的提示UI*/
    var itemActivity: Activity? = itemFragment?.activity

    init {
        itemLayoutId = R.layout.dsl_record_audio_item
        //关闭adapter状态提示
        itemNestedAdapter.dslAdapterStatusItem.itemEnable = false
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.lib_record_text_view)?.text = itemRecordText

        //录音
        recordControl.recordUI.maxRecordTime = itemRecordTimeMaxLimit
        recordControl.recordUI.minRecordTime = itemRecordTimeMinLimit
        val recordLayout = itemHolder.view(R.id.lib_record_text_view)
        if (recordLayout != null) {
            when {
                itemActivity == null -> {
                    recordLayout.clickIt {
                        L.e("语音录制表单, 需要[itemFragment]的支持. 请赋值.")
                    }
                }
                itemEnable -> recordControl.wrap(recordLayout, itemActivity!!, onRecordStart = {
                    if (itemLoaderMedias.size >= itemRecordMaxLimit) {
                        toastQQ("录制文件已达上限")
                        false
                    } else {
                        //停止正在播放的录音
                        true
                    }
                }) { recordFile ->
                    val file = recordControl.rename(recordFile)
                    L.i("录制结束:${file}")
                    val localMedia =
                        LoaderMedia(localPath = file.absolutePath, mimeType = "audio/amr")
                    itemLoaderMedias.add(localMedia)
                    itemChanging = true

                    itemNestedAdapter + DslPlayAudioItem().apply {
                        configDslRecordVoiceItem(this, localMedia)
                    }
                }
                else -> itemHolder.gone(R.id.lib_record_text_view)
            }
        }
    }

    override fun onRenderNestedAdapter(dslAdapter: DslAdapter) {
        itemNestedAdapter.updateSingleData<DslPlayAudioItem>(itemLoaderMedias) { data ->
            configDslRecordVoiceItem(this, data as LoaderMedia?)
        }
    }

    /**数据传递*/
    protected open fun configDslRecordVoiceItem(
        item: DslPlayAudioItem,
        loaderMedia: LoaderMedia? = null
    ) {
        item.apply {
            itemData = loaderMedia
            itemAudioUri = loaderMedia?.loadUri()
            itemRecordMaxDuration = itemRecordTimeMaxLimit * 1_000
            itemShowDelete = this@DslRecordAudioItem.itemEnable
            itemLeftInsert = _dimen(R.dimen.lib_padding_left)
            itemTopInsert = 4 * dpi

            itemDeleteAction = {
                itemLoaderMedias.remove(loaderMedia)
                this@DslRecordAudioItem.itemChanging = true
                true
            }
            itemAudioDuration = loaderMedia?.duration ?: -1
        }
    }
}