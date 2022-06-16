package com.angcyo.library.component

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.SparseArray
import java.lang.ref.WeakReference

/**
 * 应用程序后台通知
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/22
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

object RBackground {

    const val CREATE = "onActivityCreated"
    const val STARTED = "onActivityStarted"
    const val RESUMED = "onActivityResumed"
    const val PAUSED = "onActivityPaused"
    const val STOPPED = "onActivityStopped"
    const val SAVE = "onActivitySaveInstanceState"

    /** class_name|state */
    val stack = SparseArray<String>()

    private val observers = mutableListOf<OnBackgroundObserver>()

    /**最后一个[Activity]*/
    var lastActivity: WeakReference<Activity>? = null

    /**当有[Activity]创建时, app之前是否是在后台*/
    var isCreatedFromBackground: Boolean = false

    /**主线程回调*/
    val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            isCreatedFromBackground = isBackground()
            pushItem(activity.hashCode(), activity.javaClass.name, CREATE)
        }

        override fun onActivityStarted(activity: Activity) {
            changeItem(activity.hashCode(), activity.javaClass.name, STARTED)
        }

        override fun onActivityResumed(activity: Activity) {
            lastActivity = WeakReference(activity)
            changeItem(activity.hashCode(), activity.javaClass.name, RESUMED)
        }

        override fun onActivityPaused(activity: Activity) {
            if (activity.isFinishing) {
            } else {
                changeItem(activity.hashCode(), activity.javaClass.name, PAUSED)
            }
        }

        override fun onActivityStopped(activity: Activity) {
            if (activity.isFinishing) {
            } else {
                changeItem(activity.hashCode(), activity.javaClass.name, STOPPED)
            }
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (lastActivity?.get() == activity) {
                lastActivity = null
            }
            removeItem(activity.hashCode(), activity.javaClass.name)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            //2021-7-6 注释
            //changeItem(activity.hashCode(), activity.javaClass.name, SAVE)
        }
    }

    private fun pushItem(code: Int, className: String, state: String) {
        val item = "$className|$state"

        stack.put(code, item)

        observers.forEach {
            it.onActivityChanged(stack, isBackground())
        }
    }

    private fun changeItem(code: Int, className: String, state: String) {
        pushItem(code, className, state)
    }

    private fun removeItem(code: Int, className: String) {
        stack.remove(code)

        observers.forEach {
            it.onActivityChanged(stack, isBackground())
        }
    }

    /**初始化组件*/
    fun init(application: Application, observer: OnBackgroundObserver? = null) {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)

        observer?.let {
            registerObserver(it)
        }
    }

    /**判断程序是否在后台运行*/
    fun isBackground(): Boolean {
        var result = true

        for (i in 0 until stack.size()) {
            val value = stack.get(stack.keyAt(i))
            if (value.endsWith(STOPPED)) {
                //所有的Activity都stop了
            } else {
                result = false
                break
            }
        }

        return result
    }

    /**观察程序进入后台*/
    fun registerObserver(observer: OnBackgroundObserver, notify: Boolean = false) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
        if (notify) {
            observer.onActivityChanged(stack, isBackground())
        }
    }

    fun unregisterObserver(observer: OnBackgroundObserver) {
        observers.remove(observer)
    }
}

interface OnBackgroundObserver {
    fun onActivityChanged(stack: SparseArray<String>, background: Boolean)
}