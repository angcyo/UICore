package com.angcyo.media.dslitem

import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.glide.giv
import com.angcyo.library.app
import com.angcyo.library.ex.fileUri
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.media.MediaProgressHelper
import com.angcyo.media.R
import com.angcyo.media.video.widget.TextureVideoView
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.Anim
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/21
 */

class DslTextureVideoItem : DslBaseDownloadItem() {

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
        itemHolder.giv(R.id.lib_image_view)?.apply {
            load(itemVideoUri)
            alpha = 1f
            visibility = View.VISIBLE
        }
        itemHolder.visible(R.id.play_view)
        MediaProgressHelper.resetLayout(itemHolder) { value, fraction ->
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
                MediaProgressHelper.showMediaProgressView(
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
                videoView?.resume()
            }
        }

        itemHolder.click(R.id.lib_image_view) {
            itemHolder.clickView(R.id.play_view)
        }

        //播放视频
        itemHolder.click(R.id.play_view) {
            if (itemVideoUri == null) {
                return@click
            }

            itemHolder.gone(R.id.play_view)

            if (itemVideoUri.isHttpScheme()) {
                //下载视频
                download(itemHolder, itemVideoUri?.path) {
                    playVideo(itemHolder, fileUri(app(), it))
                }
            } else {
                //本地视频
                playVideo(itemHolder, itemVideoUri!!)
            }
        }

        //item 事件
        itemHolder.longClick(R.id.lib_video_view, _longClickListener)
    }

    override fun onItemViewDetachedToWindow(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewDetachedToWindow(itemHolder, itemPosition)
        itemHolder.v<TextureVideoView>(R.id.lib_video_view)?.stop()
    }

    override fun onDownloadStart(itemHolder: DslViewHolder?, task: DownloadTask) {
        super.onDownloadStart(itemHolder, task)
        //开始下载视频
        MediaProgressHelper.showMediaLoadingView(itemHolder)
    }

    override fun onDownloadFinish(
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) {
        super.onDownloadFinish(itemHolder, task, cause, error)
        //下载完成
        MediaProgressHelper.showMediaLoadingView(itemHolder, false)
    }

    /**播放视频*/
    open fun playVideo(itemHolder: DslViewHolder, uri: Uri) {
        val videoView: TextureVideoView? = itemHolder.v(R.id.lib_video_view)
        itemHolder.gone(R.id.play_view)

        videoView?.run {
            setVideoURI(uri)

            if (targetState == TextureVideoView.STATE_PAUSED) {
                resume()
            } else {
                start()
            }

            onPlayListener(this)
        }
    }
}