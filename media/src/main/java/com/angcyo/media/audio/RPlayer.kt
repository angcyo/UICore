package com.angcyo.media.audio

import android.media.AudioManager
import android.media.MediaPlayer
import android.text.TextUtils
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex.abandonAudioFocus
import com.angcyo.library.ex.requestAudioFocus
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2017/10/25 16:20
 */
class RPlayer {
    private var mediaPlay: MediaPlayer? = null

    /**是否循环播放*/
    var isLoop = false

    var onPlayListener: OnPlayerListener? = null

    var audioStreamType = AudioManager.STREAM_MUSIC

    var leftVolume: Float = 0.5f
    var rightVolume: Float = 0.5f

    /**正在播放的url, 播放完成后, 会被置空*/
    var _playingUrl = ""

    /**播放的url, 正常播放过的url*/
    var playUrl = ""

    var onAudioFocusChange: (focusChange: Int) -> Unit = {
        when (it) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                // Pause playback immediately
                //焦点丢失,停止播放
                stopPlay()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause playback
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, keep playing
                //保持播放, 减少音量
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Your app has been granted audio focus again
                // Raise volume to normal, restart playback if necessary
                //重新获得焦点
            }
        }
    }

    /**当前播放的状态*/
    private var playState: AtomicInteger = AtomicInteger(STATE_INIT)

    companion object {
        //初始化状态
        const val STATE_INIT = 0

        /**正常情况*/
        const val STATE_NORMAL = 1

        /**播放中*/
        const val STATE_PLAYING = 2

        /**停止播放*/
        const val STATE_STOP = 3

        /**资源释放*/
        const val STATE_RELEASE = 4

        const val STATE_PAUSE = 5

        /**播放完成*/
        const val STATE_COMPLETION = 6
        const val STATE_ERROR = -1

        fun stateString(state: Int): String {
            return when (state) {
                STATE_INIT -> "STATE_INIT"
                STATE_NORMAL -> "STATE_NORMAL"
                STATE_PLAYING -> "STATE_PLAYING"
                STATE_STOP -> "STATE_STOP"
                STATE_RELEASE -> "STATE_RELEASE"
                STATE_PAUSE -> "STATE_PAUSE"
                STATE_COMPLETION -> "STATE_COMPLETION"
                STATE_ERROR -> "STATE_ERROR"
                else -> "UNKNOWN"
            }
        }
    }

    private var seekToPosition = -1

    @Synchronized
    fun init() {
        if (mediaPlay == null) {
            mediaPlay = MediaPlayer()
        }
    }

    //开始播放
    private fun startPlayInner(mediaPlay: MediaPlayer) {
        mediaPlay.checkStart {
            setPlayState(STATE_PLAYING)
            startProgress()
        }
    }

    /**@param url 可以有效的网络, 和有效的本地地址*/
    fun startPlay(url: String) {
        if (_playingUrl == url) {
            if (isPlayCall()) {

            } else {
                mediaPlay?.let {
                    startPlayInner(it)
                }
            }
            return
        } else {
            if (playState.get() != STATE_INIT) {
                stopPlay()
            }
        }
        if (mediaPlay == null) {
            init()
        }
        mediaPlay?.let {
            it.isLooping = isLoop
            it.setAudioStreamType(audioStreamType)
            it.setVolume(leftVolume, rightVolume)

            it.setOnErrorListener { mp, what, extra ->
                //L.e("call: startPlay -> $what $extra")
                setPlayState(STATE_ERROR)
                onPlayListener?.onPlayError(what, extra)

                it.reset()
                true
            }
            it.setOnCompletionListener {
                setPlayState(STATE_COMPLETION)
                onPlayListener?.onPlayCompletion(it.duration)
                it.reset()
            }
            it.setOnPreparedListener {
                //L.e("call: startPlay -> onPrepared ${it.duration}")
                onPlayListener?.onPreparedCompletion(it.duration)
                if (playState.get() == STATE_NORMAL) {
                    //startPlayInner(it)
                    playSeekTo(max(seekToPosition, 0))
                }
            }
            it.setOnSeekCompleteListener {
                startPlayInner(it)
            }
            it.setDataSource(url)
            _playingUrl = url
            playUrl = url

            setPlayState(STATE_NORMAL)
            it.prepareAsync()
        }
    }

    /**停止播放, 不释放资源, 下次可以重新setDataSource*/
    fun stopPlay() {

        mediaPlay?.let {
            if (isPlaying() && it.isPlaying) {
                it.stop()
            }
            it.reset()
        }

        setPlayState(STATE_STOP)
    }

    /**
     * 暂停播放
     * */
    fun pausePlay() {

        mediaPlay?.let {
            if (isPlaying()) {
                it.pause()
            }
        }

        setPlayState(STATE_PAUSE)
    }

    /**
     * 恢复播放
     * */
    fun resumePlay() {
        mediaPlay?.let {
            if (isPause()) {
                it.checkStart {
                    setPlayState(STATE_PLAYING)
                }
            }
        }
    }

    /**获取音频焦点*/
    fun MediaPlayer.checkStart(action: () -> Unit = {}) {
        app().requestAudioFocus(audioStreamType, onAudioFocusChange = onAudioFocusChange).apply {
            if (this == AudioManager.AUDIOFOCUS_GAIN) {
                //获取焦点
                start()
                action()
            }
        }
    }

    /**
     * 重新播放
     * */
    fun replay() {
        _playingUrl = ""
        startPlay(playUrl)
    }

    /**释放资源, 下次需要重新创建*/
    fun release() {
        setPlayState(STATE_RELEASE)
        stopPlay()
        mediaPlay?.let {
            it.release()
        }
        mediaPlay = null
    }

    /**设置音量*/
    fun setVolume(value: Float) {
        leftVolume = value
        rightVolume = value
        mediaPlay?.let {
            it.setVolume(value, value)
        }
    }

    /**
     * 多次点击同一视图,自动处理 暂停/恢复/播放
     * */
    fun click(url: String? = null) {
        if (isPlaying()) {
            pausePlay()
        } else if (isPause()) {
            resumePlay()
        } else {
            if (!TextUtils.isEmpty(url)) {
                startPlay(url!!)
            }
        }
    }

    /**正在播放中, 解析也完成了*/
    fun isPlaying() = playState.get() == STATE_PLAYING

    /**是否调用了播放, 但是有可能还在解析数据中*/
    fun isPlayCall() = (playState.get() == STATE_PLAYING || playState.get() == STATE_NORMAL)

    fun isPause() = playState.get() == STATE_PAUSE

    fun playState() = playState.get()

    private fun setPlayState(state: Int) {
        playUrl = _playingUrl

        val oldState = playState.get()
        playState.set(state)

        when (state) {
            STATE_STOP, STATE_RELEASE, STATE_ERROR, STATE_COMPLETION -> {
                _playingUrl = ""
                app().abandonAudioFocus(onAudioFocusChange)
            }
        }

        L.i("RPlayer: onPlayStateChange -> ${stateString(oldState)}->${stateString(state)}")

        if (oldState != state) {
            onPlayListener?.onPlayStateChange(playUrl, oldState, state)
        }
    }

    /**播放中的进度, 毫秒*/
    var currentPosition = 0

    /**媒体时长, 毫秒*/
    val duration: Int get() = mediaPlay?.duration ?: -1

    /*开始进度读取*/
    private fun startProgress() {
        Thread(Runnable {
            while ((isPlayCall() || isPause()) &&
                mediaPlay != null &&
                onPlayListener != null
            ) {
                MainExecutor.execute {
                    if (isPlaying() && mediaPlay != null) {
                        currentPosition = mediaPlay!!.currentPosition
                        L.d("RPlayer: startProgress -> $currentPosition:${mediaPlay!!.duration}")
                        onPlayListener?.onPlayProgress(currentPosition, mediaPlay!!.duration)
                    }
                }
                try {
                    Thread.sleep(300)
                } catch (e: Exception) {
                }
            }
        }).apply {
            start()
        }
    }

    interface OnPlayerListener {
        /**@param duration 媒体总时长 毫秒*/
        fun onPreparedCompletion(duration: Int)

        /**
         * 播放进度回调, 毫秒
         * @param progress 当前播放多少毫秒
         * @param duration 总共多少毫秒
         * */
        fun onPlayProgress(progress: Int, duration: Int)

        /**播放完成, 毫秒*/
        fun onPlayCompletion(duration: Int)

        /**播放错误*/
        fun onPlayError(what: Int, extra: Int)

        /**播放状态回调*/
        fun onPlayStateChange(playUrl: String, from: Int, to: Int)

    }

    /**[fraction]比例*/
    fun playSeekToFraction(fraction: Float) {
        playSeekTo((fraction * (mediaPlay?.duration ?: 1)).toInt())
    }

    fun playSeekTo(msec: Int /*毫秒*/) {
        seekToPosition = msec
        if (msec >= 0 /*&& playState.get() == STATE_PLAYING*/) {
            mediaPlay?.let {
                it.seekTo(msec)
                seekToPosition = -1
            }
        }
    }
}

