package com.angcyo.media.video.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.Uri
import android.os.*
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import com.angcyo.library.L
import com.angcyo.library.ex.isDebug
import java.io.IOException

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class TextureVideoView : TextureView, SurfaceTextureListener, Handler.Callback,
    OnPreparedListener, OnVideoSizeChangedListener, OnCompletionListener,
    OnErrorListener, OnInfoListener, OnBufferingUpdateListener,
    OnSeekCompleteListener {

    companion object {
        const val STATE_ERROR = -1
        const val STATE_IDLE = 0
        const val STATE_PREPARING = 1
        const val STATE_PREPARED = 2
        const val STATE_PLAYING = 3
        const val STATE_PAUSED = 4
        const val STATE_PLAYBACK_COMPLETED = 5
        const val STATE_STOP = 6
        private const val TAG = "TextureVideoView"
        private var SHOW_LOGS = isDebug()
        private const val MSG_START = 0x0001
        private const val MSG_PAUSE = 0x0004
        private const val MSG_STOP = 0x0006
        private const val MSG_VIDEO_PROGRESS = 0x0010
        private val sThread = HandlerThread("VideoPlayThread")

        init {
            sThread.start()
        }
    }

    var mScalableType = ScalableType.CENTER_CROP

    @Volatile
    var currentState = STATE_IDLE

    @Volatile
    var targetState = STATE_IDLE
    private var mUri: Uri? = null
    private var mContext: Context? = null
    private var mSurface: Surface? = null
    var mediaPlayer: MediaPlayer? = null
    private val mAudioManager: AudioManager? = null
    private var playerCallback: MediaPlayerCallback? = null
    private lateinit var mainHandler: Handler
    private var videoThreadHandler: Handler? = null
    var isMute = false
    var isHasAudio = false
    private var repeatPlay = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mContext = context
        currentState = STATE_IDLE
        targetState = STATE_IDLE
        mainHandler = Handler(Looper.getMainLooper())
        if (!isInEditMode) {
            videoThreadHandler = Handler(sThread.looper, this)
            surfaceTextureListener = this
        }
    }

    fun setMediaPlayerCallback(mediaPlayerCallback: MediaPlayerCallback?) {
        playerCallback = mediaPlayerCallback
        if (mediaPlayerCallback == null) {
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        synchronized(TextureVideoView::class.java) {
            when (msg.what) {
                MSG_START -> {
                    if (SHOW_LOGS) Log.i(TAG, "<< handleMessage init")
                    openVideo()
                    if (SHOW_LOGS) Log.i(TAG, ">> handleMessage init")
                }
                MSG_PAUSE -> {
                    if (SHOW_LOGS) Log.i(TAG, "<< handleMessage pause")
                    mediaPlayer?.pause()
                    currentState = STATE_PAUSED
                    if (SHOW_LOGS) Log.i(TAG, ">> handleMessage pause")
                }
                MSG_STOP -> {
                    if (SHOW_LOGS) Log.i(TAG, "<< handleMessage stop")
                    release(true)
                    if (SHOW_LOGS) Log.i(TAG, ">> handleMessage stop")
                }
                MSG_VIDEO_PROGRESS ->
                    //if (SHOW_LOGS) Log.e(TAG, "______________MSG_VIDEO_PROGRESS")
                    if (playerCallback != null && mediaPlayer != null) {
                        mainHandler.post {
                            try {
                                if (playerCallback != null && mediaPlayer != null) {
                                    if (currentState == STATE_PLAYBACK_COMPLETED) {
                                        playerCallback!!.onVideoPlayProgress(
                                            mediaPlayer!!,
                                            mediaPlayer!!.duration,
                                            mediaPlayer!!.duration
                                        )
                                    } else {
                                        playerCallback!!.onVideoPlayProgress(
                                            mediaPlayer!!,
                                            mediaPlayer!!.currentPosition,
                                            mediaPlayer!!.duration
                                        )
                                        if (currentState == STATE_PLAYING) {
                                            videoThreadHandler!!.sendEmptyMessageDelayed(
                                                MSG_VIDEO_PROGRESS,
                                                300
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                else -> {
                }
            }
        }
        return true
    }

    // release the media player in any state
    private fun release(clearTargetState: Boolean) {
        if (mediaPlayer != null) {
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
            currentState = STATE_IDLE
            if (clearTargetState) {
                targetState = STATE_IDLE
            }
            //mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_LOSS);
            //AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            //am.abandonAudioFocus(null);
        }
    }

    private fun openVideo() {
        if (mUri == null || mSurface == null || targetState != STATE_PLAYING) { // not ready for playback just yet, will try again later
            return
        }
        //mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false)
        try {
            mediaPlayer = MediaPlayer().apply {
                reset()
                setOnPreparedListener(this@TextureVideoView)
                setOnVideoSizeChangedListener(this@TextureVideoView)
                setOnCompletionListener(this@TextureVideoView)
                setOnErrorListener(this@TextureVideoView)
                setOnInfoListener(this@TextureVideoView)
                setOnBufferingUpdateListener(this@TextureVideoView)
                setOnSeekCompleteListener(this@TextureVideoView)
                setDataSource(mContext!!, mUri!!)
                setSurface(mSurface)
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                prepareAsync()
                if (isMute) {
                    setVolume(0f, 0f)
                }
            }
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            currentState = STATE_PREPARING
            targetState = STATE_PREPARING
            isHasAudio = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                try {
                    val mediaExtractor = MediaExtractor()
                    mediaExtractor.setDataSource(mContext!!, mUri!!, null)
                    var format: MediaFormat
                    for (i in 0 until mediaExtractor.trackCount) {
                        format = mediaExtractor.getTrackFormat(i)
                        val mime = format.getString(MediaFormat.KEY_MIME)
                        if (mime?.startsWith("audio/") == true) {
                            isHasAudio = true
                            break
                        }
                    }
                } catch (ex: Exception) { // may be failed to instantiate extractor.
                }
            }
        } catch (ex: IOException) {
            if (SHOW_LOGS) Log.w(TAG, "Unable to open content: $mUri", ex)
            currentState = STATE_ERROR
            targetState = STATE_ERROR
            mainHandler.post {
                playerCallback?.onError(mediaPlayer!!, MEDIA_ERROR_UNKNOWN, 0)
            }
        } catch (ex: IllegalArgumentException) {
            if (SHOW_LOGS) Log.w(TAG, "Unable to open content: $mUri", ex)
            currentState = STATE_ERROR
            targetState = STATE_ERROR
            mainHandler.post {
                playerCallback?.onError(mediaPlayer!!, MEDIA_ERROR_UNKNOWN, 0)
            }
        } catch (ex: Exception) {
            L.w(ex)
        }
    }

    override fun onSurfaceTextureAvailable(
        surface: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        mSurface = Surface(surface)
        if (targetState == STATE_PLAYING) {
            if (SHOW_LOGS) Log.i(TAG, "onSurfaceTextureAvailable start")
            start()
        }
    }

    override fun onSurfaceTextureSizeChanged(
        surface: SurfaceTexture,
        width: Int,
        height: Int
    ) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        mSurface = null
        stop()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    fun setVideoPath(path: String?) {
        setVideoURI(Uri.parse(path))
    }

    fun setVideoURI(uri: Uri) {
        if (SHOW_LOGS) Log.i(TAG, "setVideoURI $uri")
        mUri = uri
    }

    fun start() {
        targetState = STATE_PLAYING
        if (isInPlaybackState) {
            videoThreadHandler?.obtainMessage(MSG_STOP)?.sendToTarget()
        }
        if (mUri != null && mSurface != null) {
            videoThreadHandler?.obtainMessage(MSG_START)?.sendToTarget()
        }
        mainHandler.post { playerCallback?.onPlayStateChanged(mediaPlayer, STATE_PLAYING) }
    }

    fun pause() {
        targetState = STATE_PAUSED
        if (isPlaying) {
            videoThreadHandler?.obtainMessage(MSG_PAUSE)?.sendToTarget()
        }
        mainHandler.post {
            playerCallback?.onPlayStateChanged(mediaPlayer, STATE_PAUSED)
        }
    }

    fun resume() {
        if (targetState == STATE_PAUSED) {
            startInner()
        } else {
            if (!isPlaying) {
                videoThreadHandler?.obtainMessage(MSG_START)?.sendToTarget()
                mainHandler.post {
                    playerCallback?.onPlayStateChanged(mediaPlayer, STATE_PLAYING)
                }
            }
        }
        targetState = STATE_PLAYING
    }

    fun stop() {
        targetState = STATE_STOP
        if (isInPlaybackState) {
            videoThreadHandler?.obtainMessage(MSG_STOP)?.sendToTarget()
        }
        mainHandler.post {
            playerCallback?.onPlayStateChanged(mediaPlayer, STATE_STOP)
        }
    }

    val isPlaying: Boolean
        get() {
            var isPlaying = false
            try {
                if (mediaPlayer != null) {
                    isPlaying = mediaPlayer!!.isPlaying
                }
            } catch (e: IllegalStateException) {
                mediaPlayer = null
                mediaPlayer = MediaPlayer()
            }
            return isInPlaybackState && isPlaying
        }

    /**[fraction]比例*/
    fun seekToFraction(fraction: Float) {
        seekTo((fraction * (mediaPlayer?.duration ?: 1)).toInt())
    }

    fun seekTo(msec: Int) {
        videoThreadHandler?.post { mediaPlayer?.seekTo(msec) }
    }

    fun mute() {
        isMute = true
        mediaPlayer?.setVolume(0f, 0f)
    }

    fun unMute() {
        if (mAudioManager != null && mediaPlayer != null) {
            val max = 100
            val audioVolume = 100
            val numerator: Double =
                if (max - audioVolume > 0) Math.log(max - audioVolume.toDouble()) else 0.0
            val volume =
                (1 - numerator / Math.log(max.toDouble())).toFloat()
            mediaPlayer?.setVolume(volume, volume)
            isMute = false
        }
    }

    private val isInPlaybackState: Boolean
        get() = mediaPlayer != null &&
                currentState != STATE_ERROR &&
                currentState != STATE_IDLE &&
                currentState != STATE_PLAYBACK_COMPLETED &&
                currentState != STATE_PREPARING

    /**
     * 播放完是否重复播放
     */
    fun setRepeatPlay(repeatPlay: Boolean) {
        this.repeatPlay = repeatPlay
    }

    private fun startInner() {
        mediaPlayer?.run {
            currentState = STATE_PLAYING
            videoThreadHandler?.obtainMessage(MSG_VIDEO_PROGRESS)?.sendToTarget()
            start()

            mainHandler.post { playerCallback?.onPlayStateChanged(this, STATE_PLAYING) }
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        currentState = STATE_PLAYBACK_COMPLETED
        targetState = STATE_PLAYBACK_COMPLETED
        if (mediaPlayer != null) {
            mainHandler.post {
                if (repeatPlay) {
                    startInner()
                }
                playerCallback?.onCompletion(mp)
            }
        }
    }

    override fun onSeekComplete(mp: MediaPlayer) {
        if (SHOW_LOGS) Log.e(TAG, "______________onSeekComplete")
        startInner()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        if (SHOW_LOGS) Log.e(
            TAG,
            "onError() called with mp = [$mp], what = [$what], extra = [$extra]"
        )
        currentState = STATE_ERROR
        targetState = STATE_ERROR
        if (playerCallback != null) {
            mainHandler.post {
                if (playerCallback != null) {
                    playerCallback!!.onError(mp, what, extra)
                }
            }
        }
        return true
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (SHOW_LOGS) Log.i(TAG, "onPrepared " + mUri.toString())
        if (targetState != STATE_PREPARING || currentState != STATE_PREPARING) {
            return
        }
        currentState = STATE_PREPARED
        if (isInPlaybackState) {
            if (playerCallback != null) {
                val videoStartPlayProgress = 0
                if (SHOW_LOGS) Log.i(TAG, "视频断点播放进度: $videoStartPlayProgress")
                if (videoStartPlayProgress < 0) {
                    mediaPlayer?.start()
                } else {
                    mediaPlayer?.seekTo(videoStartPlayProgress)
                }
            } else {
                mediaPlayer?.start()
            }
            currentState = STATE_PLAYING
            targetState = STATE_PLAYING
        }
//        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//            @Override
//            public void onSeekComplete(MediaPlayer mp) {
//                if(!isPlaying()){
//                    resume();
//                }
//            }
//        });
        mainHandler.post {
            playerCallback?.onPrepared(mp)
        }
    }

    override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
        if (playerCallback != null) {
            mainHandler.post {
                if (playerCallback != null) {
                    playerCallback!!.onVideoSizeChanged(mp, width, height)
                    scaleVideoSize(width, height)
                }
            }
        }
    }

    private fun scaleVideoSize(videoWidth: Int, videoHeight: Int) {
        if (videoWidth == 0 || videoHeight == 0) {
            return
        }
        val viewSize =
            Size(width, height)
        val videoSize =
            Size(videoWidth, videoHeight)
        val scaleVideo = ScaleVideo(viewSize, videoSize)
        val matrix = scaleVideo.getScaleMatrix(mScalableType)
        matrix?.let { setTransform(it) }
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        if (playerCallback != null) {
            mainHandler.post {
                if (playerCallback != null) {
                    playerCallback!!.onBufferingUpdate(mp, percent)
                }
            }
        }
    }

    override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        if (playerCallback != null) {
            mainHandler.post {
                if (playerCallback != null) {
                    playerCallback!!.onInfo(mp, what, extra)
                }
            }
        }
        return true
    }

    override fun onVisibilityChanged(
        changedView: View,
        visibility: Int
    ) {
        super.onVisibilityChanged(changedView, visibility)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release(true)
        if (isPlaying) {
            playerCallback?.onPlayStateChanged(mediaPlayer, STATE_PLAYBACK_COMPLETED)
        }
    }

    fun setScaletype(scalableType: ScalableType) {
        mScalableType = scalableType
    }

    /**智能播放*/
    fun play() {
        when {
            mediaPlayer == null -> start()
            targetState == STATE_PAUSED -> resume()
            else -> start()
        }
    }

    interface MediaPlayerCallback {
        fun onPrepared(mp: MediaPlayer)
        fun onCompletion(mp: MediaPlayer)
        fun onBufferingUpdate(mp: MediaPlayer, percent: Int)
        fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int)
        fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean
        fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean
        fun onSeekComplete(mp: MediaPlayer)
        fun onVideoPlayProgress(mp: MediaPlayer, progress: Int, duration: Int /*毫秒*/)
        fun onPlayStateChanged(mp: MediaPlayer?, newState: Int)
    }

    abstract class SimpleMediaPlayerCallback : MediaPlayerCallback {
        override fun onPrepared(mp: MediaPlayer) {}
        override fun onCompletion(mp: MediaPlayer) {}
        override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {}
        override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
        }

        override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            return false
        }

        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            return false
        }

        override fun onSeekComplete(mp: MediaPlayer) {}

        override fun onVideoPlayProgress(mp: MediaPlayer, progress: Int, duration: Int) {
            L.d("$mp $progress:$duration")
        }

        override fun onPlayStateChanged(mp: MediaPlayer?, newState: Int) {}
    }
}