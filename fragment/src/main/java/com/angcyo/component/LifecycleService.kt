package com.angcyo.component

import android.app.Service
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.angcyo.library.component.BaseService

/**
 * 具有生命周期[LifecycleOwner]提供的[Service]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/18
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class LifecycleService : BaseService(), LifecycleOwner {

    //<editor-fold desc="Lifecycle支持">

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return super.onStartCommand(intent, flags, startId)
    }

    val lifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    //</editor-fold desc="Lifecycle支持">

}