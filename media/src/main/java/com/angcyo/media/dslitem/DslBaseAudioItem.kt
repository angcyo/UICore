package com.angcyo.media.dslitem

import android.net.Uri
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.media.audio.RPlayer
import com.angcyo.widget.DslViewHolder

/**
 * 语音播放item基类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/22
 */

abstract class DslBaseAudioItem : DslBaseDownloadItem(), RPlayer.OnPlayerListener {
    /**音频地址*/
    var itemAudioUri: Uri? = null

    val _player = RPlayer()

    init {
        _player.onPlayListener = this
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
    }

    override fun onItemViewDetachedToWindow(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewDetachedToWindow(itemHolder, itemPosition)
        stopPlay()
    }

    override fun onItemViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewRecycled(itemHolder, itemPosition)
        release()
    }

    /**自动处理 暂停/恢复/播放*/
    open fun clickPlay(itemHolder: DslViewHolder?) {
        itemAudioUri?.run {
            if (isHttpScheme()) {
                download(itemHolder, path) {
                    _player.click(it)
                }
            } else {
                _player.click(path)
            }
        }
    }

    open fun stopPlay() {
        _player.stopPlay()
    }

    open fun release() {
        _player.release()
    }

    //<editor-fold desc="播放状态">

    override fun onPreparedCompletion(duration: Int) {
        updateAdapterItem()
    }

    override fun onPlayProgress(progress: Int, duration: Int) {
        updateAdapterItem()
    }

    override fun onPlayCompletion(duration: Int) {
        updateAdapterItem()
    }

    override fun onPlayError(what: Int, extra: Int) {
        updateAdapterItem()
    }

    override fun onPlayStateChange(playUrl: String, from: Int, to: Int) {
        updateAdapterItem()
    }

    //</editor-fold desc="播放状态">
}