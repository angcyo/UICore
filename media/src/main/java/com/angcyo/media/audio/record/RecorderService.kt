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

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.angcyo.library.utils.folderPath
import java.io.File
import java.io.IOException

/**
 * https://github.com/angcyo/RecordDemo
 * https://github.com/WaytoIns/SoundRecorder
 * https://github.com/MiCode/SoundRecorder
 */
class RecorderService : Service(), MediaRecorder.OnErrorListener {

    companion object {
        const val ACTION_NAME = "action_type"
        const val ACTION_INVALID = 0
        const val ACTION_START_RECORDING = 1
        const val ACTION_STOP_RECORDING = 2
        const val ACTION_ENABLE_MONITOR_REMAIN_TIME = 3
        const val ACTION_DISABLE_MONITOR_REMAIN_TIME = 4
        const val ACTION_PARAM_FORMAT = "format"
        const val ACTION_PARAM_PATH = "path"
        const val ACTION_PARAM_HIGH_QUALITY = "high_quality"
        const val ACTION_PARAM_MAX_FILE_SIZE = "max_file_size"
        const val RECORDER_SERVICE_BROADCAST_NAME = "com.android.soundrecorder.broadcast"
        const val RECORDER_SERVICE_BROADCAST_STATE = "is_recording"
        const val RECORDER_SERVICE_BROADCAST_ERROR = "error_code"
        const val NOTIFICATION_ID = 62343234

        private var mediaRecorder: MediaRecorder? = null

        var filePath: String? = null
        var startTime: Long = 0
        val isRecording: Boolean
            get() = mediaRecorder != null

        fun startRecording(
            context: Context,
            outputFileFormat: Int,
            path: String?,
            highQuality: Boolean,
            maxFileSize: Long
        ) {
            val intent = Intent(context, RecorderService::class.java)
            intent.putExtra(ACTION_NAME, ACTION_START_RECORDING)
            intent.putExtra(ACTION_PARAM_FORMAT, outputFileFormat)
            intent.putExtra(ACTION_PARAM_PATH, path)
            intent.putExtra(ACTION_PARAM_HIGH_QUALITY, highQuality)
            intent.putExtra(ACTION_PARAM_MAX_FILE_SIZE, maxFileSize)
            context.startService(intent)
        }

        fun stopRecording(context: Context) {
            val intent = Intent(context, RecorderService::class.java)
            intent.putExtra(ACTION_NAME, ACTION_STOP_RECORDING)
            context.startService(intent)
        }

        val maxAmplitude: Int
            get() = if (mediaRecorder == null) 0 else mediaRecorder!!.maxAmplitude
    }

    private val mHandler = Handler()
    private var mRemainingTimeCalculator: RemainingTimeCalculator? = null
    private var notifyManager: NotificationManager? = null
    private var mLowStorageNotification: Notification? = null
    private var telephonyManager: TelephonyManager? = null
    private var mWakeLock: WakeLock? = null
    private var mKeyguardManager: KeyguardManager? = null
    private var mNeedUpdateRemainingTime = false
    private val mPhoneStateListener: PhoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state != TelephonyManager.CALL_STATE_IDLE) {
                localStopRecording()
            }
        }
    }
    private val mUpdateRemainingTime = Runnable {
        if (mediaRecorder != null && mNeedUpdateRemainingTime) {
            updateRemainingTime()
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaRecorder = null
        mLowStorageNotification = null
        mRemainingTimeCalculator = RemainingTimeCalculator()
        mNeedUpdateRemainingTime = false
        notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        telephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        /*监听电话状态*/
        telephonyManager?.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$packageName:RecorderService")
        mKeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val bundle = intent.extras
        if (bundle != null && bundle.containsKey(ACTION_NAME)) {
            when (bundle.getInt(ACTION_NAME, ACTION_INVALID)) {
                ACTION_START_RECORDING -> localStartRecording(
                    bundle.getInt(ACTION_PARAM_FORMAT),
                    bundle.getString(ACTION_PARAM_PATH) ?: folderPath(RecordControl.FOLDER_NAME),
                    bundle.getBoolean(ACTION_PARAM_HIGH_QUALITY),
                    bundle.getLong(ACTION_PARAM_MAX_FILE_SIZE)
                )
                ACTION_STOP_RECORDING -> localStopRecording()
                ACTION_ENABLE_MONITOR_REMAIN_TIME -> if (mediaRecorder != null) {
                    mNeedUpdateRemainingTime = true
                    mHandler.post(mUpdateRemainingTime)
                }
                ACTION_DISABLE_MONITOR_REMAIN_TIME -> {
                    mNeedUpdateRemainingTime = false
                    if (mediaRecorder != null) {
                        showRecordingNotification()
                    }
                }
                else -> {
                }
            }
            return START_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        telephonyManager?.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE)
        //释放唤醒锁
        mWakeLock?.apply {
            if (isHeld) {
                release()
            }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLowMemory() {
        localStopRecording()
        super.onLowMemory()
    }

    private fun localStartRecording(
        outputFileFormat: Int,
        path: String,
        highQuality: Boolean,
        maxFileSize: Long
    ) {
        if (mediaRecorder == null) {
            mRemainingTimeCalculator!!.reset()
            if (maxFileSize != -1L) {
                mRemainingTimeCalculator!!.setFileSizeLimit(File(path), maxFileSize)
            }
            mediaRecorder = MediaRecorder()
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            if (outputFileFormat == MediaRecorder.OutputFormat.THREE_GPP) {//3gp
                mRemainingTimeCalculator!!.setBitRate(RRecord.BITRATE_3GPP)
                mediaRecorder!!.setAudioSamplingRate(if (highQuality) 44100 else 22050)
                mediaRecorder!!.setOutputFormat(outputFileFormat)
                mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            } else {//amr
                mRemainingTimeCalculator!!.setBitRate(RRecord.BITRATE_AMR)
                mediaRecorder!!.setAudioSamplingRate(if (highQuality) 16000 else 8000)
                mediaRecorder!!.setOutputFormat(outputFileFormat)
                mediaRecorder!!.setAudioEncoder(if (highQuality) MediaRecorder.AudioEncoder.AMR_WB else MediaRecorder.AudioEncoder.AMR_NB)
            }
            mediaRecorder!!.setOutputFile(path)
            mediaRecorder!!.setOnErrorListener(this)

            // Handle IOException
            try {
                mediaRecorder!!.prepare()
            } catch (exception: IOException) {
                sendErrorBroadcast(Recorder.INTERNAL_ERROR)
                mediaRecorder!!.reset()
                mediaRecorder!!.release()
                mediaRecorder = null
                return
            }
            // Handle RuntimeException if the recording couldn't start
            try {
                mediaRecorder!!.start()
            } catch (exception: RuntimeException) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val isInCall = audioManager.mode == AudioManager.MODE_IN_CALL
                if (isInCall) {
                    sendErrorBroadcast(Recorder.IN_CALL_RECORD_ERROR)
                } else {
                    sendErrorBroadcast(Recorder.INTERNAL_ERROR)
                }
                mediaRecorder!!.reset()
                mediaRecorder!!.release()
                mediaRecorder = null
                return
            }
            filePath = path
            startTime = System.currentTimeMillis()
            //获取唤醒锁, 防止手机灭屏
            mWakeLock?.acquire()
            mNeedUpdateRemainingTime = false
            sendStateBroadcast()
            showRecordingNotification()
        }
    }

    private fun localStopRecording() {
        if (mediaRecorder != null) {
            mNeedUpdateRemainingTime = false
            try {
                mediaRecorder!!.stop()
            } catch (e: RuntimeException) {
            }
            mediaRecorder!!.release()
            mediaRecorder = null
            sendStateBroadcast()
            showStoppedNotification()
        }
        stopSelf()
    }

    private fun showRecordingNotification() {
//        Notification notification = new Notification(R.drawable.stat_sys_call_record,
//                getString(R.string.notification_recording), System.currentTimeMillis());
//        notification.flags = Notification.FLAG_ONGOING_EVENT;
//        PendingIntent pendingIntent;
//        pendingIntent = PendingIntent
//                .getActivity(this, 0, new Intent(this, SoundRecorder.class), 0);
//
////        notification.setLatestEventInfo(this, getString(R.string.app_name),
////                getString(R.string.notification_recording), pendingIntent);
//
//        startForeground(NOTIFICATION_ID, notification);
    }

    private fun showLowStorageNotification(minutes: Int) {
//        if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
//            // it's not necessary to show this notification in lock-screen
//            return;
//        }
//
//        if (mLowStorageNotification == null) {
//            mLowStorageNotification = new Notification(R.drawable.stat_sys_call_record_full,
//                    getString(R.string.notification_recording), System.currentTimeMillis());
//            mLowStorageNotification.flags = Notification.FLAG_ONGOING_EVENT;
//        }
//
//        PendingIntent pendingIntent;
//        pendingIntent = PendingIntent
//                .getActivity(this, 0, new Intent(this, SoundRecorder.class), 0);
//
////        mLowStorageNotification.setLatestEventInfo(this, getString(R.string.app_name),
////                getString(R.string.notification_warning, minutes), pendingIntent);
//        startForeground(NOTIFICATION_ID, mLowStorageNotification);
    }

    private fun showStoppedNotification() {
//        stopForeground(true);
//        mLowStorageNotification = null;
//
//        Notification notification = new Notification(R.drawable.stat_sys_call_record,
//                getString(R.string.notification_stopped), System.currentTimeMillis());
//        notification.flags = Notification.FLAG_AUTO_CANCEL;
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setType("audio/*");
//        intent.setDataAndType(Uri.fromFile(new File(mFilePath)), "audio/*");
//
//        PendingIntent pendingIntent;
//        pendingIntent = PendingIntent.getActivity(this, 0, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
////        notification.setLatestEventInfo(this, getString(R.string.app_name),
////                getString(R.string.notification_stopped), pendingIntent);
//        mNotifiManager.notify(NOTIFICATION_ID, notification);
    }

    private fun sendStateBroadcast() {
        val intent = Intent(RECORDER_SERVICE_BROADCAST_NAME)
        intent.putExtra(
            RECORDER_SERVICE_BROADCAST_STATE,
            mediaRecorder != null
        )
        sendBroadcast(intent)
    }

    private fun sendErrorBroadcast(error: Int) {
        val intent = Intent(RECORDER_SERVICE_BROADCAST_NAME)
        intent.putExtra(RECORDER_SERVICE_BROADCAST_ERROR, error)
        sendBroadcast(intent)
    }

    private fun updateRemainingTime() {
        val t = mRemainingTimeCalculator!!.timeRemaining()
        if (t <= 0) {
            localStopRecording()
            return
        } else if (t <= 1800
            && mRemainingTimeCalculator!!.currentLowerLimit() != RemainingTimeCalculator.FILE_SIZE_LIMIT
        ) {
            // less than half one hour
            showLowStorageNotification(Math.ceil(t / 60.0).toInt())
        }
        if (mediaRecorder != null && mNeedUpdateRemainingTime) {
            mHandler.postDelayed(mUpdateRemainingTime, 500)
        }
    }

    override fun onError(mr: MediaRecorder, what: Int, extra: Int) {
        sendErrorBroadcast(Recorder.INTERNAL_ERROR)
        localStopRecording()
    }
}