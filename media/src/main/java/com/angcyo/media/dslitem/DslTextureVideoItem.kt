package com.angcyo.media.dslitem

import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import com.angcyo.download.dslitem.DslBaseDownloadItem
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.glide.loadImage
import com.angcyo.library.app
import com.angcyo.library.ex.fileUri
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.library.ex.loadUrl
import com.angcyo.media.MediaHelper
import com.angcyo.media.R
import com.angcyo.media.video.widget.TextureVideoView
import com.angcyo.widget.DslViewHolder
import com.angcyo.library.ex.Anim
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause

/**
 * 全屏视频播放界面item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/21
 */

open class DslTextureVideoItem : DslBaseDownloadItem() {

    companion object {

        const val KEY_DISABLE_DOWNLOAD = "disable_download"

        /**禁用视频的先下载后播放, 而使用直播播放*/
        fun disableDownload(url: String?, disable: Boolean = true): String? {
            if (url.isNullOrEmpty()) {
                return url
            }
            return if (url.contains("?")) {
                "$url&$KEY_DISABLE_DOWNLOAD=${disable}"
            } else {
                "$url?$KEY_DISABLE_DOWNLOAD=${disable}"
            }
        }

        fun disableDownload(uri: Uri, disable: Boolean = true): Uri {
            val builder = uri.buildUpon()
            builder.appendQueryParameter(KEY_DISABLE_DOWNLOAD, "$disable")
            return builder.build()
        }
    }

    /**视频地址*/
    var itemVideoUri: Uri? = null

    /**播放监听*/
    var onPlayListener: (TextureVideoView) -> Unit = {}

    init {
        itemLayoutId = R.layout.dsl_texture_video_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        val videoView: TextureVideoView? = itemHolder.v(R.id.lib_video_view)
        videoView?.setRepeatPlay(false)
        videoView?.stop()

        //加载封面
        itemHolder.img(R.id.lib_image_view)?.apply {
            loadImage(itemVideoUri) {
                //使用原始大小
                originalSize = true
            }
            alpha = 1f
            visibility = View.VISIBLE
        }
        itemHolder.visible(R.id.play_view)
        MediaHelper.resetLayout(itemHolder) { value, fraction ->
            videoView?.seekToFraction(fraction)
        }

        //视频
        if (itemVideoUri == null) {
            return
        }

        videoView?.setMediaPlayerCallback(object : TextureVideoView.SimpleMediaPlayerCallback() {
            override fun onPrepared(mp: MediaPlayer) {
                super.onPrepared(mp)
                onPlayStateChanged(mp, TextureVideoView.STATE_PLAYING)
            }

            override fun onCompletion(mp: MediaPlayer) {
                super.onCompletion(mp)
                onPlayStateChanged(mp, TextureVideoView.STATE_PLAYBACK_COMPLETED)
            }

            override fun onPlayStateChanged(mp: MediaPlayer?, newState: Int) {
                super.onPlayStateChanged(mp, newState)
                if (newState == TextureVideoView.STATE_PLAYING) {
                    itemHolder.gone(R.id.play_view)
                    itemHolder.gone(R.id.hs_progress_view)
                    itemHolder.view(R.id.lib_image_view)?.run {
                        animate().alpha(0.5f)
                            .setDuration(Anim.ANIM_DURATION)
                            .withEndAction {
                                itemHolder.gone(R.id.lib_image_view)
                            }
                            .start()
                    }
                } else {
                    itemHolder.visible(R.id.play_view)
                }
            }

            override fun onVideoPlayProgress(mp: MediaPlayer, progress: Int, duration: Int) {
                super.onVideoPlayProgress(mp, progress, duration)
                MediaHelper.showMediaProgressView(
                    itemHolder, progress.toLong(),
                    duration.toLong()
                )
            }
        })

        //点击视频, 暂停or恢复
        itemHolder.click(R.id.lib_video_view) {
            if (videoView?.isPlaying == true) {
                videoView.pause()
            } else {
                videoView?.play()
            }
        }

        itemHolder.click(R.id.lib_image_view) {
            itemHolder.clickView(R.id.play_view)
        }

        //播放视频
        itemHolder.click(R.id.play_view) {
            val videoUri = itemVideoUri ?: return@click

            itemHolder.gone(R.id.play_view)

            if (videoUri.isHttpScheme()) {
                //下载视频
                if (videoUri.getQueryParameter(KEY_DISABLE_DOWNLOAD) == "true") {
                    //禁用先下载后播放
                    playVideo(itemHolder, videoUri)
                } else {
                    download(itemHolder, videoUri.loadUrl()) {
                        playVideo(itemHolder, fileUri(app(), it))
                    }
                }
            } else {
                //本地视频
                playVideo(itemHolder, videoUri)
            }
        }

        //item 事件
        itemHolder.longClick(R.id.lib_video_view, _longClickListener)
    }

    override fun onItemViewDetachedToWindow(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewDetachedToWindow(itemHolder, itemPosition)
        itemHolder.v<TextureVideoView>(R.id.lib_video_view)?.pause()
    }

    override fun onItemViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewRecycled(itemHolder, itemPosition)
        itemHolder.v<TextureVideoView>(R.id.lib_video_view)?.stop()
    }

    override fun onDownloadStart(itemHolder: DslViewHolder?, task: DownloadTask) {
        super.onDownloadStart(itemHolder, task)
        //开始下载视频
        MediaHelper.showMediaLoadingView(itemHolder)
    }

    override fun onDownloadFinish(
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) {
        super.onDownloadFinish(itemHolder, task, cause, error)
        //下载完成
        MediaHelper.showMediaLoadingView(itemHolder, false)
    }

    /**播放视频*/
    open fun playVideo(itemHolder: DslViewHolder, uri: Uri) {
        val videoView: TextureVideoView? = itemHolder.v(R.id.lib_video_view)
        itemHolder.gone(R.id.play_view)

        videoView?.run {
            setVideoURI(uri)
            play()
            onPlayListener(this)
        }
    }
}