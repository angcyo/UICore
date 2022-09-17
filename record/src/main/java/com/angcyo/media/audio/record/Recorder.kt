/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.angcyo.media.audio.record

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.io.IOException

class Recorder(val mContext: Context, folderPath: String /*保存在那个文件夹*/) :
    OnCompletionListener, MediaPlayer.OnErrorListener {

    companion object {
        const val SAMPLE_DEFAULT_DIR = "/audio_recorder"

        /**
         * 录制闲置中
         */
        const val IDLE_STATE = 0

        /**
         * 录制中
         */
        const val RECORDING_STATE = 1

        /**
         * 播放中
         */
        const val PLAYING_STATE = 2

        /**
         * 播放暂停中
         */
        const val PLAYING_PAUSED_STATE = 3
        const val NO_ERROR = 0
        const val STORAGE_ACCESS_ERROR = 1
        const val INTERNAL_ERROR = 2
        const val IN_CALL_RECORD_ERROR = 3
        private const val SAMPLE_PREFIX = "recording"
        private const val SAMPLE_PATH_KEY = "sample_path"
        private const val SAMPLE_LENGTH_KEY = "sample_length"
    }

    private var mState = IDLE_STATE
    private var mOnStateChangedListener: OnStateChangedListener? = null
    private var mSampleStart: Long = 0 // time at which latest record or play
    private var mSampleLength = 0 // length of current sample

    // operation started
    private var mSampleFile: File? = null
    private var mSampleDir: File? = null
    private var mPlayer: MediaPlayer? = null

    /**
     * 需要播放的文件路径
     */
    var playFilePath: String? = null

    init {
        val sampleDir = File(folderPath)
        if (!sampleDir.exists()) {
            sampleDir.mkdirs()
        }
        mSampleDir = sampleDir
        syncStateWithService()
    }

    fun syncStateWithService(): Boolean {
        if (RecorderService.isRecording) {
            mState = RECORDING_STATE
            mSampleStart = RecorderService.startTime
            mSampleFile = File(RecorderService.filePath)
            return true
        } else if (mState == RECORDING_STATE) {
            // service is idle but local state is recording
            return false
        } else if (mSampleFile != null && mSampleLength == 0) {
            // this state can be reached if there is an incoming call
            // the record service is stopped by incoming call without notifying
            // the UI
            return false
        }
        return true
    }

    /**
     * 保存录制状态, 用于界面恢复
     */
    fun saveState(recorderState: Bundle) {
        recorderState.putString(SAMPLE_PATH_KEY, mSampleFile!!.absolutePath)
        recorderState.putInt(SAMPLE_LENGTH_KEY, mSampleLength)
    }

    /**
     * 恢复状态
     */
    fun restoreState(recorderState: Bundle) {
        val samplePath = recorderState.getString(SAMPLE_PATH_KEY) ?: return
        val sampleLength = recorderState.getInt(SAMPLE_LENGTH_KEY, -1)
        if (sampleLength == -1) {
            return
        }
        val file = File(samplePath)
        if (!file.exists()) {
            return
        }
        if (mSampleFile != null
            && mSampleFile!!.absolutePath.compareTo(file.absolutePath) == 0
        ) {
            return
        }
        delete()
        mSampleFile = file
        mSampleLength = sampleLength
        signalStateChanged(IDLE_STATE)
    }

    /**
     * 录制文件所在的目录
     */
    val recordDir: String
        get() = mSampleDir!!.absolutePath

    /**
     * 录制声音的振幅
     */
    val maxAmplitude: Int
        get() = if (mState != RECORDING_STATE) {
            0
        } else RecorderService.maxAmplitude

    /**
     * 设置事件监听
     */
    fun setOnStateChangedListener(listener: OnStateChangedListener?) {
        mOnStateChangedListener = listener
    }

    /**
     * 返回当前状态
     */
    fun state(): Int {
        return mState
    }

    /**
     * 返回录制时长 (秒)
     */
    fun progress(): Int {
        if (mState == RECORDING_STATE) {
            return ((System.currentTimeMillis() - mSampleStart) / 1000).toInt()
        } else if (mState == PLAYING_STATE || mState == PLAYING_PAUSED_STATE) {
            if (mPlayer != null) {
                return (mPlayer!!.currentPosition / 1000)
            }
        }
        return 0
    }

    /**
     * 播放进度 (0-1f)
     */
    fun playProgress(): Float {
        return if (mPlayer != null) {
            mPlayer!!.currentPosition.toFloat() / mPlayer!!.duration
        } else 0.0f
    }

    /**
     * 采样长度, 文件大小
     */
    fun sampleLength(): Int {
        return mSampleLength
    }

    /**
     * 录制文件
     */
    fun sampleFile(): File? {
        return mSampleFile
    }

    fun renameSampleFile(name: String) {
        if (mSampleFile != null && mState != RECORDING_STATE && mState != PLAYING_STATE) {
            if (!TextUtils.isEmpty(name)) {
                val oldName = mSampleFile!!.absolutePath
                val extension = oldName.substring(oldName.lastIndexOf('.'))
                val newFile =
                    File(mSampleFile!!.parent!! + "/" + name + extension)
                if (!TextUtils.equals(oldName, newFile.absolutePath)) {
                    if (mSampleFile!!.renameTo(newFile)) {
                        mSampleFile = newFile
                    }
                }
            }
        }
    }

    /**
     * Resets the recorder state. If a sample was recorded, the file is deleted.
     */
    fun delete() {
        stop()
        if (mSampleFile != null) {
            mSampleFile!!.delete()
        }
        mSampleFile = null
        mSampleLength = 0
        signalStateChanged(IDLE_STATE)
    }

    /**
     * Resets the recorder state. If a sample was recorded, the file is left on
     * disk and will be reused for a new recording.
     */
    fun clear() {
        stop()
        mSampleLength = 0
        signalStateChanged(IDLE_STATE)
    }

    fun reset() {
        stop()
        mSampleLength = 0
        mSampleFile = null
        mState = IDLE_STATE
        if (mSampleDir == null) {
            val sampleDir =
                File(Environment.getExternalStorageDirectory().absolutePath + SAMPLE_DEFAULT_DIR)
            if (!sampleDir.exists()) {
                sampleDir.mkdirs()
            }
            mSampleDir = sampleDir
        }
        signalStateChanged(IDLE_STATE)
    }

    fun isRecordExisted(path: String): Boolean {
        if (!TextUtils.isEmpty(path)) {
            val file = File(mSampleDir!!.absolutePath + "/" + path)
            return file.exists()
        }
        return false
    }

    /**
     * 开始录制
     */
    fun startRecording(
        outputFileFormat: Int,
        name: String,
        extension: String?,
        highQuality: Boolean /*质量*/,
        maxFileSize: Long /*最大文件大小*/
    ) {
        stop()
        if (mSampleFile == null) {
            try {
                mSampleFile = File.createTempFile(SAMPLE_PREFIX, extension, mSampleDir)
                renameSampleFile(name)
            } catch (e: IOException) {
                setError(STORAGE_ACCESS_ERROR)
                return
            }
        }
        RecorderService.startRecording(
            mContext, outputFileFormat, mSampleFile!!.absolutePath,
            highQuality, maxFileSize
        )
        mSampleStart = System.currentTimeMillis()
    }

    /**
     * 停止录制
     */
    fun stopRecording() {
        if (RecorderService.isRecording) {
            RecorderService.stopRecording(mContext)
            mSampleLength = ((System.currentTimeMillis() - mSampleStart) / 1000).toInt()
            if (mSampleLength == 0) {
                // round up to 1 second if it's too short
                mSampleLength = 1
            }
        }
    }

    /**
     * 开始播放
     */
    fun startPlayback(playPath: String?, percentage: Float) {
        if (TextUtils.isEmpty(playPath) || !File(playPath!!).exists()) {
            stop()
            return
        }
        if (TextUtils.equals(
                playFilePath,
                playPath
            ) && state() == PLAYING_PAUSED_STATE
        ) {
            mSampleStart = System.currentTimeMillis() - mPlayer!!.currentPosition
            mPlayer!!.seekTo((percentage * mPlayer!!.duration).toInt())
            mPlayer!!.start()
            setState(PLAYING_STATE)
        } else {
            stop()
            playFilePath = playPath
            mPlayer = MediaPlayer()
            try {
                mPlayer!!.setDataSource(playPath)
                mPlayer!!.setOnCompletionListener(this)
                mPlayer!!.setOnErrorListener(this)
                mPlayer!!.prepare()
                mPlayer!!.seekTo((percentage * mPlayer!!.duration).toInt())
                mPlayer!!.start()
            } catch (e: IllegalArgumentException) {
                setError(INTERNAL_ERROR)
                mPlayer = null
                return
            } catch (e: IOException) {
                setError(STORAGE_ACCESS_ERROR)
                mPlayer = null
                return
            }
            mSampleStart = System.currentTimeMillis()
            setState(PLAYING_STATE)
        }
    }

    /**
     * 暂停播放
     */
    fun pausePlayback() {
        if (mPlayer == null) {
            return
        }
        mPlayer!!.pause()
        setState(PLAYING_PAUSED_STATE)
    }

    /**
     * 停止播放
     */
    fun stopPlayback() {
        if (mPlayer == null) { // we were not in playback
            return
        }
        mPlayer!!.stop()
        mPlayer!!.release()
        mPlayer = null
        setState(IDLE_STATE)
    }

    /**
     * 停止录制, 停止播放
     */
    fun stop() {
        stopRecording()
        stopPlayback()
    }

    fun getSampleFile(): File? {
        return mSampleFile
    }

    override fun onError(
        mp: MediaPlayer,
        what: Int,
        extra: Int
    ): Boolean {
        stop()
        setError(STORAGE_ACCESS_ERROR)
        return true
    }

    override fun onCompletion(mp: MediaPlayer) {
        stop()
    }

    /**
     * 设置当前录制状态
     */
    fun setState(state: Int) {
        if (state == mState) {
            return
        }
        mState = state
        signalStateChanged(mState)
    }

    private fun signalStateChanged(state: Int) {
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener!!.onStateChanged(state)
        }
    }

    fun setError(error: Int) {
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener!!.onError(error)
        }
    }

    interface OnStateChangedListener {
        /**
         * 状态回调
         */
        fun onStateChanged(state: Int)

        /**
         * 录制失败回调
         */
        fun onError(error: Int)
    }
}