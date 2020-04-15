package com.angcyo.media.dslitem

import android.net.Uri
import com.angcyo.download.dslitem.DslBaseDownloadItem
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.isItemAttached
import com.angcyo.library.L
import com.angcyo.library.LTime
import com.angcyo.library.ex.getMediaDuration
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.library.ex.loadUrl
import com.angcyo.media.R
import com.angcyo.media.audio.RPlayer
import com.angcyo.widget.DslViewHolder

/**
 * 语音播放item基类, 先下载后播放的原则
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/22
 */

abstract class DslBaseAudioItem : DslBaseDownloadItem(), RPlayer.OnPlayerListener {
    /**音频地址*/
    var itemAudioUri: Uri? = null

    /**音频显示的标题*/
    var itemAudioTitle: CharSequence? = null

    /**音频时长, 毫秒. 负数会自动从uri中获取*/
    var itemAudioDuration: Long = -1

    /**[onItemViewDetachedToWindow]时, 是否停止播放. 如果设置false, 请手动控制资源的释放*/
    var itemStopOnDetached: Boolean = true

    //播放器
    var _player = RPlayer()

    //记录正在播放的url
    var _playerUrl: String? = null

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

        if (itemAudioDuration < 0) {
            val path = itemAudioUri?.path
            if (!path.isNullOrBlank()) {
                LTime.tick()
                itemAudioDuration = path.getMediaDuration()
                L.d("解析时长:${itemAudioDuration} 耗时:${LTime.time()}")
            }
        }

        itemHolder.tv(R.id.lib_text_view)?.text = itemAudioTitle
    }

    override fun onItemViewDetachedToWindow(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewDetachedToWindow(itemHolder, itemPosition)
        if (itemStopOnDetached) {
            stopPlay()
        }
    }

    override fun onItemViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewRecycled(itemHolder, itemPosition)
        if (itemStopOnDetached) {
            release()
        }
    }

    /**自动处理 暂停/恢复/播放*/
    open fun clickPlay(itemHolder: DslViewHolder?) {
        itemAudioUri?.run {
            if (isHttpScheme()) {
                download(itemHolder, loadUrl()) {
                    _playerUrl = it
                    _player.click(it)
                }
            } else {
                _playerUrl = path
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

    fun resume() {
        _player.resumePlay()
    }

    fun replay() {
        _player.replay()
    }

    /**
     * 指定的url, 是否正在播放中
     * */
    fun isPlaying(url: String = _playerUrl ?: ""): Boolean {
        return (_player.playUrl == url ||
                _player.playUrl == _playerUrl) && _player.isPlaying()
    }

    fun isPause(url: String = _playerUrl ?: ""): Boolean {
        return (_player.playUrl == url ||
                _player.playUrl == _playerUrl) && _player.isPause()
    }

    //<editor-fold desc="播放状态">

    override fun onPreparedCompletion(duration: Int) {
        if (itemAudioDuration < 0) {
            itemAudioDuration = duration.toLong()
        }
        if (isItemAttached()) {
            updateAdapterItem()
        }
    }

    override fun onPlayProgress(progress: Int, duration: Int) {
        if (isItemAttached()) {
            updateAdapterItem()
        }
    }

    override fun onPlayCompletion(duration: Int) {
        if (isItemAttached()) {
            updateAdapterItem()
        }
    }

    override fun onPlayError(what: Int, extra: Int) {
        if (isItemAttached()) {
            updateAdapterItem()
        }
    }

    override fun onPlayStateChange(playUrl: String, from: Int, to: Int) {
        if (isItemAttached()) {
            updateAdapterItem()
        }
    }

    //</editor-fold desc="播放状态">
}