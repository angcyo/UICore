package com.angcyo.core.component

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


/**
 * 循环查询系统音量, 如果发现有改变. 则通知观察者
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object VolumeObserver : Runnable {
    val observer = CopyOnWriteArrayList<IObserver>()

    /**开始的值*/
    val fromValueMap = ConcurrentHashMap<Int, StreamValue>()

    var isListening = false

    lateinit var am: AudioManager
    val handler = Handler(Looper.getMainLooper())
    val listenerType = listOf(
        AudioManager.STREAM_SYSTEM,
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_RING,
        AudioManager.STREAM_ALARM
    )

    fun init(context: Context) {
        am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        resetValue()
    }

    /**开始轮询值*/
    fun startListener() {
        if (isListening) {
            return
        }
        isListening = true
        handler.post(this)
    }

    /**结束轮询*/
    fun stopListener() {
        isListening = false
        handler.removeCallbacks(this)
    }

    /**观察值的变化*/
    fun observe(observer: IObserver) {
        if (!VolumeObserver.observer.contains(observer)) {
            VolumeObserver.observer.add(observer)
        }
    }

    /**移除值的变化*/
    fun removeObserve(observer: IObserver) {
        if (VolumeObserver.observer.contains(observer)) {
            VolumeObserver.observer.remove(observer)
        }
    }

    /**初始化值*/
    fun resetValue() {
        listenerType.forEach {
            fromValueMap[it] = _getValue(it)
        }
    }

    fun _getValue(type: Int = AudioManager.STREAM_MUSIC): StreamValue {
        //val max = am.getStreamMaxVolume(type)
        val value = am.getStreamVolume(type)
        return StreamValue(type, value)
    }

    var listenerInterval = 160L

    override fun run() {

        listenerType.forEach { type ->
            val oldValue = fromValueMap[type]
            val newValue = _getValue(type)
            fromValueMap[type] = newValue

            val old = oldValue?.value ?: 0
            if (old != newValue.value) {
                observer.forEach {
                    it.onChange(type, old, newValue.value)
                }
            }
        }

        if (isListening) {
            handler.postDelayed(this, listenerInterval)
        }
    }
}

interface IObserver {
    fun onChange(type: Int, from: Int, value: Int)
}

data class StreamValue(val type: Int = AudioManager.STREAM_MUSIC, val value: Int = 0)