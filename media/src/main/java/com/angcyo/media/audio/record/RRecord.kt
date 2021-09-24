package com.angcyo.media.audio.record

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import java.io.File

/**
 * Android 手机录音工具类
 *
 *
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/01/23
 * Copyright (c) 2019 Shenzhen O&M Cloud Co., Ltd. All rights reserved.
 */
class RRecord(context: Context, folderPath: String /*保存在那个文件夹*/) {

    companion object {
        const val BITRATE_AMR = 2 * 1024 * 8 // bits/sec
        const val BITRATE_3GPP = 20 * 1024 * 8 // bits/sec
        private const val FILE_EXTENSION_AMR = ".amr"
        private const val FILE_EXTENSION_3GPP = ".3gpp"

        /**
         * 请求拿到音频焦点
         */
        private fun requestAudioFocus(context: Context) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) //请求焦点
        }

        /**
         * 释放音频焦点
         */
        private fun abandonAudioFocus(context: Context) {
            val audioManager =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.abandonAudioFocus(null) //放弃焦点
        }
    }

    var innerRecorder: Recorder = Recorder(context.applicationContext, folderPath)

    /**
     * 默认最大录制文件大小
     */
    var mMaxFileSize = 5 * 1024 * 1024
    var mReceiver: RecorderReceiver
    var context: Context = context.applicationContext
    var onRecordListener: OnRecordListener? = null
    var mainHandler = Handler(Looper.getMainLooper())

    /**高质量录制, [amr or 3gpp]*/
    var isHighQuality = false

    /**
     * 进度回调检查
     */
    var checkProgressRunnable: Runnable = object : Runnable {
        override fun run() {
            val state = innerRecorder.state()
            var doNext = false
            if (state == Recorder.RECORDING_STATE) {
                doNext = true
                //录制进度
                onRecordListener?.onRecordProgress(innerRecorder.progress())
                onRecordListener?.onRecordMaxAmplitude(innerRecorder.maxAmplitude)
            } else if (state == Recorder.PLAYING_STATE) {
                doNext = true
                //播放进度
                onRecordListener?.onPlayProgress(
                    innerRecorder.progress(),
                    innerRecorder.playProgress()
                )
            }
            if (doNext) {
                mainHandler.postDelayed(this, 100)
            }
        }
    }

    init {
        innerRecorder.setOnStateChangedListener(object : Recorder.OnStateChangedListener {
            override fun onStateChanged(state: Int) {
                if (onRecordListener != null) {
                    onRecordListener!!.onStateChanged(state)
                    when (state) {
                        Recorder.RECORDING_STATE -> {
                            onRecordListener!!.onRecordStart()
                            checkProgress()
                        }
                        Recorder.PLAYING_STATE -> {
                            onRecordListener!!.onPlayStart()
                            checkProgress()
                        }
                        Recorder.IDLE_STATE -> {
                            onRecordListener!!.onRecordEnd()
                            onRecordListener!!.onPlayEnd()
                        }
                    }
                }
            }

            override fun onError(error: Int) {
                if (onRecordListener != null) {
                    onRecordListener!!.onError(error)
                }
            }
        })
        mReceiver = RecorderReceiver()
        val filter = IntentFilter()
        filter.addAction(RecorderService.RECORDER_SERVICE_BROADCAST_NAME)
        this.context.registerReceiver(mReceiver, filter)
    }

    /**
     * 开始录制, 需要注册 RecorderService 服务哦
     *
     * @param fileName 不包括后缀名
     * @see RecorderService
     */
    fun startRecord(fileName: String): RRecord {
        val state = innerRecorder.state()
        if (state != Recorder.IDLE_STATE) {
            return this
        }

        //重置 2019-4-13
        innerRecorder.reset()
        requestAudioFocus(context)
        val outputFileFormat =
            if (isHighQuality) MediaRecorder.OutputFormat.AMR_WB else MediaRecorder.OutputFormat.THREE_GPP
        //if (isHighQuality) MediaRecorder.OutputFormat.AMR_WB else MediaRecorder.OutputFormat.AMR_NB
        val fileExt = if (isHighQuality) FILE_EXTENSION_AMR else FILE_EXTENSION_3GPP
        innerRecorder.startRecording(
            outputFileFormat,
            fileName,
            fileExt,
            isHighQuality,
            mMaxFileSize.toLong()
        )
        return this
    }

    /**
     * 停止录制
     */
    fun stopRecord(): RRecord {
        abandonAudioFocus(context)
        innerRecorder.stopRecording()
        return this
    }

    /**
     * 释放资源
     */
    fun release(): RRecord {
        abandonAudioFocus(context)
        innerRecorder.stop()
        context.unregisterReceiver(mReceiver)
        return this
    }

    /**
     * 开始回放
     */
    fun startPlayback(playPath: String?, percentage: Float): RRecord {
        requestAudioFocus(context)
        innerRecorder.startPlayback(playPath, percentage)
        return this
    }

    /**
     * 结束回放
     */
    fun stopPlayback(): RRecord {
        abandonAudioFocus(context)
        innerRecorder.stopPlayback()
        return this
    }

    fun setOnRecordListener(onRecordListener: OnRecordListener?): RRecord {
        this.onRecordListener = onRecordListener
        return this
    }

    /**
     * 返回当前保存的录音文件
     */
    val sampleFile: File?
        get() = innerRecorder.getSampleFile()

    /**
     * 回放文件路径
     */
    val playFilePath: String?
        get() = innerRecorder.playFilePath

    private fun checkProgress() {
        mainHandler.removeCallbacks(checkProgressRunnable)
        mainHandler.post(checkProgressRunnable)
    }

    val maxAmplitude: Int
        get() = innerRecorder.maxAmplitude

    abstract class OnRecordListener :
        Recorder.OnStateChangedListener {
        override fun onStateChanged(state: Int) {}
        override fun onError(error: Int) {}
        fun onRecordStart() {}
        fun onRecordMaxAmplitude(maxAmplitude: Int) {}
        fun onRecordEnd() {}

        /**
         * 录制进度 (秒)
         */
        fun onRecordProgress(time: Int) {}
        fun onPlayStart() {}
        fun onPlayEnd() {}

        /**
         * 返回播放时长, 和进度比例
         *
         * @param time 秒
         */
        fun onPlayProgress(time: Int, progress: Float) {}
    }

    inner class RecorderReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.hasExtra(RecorderService.RECORDER_SERVICE_BROADCAST_STATE)) {
                val isRecording = intent.getBooleanExtra(
                    RecorderService.RECORDER_SERVICE_BROADCAST_STATE, false
                )
                innerRecorder.setState(if (isRecording) Recorder.RECORDING_STATE else Recorder.IDLE_STATE)
            } else if (intent.hasExtra(RecorderService.RECORDER_SERVICE_BROADCAST_ERROR)) {
                val error =
                    intent.getIntExtra(RecorderService.RECORDER_SERVICE_BROADCAST_ERROR, 0)
                innerRecorder.setError(error)
            }
        }
    }
}