package com.angcyo.media.dslitem

import android.view.View
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.media.R
import com.angcyo.media.audio.SimplePlayerListener
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.progress.HSProgressView
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/22
 */
class DslPreviewAudioItem : DslBaseAudioItem() {
    init {
        itemLayoutId = R.layout.dsl_preview_audio_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        _player.onPlayListener = object : SimplePlayerListener() {
            override fun onPlayProgress(progress: Int, duration: Int) {
                super.onPlayProgress(progress, duration)
            }

            override fun onPlayStateChange(playUrl: String, from: Int, to: Int) {
                super.onPlayStateChange(playUrl, from, to)
                updatePlayStatus(itemHolder)
            }
        }

        updatePlayStatus(itemHolder)

        itemHolder.click(R.id.play_view) {
            clickPlay(itemHolder)
        }
    }

    fun updatePlayStatus(itemHolder: DslViewHolder) {
        if (_player.isPlaying()) {
            itemHolder.img(R.id.play_view)?.setImageResource(R.drawable.media_pause)
        } else {
            itemHolder.img(R.id.play_view)?.setImageResource(R.drawable.media_play)
        }
    }

    override fun onDownloadStart(itemHolder: DslViewHolder?, task: DownloadTask) {
        super.onDownloadStart(itemHolder, task)
        //开始下载视频
        itemHolder?.v<HSProgressView>(R.id.hs_progress_view)?.apply {
            visibility = View.VISIBLE
            startAnimator()
        }
    }

    override fun onDownloadFinish(
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) {
        super.onDownloadFinish(itemHolder, task, cause, error)
        //开始下载视频
        itemHolder?.v<HSProgressView>(R.id.hs_progress_view)?.apply {
            visibility = View.GONE
            stopAnimator()
        }
    }
}