package com.angcyo.library.component

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.SoundPool
import android.os.Build
import com.angcyo.library.L
import com.angcyo.library.app

/**
 * [SoundPool]操作类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/04
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

//val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//val rt = RingtoneManager.getRingtone(fContext(), uri)
//rt.play()

class RSoundPool {

    var isInit: Boolean = false

    /**音量值（范围= 0.0到1.0）*/
    var volume = 1f

    /**priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理*/
    var priority = 1

    /**loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次*/
    var loop = 0

    /**rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率*/
    var rate = 1f

    var maxStreams = 1

    var streamType = AudioManager.STREAM_MUSIC

    /**加载成功的id*/
    val completeSampleIdList = mutableListOf<Int>()

    /**资源和id的映射*/
    val soundIdMap = hashMapOf<Any, Int>()

    /**等待播放的sound id*/
    val pendingPlayIdList = mutableListOf<Int>()

    /**已经播放的stream id*/
    val streamIdMap = hashMapOf<Any, Int>()

    lateinit var soundPool: SoundPool

    fun init(context: Context) {
        if (isInit) {
            return
        }
        isInit = true

        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        val actualVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volume = actualVolume * 1f / maxVolume

        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                .setMaxStreams(maxStreams)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setLegacyStreamType(streamType)
                        .setContentType(streamType)
                        .build()
                )
                .build()
        } else {
            SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0)
        }

        /**回调*/
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if (status == 0) {
                completeSampleIdList.add(sampleId)

                if (pendingPlayIdList.contains(sampleId)) {
                    _play(sampleId)
                }
            }
        }
    }

    /**指定的资源, 是否加载成功了*/
    fun isLoad(res: Any): Boolean {
        val soundId = soundIdMap[res]
        if (soundId == null || !completeSampleIdList.contains(soundId)) {
            return false
        }
        return true
    }

    /**播放资源
     * [R.raw.incoming]*/
    fun play(resId: Int, context: Context = app()) {
        init(context)
        val soundId = soundIdMap[resId]
        if (isLoad(resId)) {
            _play(soundId!!)
        } else {
            //资源还未加载, 或者加载失败, 在重新加载
            val id = soundPool.load(context, resId, priority)
            soundIdMap[resId] = id
            pendingPlayIdList.add(id)
        }
    }

    /**播放系统默认的铃声
     * [RingtoneManager.TYPE_ALL]
     * [RingtoneManager.TYPE_ALARM]
     * [RingtoneManager.TYPE_NOTIFICATION]
     * [RingtoneManager.TYPE_RINGTONE]
     * */
    fun playDefaultRingtone(
        type: Int = RingtoneManager.TYPE_NOTIFICATION,
        context: Context = app()
    ) {
        init(context)
        val soundId = soundIdMap[type]
        if (isLoad(type)) {
            _play(soundId!!)
        } else {
            //资源还未加载, 或者加载失败, 在重新加载
            val uri = RingtoneManager.getActualDefaultRingtoneUri(
                context,
                type
            )
            val id = soundPool.load(uri.path, priority)
            soundIdMap[type] = id
            pendingPlayIdList.add(id)
        }
    }

    fun _play(soundId: Int) {
        pendingPlayIdList.remove(soundId)
        val streamId = soundPool.play(soundId, volume, volume, priority, loop, rate)
        if (streamId == 0) {
            L.w("播放失败->[$soundId]")
        } else {
            soundIdMap.forEach { entry ->
                if (entry.value == soundId) {
                    //将资源id和流id对应起来
                    streamIdMap[entry.key] = streamId
                }
            }
        }
    }

    /**停止播放, 如果是无限循环的播放, 则需要主动停止*/
    fun stop(resId: Int) {
        streamIdMap[resId]?.let {
            soundPool.stop(it)
        }
    }

    /**释放资源*/
    fun release() {
        if (isInit) {
            streamIdMap.forEach { entry ->
                soundPool.stop(entry.value)
            }
            completeSampleIdList.forEach {
                soundPool.unload(it)
            }
            soundPool.release()
        }
    }
}