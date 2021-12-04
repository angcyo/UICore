package com.angcyo.core.component

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.angcyo.library.component.*
import com.angcyo.library.ex.getAppOpenIntentByPackageName
import com.angcyo.library.ex.urlIntent
import com.angcyo.library.getAppName
import com.angcyo.library.toastQQ

/**
 * 好好活着
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/04
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class AliveService : Service() {

    var _notifyId: Int = (System.currentTimeMillis() and 0xFFFFFFF).toInt()

    //<editor-fold desc="周期回调方法">

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _alive()
        return START_STICKY
    }

    override fun onDestroy() {
        _die()
        super.onDestroy()
    }

    //</editor-fold desc="周期回调方法">

    /**通知通道*/
    var notifyChannelName = "保活通知"

    fun _alive() {
        //foreground
        startForeground(_notifyId, dslBuildNotify {
            channelName = notifyChannelName
            notifyOngoing = true
            low()
            clickActivity(getAppOpenIntentByPackageName(packageName))
            single(getAppName(), "App运行中,请勿强杀进程!!!")
        })

        if (!isNotificationsEnabled() || !notifyChannelName.isChannelEnable()) {
            toastQQ("请打开通知通道[$notifyChannelName]")
        }
    }

    fun _die() {
        if (_notifyId > 0) {
            DslNotify.cancelNotify(this, _notifyId)
        }
    }
}

/**开启保活服务*/
fun Context.startAlive() {
    val intent = Intent(this, AliveService::class.java)
    try {
        startService(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**关闭保活服务*/
fun Context.stopAlive() {
    val intent = Intent(this, AliveService::class.java)
    stopService(intent)
}