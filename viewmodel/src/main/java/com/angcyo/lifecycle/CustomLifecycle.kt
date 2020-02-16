package com.angcyo.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * 自定义生命周期
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/16
 */

class CustomLifecycle : LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        setCurrentState(Lifecycle.State.CREATED)
    }

    fun doOnResume() {
        setCurrentState(Lifecycle.State.RESUMED)
    }

    fun setCurrentState(state: Lifecycle.State) {
        lifecycleRegistry.currentState = state
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}
