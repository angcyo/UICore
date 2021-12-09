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

    /**所有播放/待播放的资源*/
    val soundList = mutableListOf<SoundItem>()

    /**等待播放的sound id*/
    val pendingPlayIdList = mutableListOf<Int>()

    /**无限循环时, 播放的资源列表*/
    val playResList = mutableListOf<Any>()

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
                    soundList.find { it.soundId == sampleId }?.let {
                        _play(it)
                    }
                }
            }
        }
    }

    /**指定的资源, 是否加载成功了*/
    fun isLoad(res: Any): Boolean {
        return soundList.find { it.res == res }?.run {
            if (soundId == -1) {
                false
            } else {
                completeSampleIdList.contains(soundId)
            }
        } ?: false
    }

    fun soundIdToRes(soundId: Int): Any? {
        return soundList.find { it.soundId == soundId }?.res
    }

    /**加载资源, 可选是否播放*/
    fun loadRes(
        res: Int,
        play: Boolean = false,
        context: Context = app(),
        init: (SoundItem.() -> Unit)? = null
    ) {
        if (isLoad(res) && !play) {
            return
        }

        init(context)
        val soundItem = soundList.find { it.res == res }

        if (soundItem == null) {
            //资源还未加载, 或者加载失败, 在重新加载
            val item = soundItem ?: SoundItem(res, volume, volume, priority, loop, rate).apply {
                soundList.add(this)
            }
            init?.invoke(item)
            val id = soundPool.load(context, res, item.priority)
            item.soundId = id
            if (play) {
                pendingPlayIdList.add(id)
            }
        } else if (play) {
            _play(soundItem)
        }
    }

    fun loadDefaultRingtone(
        type: Int = RingtoneManager.TYPE_NOTIFICATION,
        play: Boolean = false,
        context: Context = app()
    ) {
        if (isLoad(type) && !play) {
            return
        }

        init(context)

        val soundItem = soundList.find { it.res == type }

        if (soundItem == null || !isLoad(type)) {
            //资源还未加载, 或者加载失败, 在重新加载
            val item = soundItem ?: SoundItem(type, volume, volume, priority, loop, rate).apply {
                soundList.add(this)
            }
            val uri = RingtoneManager.getActualDefaultRingtoneUri(
                context,
                type
            )
            val id = soundPool.load(uri.path, priority)
            item.soundId = id
            if (play) {
                pendingPlayIdList.add(id)
            }
        } else if (play) {
            _play(soundItem)
        }
    }

    /**播放资源
     * [R.raw.incoming]*/
    fun play(res: Int, context: Context = app()) {
        loadRes(res, true, context)
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
        loadDefaultRingtone(type, true, context)
    }

    fun _play(soundItem: SoundItem) {
        val soundId = soundItem.soundId
        if (playResList.contains(soundItem.res)) {
            //正在播放当前的资源
            return
        }

        pendingPlayIdList.remove(soundId)
        val streamId = soundPool.play(
            soundId,
            soundItem.leftVolume,
            soundItem.rightVolume,
            soundItem.priority,
            soundItem.loop,
            soundItem.rate
        )

        if (streamId == 0) {
            L.w("播放失败->[$soundId]")
        } else {
            soundItem.streamId = streamId
            if (soundItem.loop == -1) {
                //无限循环时, 才保存播放过的资源
                playResList.add(soundItem.res)
            }
        }
    }

    /**停止播放, 如果是无限循环的播放, 则需要主动停止*/
    fun stop(res: Any) {
        playResList.remove(res)
        val soundItem = soundList.find { it.res == res }
        soundItem?.let {
            soundPool.stop(soundItem.streamId)
        }
    }

    /**释放资源*/
    fun release() {
        if (isInit) {
            soundList.forEach {
                stop(it.res)
            }
            completeSampleIdList.forEach {
                soundPool.unload(it)
            }
            soundPool.release()
        }
    }

    data class SoundItem(
        var res: Any, //播放的资源, 系统音效/R.raw.xxx等
        var leftVolume: Float = 1f, //需要播放的音量值（范围= 0.0到1.0)
        var rightVolume: Float = 1f,
        var priority: Int = 1,//优先级
        var loop: Int = 0,//loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
        var rate: Float = 1f,//rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率
        var soundId: Int = -1, //加载成功之后, 用来真正播放的id
        var streamId: Int = -1, //播放之后, 流的id, 用来关闭
    )
}