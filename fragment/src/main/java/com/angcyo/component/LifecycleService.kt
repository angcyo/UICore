package com.angcyo.component

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * 具有生命周期[LifecycleOwner]提供的[Service]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/18
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class LifecycleService : Service(), LifecycleOwner {

    companion object {
        fun bindService(
            context: Context,
            intent: Intent,
            result: (ServiceConnection, IBinder?) -> Unit
        ) {
            context.bindService(intent, object : ServiceConnection {
                override fun onServiceDisconnected(name: ComponentName) {
                    result(this, null)
                }

                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    result(this, service)
                }
            }, BIND_AUTO_CREATE)
        }

        fun unbindService(context: Context, conn: ServiceConnection) {
            context.unbindService(conn)
        }
    }

    /** 调用startService()启动服务时回调.
     * [startId] 会随着[调用startService]调用次数的增加而增加
     * */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.apply {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED

            handleIntent(this)
        }
        return START_STICKY
    }

    open fun handleIntent(intent: Intent) {

    }

    //<editor-fold desc="Binder支持">

    val innerBinder: InnerBinder by lazy { InnerBinder() }

    /** 通过bindService()绑定到服务的客户端 */
    override fun onBind(intent: Intent?): IBinder? = innerBinder

    /** 通过unbindService()解除所有客户端绑定时调用 */
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    /** 通过bindService()将客户端绑定到服务时调用*/
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun unbindService(conn: ServiceConnection) {
        super.unbindService(conn)
    }

    inner class InnerBinder : Binder() {
        fun service() = this@LifecycleService
    }

    //</editor-fold desc="Binder支持">

    //<editor-fold desc="Lifecycle支持">

    val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    //</editor-fold desc="Lifecycle支持">

}