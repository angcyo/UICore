package com.angcyo.media.dslitem

import android.net.Uri
import android.view.Gravity
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.elseNull
import com.angcyo.library.ex.loadUrl
import com.angcyo.media.R
import com.angcyo.media.audio.record.RecordUI
import com.angcyo.media.audio.widget.VoiceView
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.frameParams
import com.angcyo.widget.progress.DYProgressView
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause

/**
 * 播放音频的item, 支持本地/在线
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslPlayAudioItem : DslBaseAudioItem() {

    /**允许的最大录制时长, 用来动态计算宽度. 毫秒*/
    var itemRecordMaxDuration = -1L

    /**重力*/
    var itemAudioLayoutGravity: Int = Gravity.LEFT or Gravity.CENTER_VERTICAL
    var itemAudioLayoutMinWidth: Int = 86 * dpi

    /**是否显示删除按钮*/
    var itemShowDelete: Boolean = false

    /**回调是否允许删除*/
    var itemDeleteAction: (Uri) -> Boolean = { true }

    init {
        itemLayoutId = R.layout.dsl_play_audio_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {

        //缓存时长, 先从路径中获取, 再从流中获取
        if (itemAudioDuration == -1L) {
            itemAudioUri?.let {
                itemAudioDuration = RecordUI.getRecordTime(it.loadUrl()).run {
                    if (this > 0) {
                        this * 1_000L
                    } else {
                        toLong()
                    }
                }
            }
        }

        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //恢复播放状态
//        if (isPlaying()) {
//            playStatus = RPlayer.STATE_PLAYING
//
//            VoicePlayControl.playControl?.onPlayerStatusChangeListener =
//                onPlayerStatusChangeListener
//        }

        //播放状态判断
        if (isPlaying()) {
            itemHolder.v<VoiceView>(R.id.voice_view)?.play()
        } else {
            itemHolder.v<VoiceView>(R.id.voice_view)?.stop()
        }

        if (itemAudioDuration > 0) {
            itemHolder.tv(R.id.audio_duration_view)?.text = "${itemAudioDuration / 1000}\""
        } else {
            itemHolder.tv(R.id.audio_duration_view)?.text = ""
        }

        //下载进度条提示
        _downTask?.apply {
            if (taskStatus() == StatusUtil.Status.RUNNING) {
                itemHolder.visible(R.id.dy_progress_view)
                itemHolder.v<DYProgressView>(R.id.dy_progress_view)?.startAnimator()
            } else {
                itemHolder.gone(R.id.dy_progress_view)
            }
        }.elseNull {
            itemHolder.gone(R.id.dy_progress_view)
        }

        //删除
        itemHolder.visible(R.id.delete_view, itemShowDelete)
        itemHolder.click(R.id.delete_view) {
            itemAudioUri?.let {
                if (itemDeleteAction(it)) {
                    release()
                    itemDslAdapter?.removeItem(this)
                }
            }.elseNull {
                release()
                itemDslAdapter?.removeItem(this)
            }
        }

        //智能根据录音时长, 设置item的宽度
        var itemWidth = itemAudioLayoutMinWidth
        if (itemAudioDuration > 0 && itemRecordMaxDuration > 0) {
            val ratio = itemAudioDuration * 1f / itemRecordMaxDuration
            itemWidth = when {
                ratio < 0.5 -> {
                    (itemAudioLayoutMinWidth + _screenWidth * 3 / 4 * ratio).toInt()
                }
                ratio < 0.8 -> {
                    (itemAudioLayoutMinWidth + _screenWidth / 3 * ratio).toInt()
                }
                else -> {
                    (itemAudioLayoutMinWidth + _screenWidth / 2 * ratio).toInt()
                }
            }
        }
        itemHolder.view(R.id.content_wrap_layout)?.apply {
            minimumWidth = itemWidth
            frameParams {
                width = itemWidth
                gravity = itemAudioLayoutGravity
            }
            //播放
            clickIt {
                clickPlay(itemHolder)
            }
        }
    }

    override fun onDownloadStart(itemHolder: DslViewHolder?, task: DownloadTask) {
        super.onDownloadStart(itemHolder, task)
        itemHolder?.visible(R.id.dy_progress_view)
        itemHolder?.v<DYProgressView>(R.id.dy_progress_view)?.startAnimator()
    }

    override fun onDownloadFinish(
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) {
        super.onDownloadFinish(itemHolder, task, cause, error)
        itemHolder?.gone(R.id.dy_progress_view)
    }
}