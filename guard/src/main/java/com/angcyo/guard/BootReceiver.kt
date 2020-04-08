package com.angcyo.guard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class BootReceiver : BroadcastReceiver() {

    companion object {

        /**全局回调*/
        var onBootReceiver: (context: Context, intent: Intent) -> Unit = { _, _ ->

        }

        var bootReceiver: BootReceiver? = null

        fun register(context: Context) {
            if (bootReceiver == null) {
                bootReceiver = BootReceiver()
                context.registerReceiver(bootReceiver, IntentFilter().apply {
                    addAction(Intent.ACTION_POWER_CONNECTED)
                    addAction(Intent.ACTION_POWER_DISCONNECTED)
                    addAction(Intent.ACTION_BATTERY_CHANGED)
                    addAction(Intent.ACTION_USER_PRESENT)
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addAction(Intent.ACTION_PACKAGE_REPLACED)
                    addAction(Intent.ACTION_CAMERA_BUTTON)
                    addAction(Intent.ACTION_MEDIA_BUTTON)
                    addAction(Intent.ACTION_CALL_BUTTON)
                })
            }
        }

        fun unregister(context: Context) {
            if (bootReceiver != null) {
                context.unregisterReceiver(bootReceiver)
            }
            bootReceiver = null
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        L.i("${this.simpleHash()} 广播:$context $intent")

        intent?.apply {
            onBootReceiver(context, this)
        }
    }
}