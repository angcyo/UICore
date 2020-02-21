package com.angcyo.media.dslitem

import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import com.angcyo.download.DslDownload
import com.angcyo.download.dslDownload
import com.angcyo.download.isCompleted
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.glide.giv
import com.angcyo.library.app
import com.angcyo.library.ex.fileUri
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.media.R
import com.angcyo.media.video.widget.TextureVideoView
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.Anim
import com.angcyo.widget.progress.HSProgressView
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/21
 */

class DslTextureVideoItem : DslAdapterItem() {

    /**视频地址*/
    var itemVideoUri: Uri? = null

    /**播放监听*/
    var onPlayListener: (TextureVideoView) -> Unit = {}

    var _downTask: DownloadTask? = null

    init {
        itemLayoutId = R.layout.dsl_texture_video_item

        onItemViewDetachedToWindow = {
            _downTask?.cancel()
            it.v<TextureVideoView>(R.id.lib_video_view)?.stop()
        }
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

//        if (videoView?.mCurrentState == TextureVideoView.STATE_IDLE) {
//            //加载封面
//            itemHolder.giv(R.id.lib_image_view)?.apply {
//                load(itemVideoUri)
//                alpha = 1f
//                visibility = View.VISIBLE
//            }
//            itemHolder.visible(R.id.play_view)
//        } else {
//            itemHolder.giv(R.id.lib_image_view)?.apply {
//                visibility = View.GONE
//            }
//            itemHolder.gone(R.id.play_view)
//        }

        //视频
        if (itemVideoUri == null) {
            return
        }

        videoView?.setMediaPlayerCallback(object : TextureVideoView.SimpleMediaPlayerCallback() {
            override fun onPrepared(mp: MediaPlayer?) {
                super.onPrepared(mp)
                onPlayStateChanged(mp, TextureVideoView.STATE_PLAYING)
            }

            override fun onCompletion(mp: MediaPlayer?) {
                super.onCompletion(mp)
                onPlayStateChanged(mp, TextureVideoView.STATE_PLAYBACK_COMPLETED)
            }

            override fun onPlayStateChanged(mp: MediaPlayer?, newState: Int) {
                super.onPlayStateChanged(mp, newState)
                if (newState == TextureVideoView.STATE_PLAYING) {
                    itemHolder.gone(R.id.hs_progress_view)
                    itemHolder.view(R.id.lib_image_view)?.run {
                        animate().alpha(0f)
                            .setDuration(Anim.ANIM_DURATION)
                            .withEndAction {
                                itemHolder.gone(R.id.lib_image_view)
                            }
                            .start()
                    }
                    itemHolder.gone(R.id.play_view)
                } else {
                    itemHolder.visible(R.id.play_view)
                }
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
                val url = itemVideoUri!!.path
                val task = DslDownload.findTask(url)
                if (task.isCompleted()) {
                    //视频已经下载好了
                    playVideo(itemHolder, fileUri(app(), task!!.file!!.absolutePath))
                } else {
                    //开始下载视频
                    itemHolder.v<HSProgressView>(R.id.hs_progress_view)?.apply {
                        visibility = View.VISIBLE
                        startAnimator()
                    }

                    downVideo(url!!) {
                        playVideo(itemHolder, fileUri(app(), it))
                    }
                }
            } else {
                //本地视频
                playVideo(itemHolder, itemVideoUri!!)
            }
        }

        //item 事件
        itemHolder.longClick(R.id.lib_video_view, _longClickListener)
    }

    /**下载视频*/
    open fun downVideo(url: String, callback: (path: String) -> Unit) {
        _downTask?.cancel()
        _downTask = dslDownload(url) {
            onTaskFinish = { downloadTask, cause, exception ->
                if (cause == EndCause.COMPLETED) {
                    callback(downloadTask.file!!.absolutePath)
                }
            }
        }
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