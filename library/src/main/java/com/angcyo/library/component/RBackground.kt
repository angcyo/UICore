package com.angcyo.library.component

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import com.angcyo.library.app
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
    const val DESTROYED = "onActivityDestroyed"
    const val SAVE = "onActivitySaveInstanceState"

    /** class_name|state */
    val stack = SparseArray<String>()

    private val observers = mutableListOf<OnBackgroundObserver>()

    /**最后一个[Activity]*/
    var lastActivityRef: WeakReference<Activity>? = null

    /**当有[Activity]创建时, app之前是否是在后台*/
    var isCreatedFromBackground: Boolean = false

    /**主线程回调*/
    val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            isCreatedFromBackground = isBackground()
            pushItem(activity.hashCode(), activity.javaClass.name, CREATE)

            observers.forEach {
                it.onActivityLifecycleChanged(activity, CREATE)
            }
        }

        override fun onActivityStarted(activity: Activity) {
            changeItem(activity.hashCode(), activity.javaClass.name, STARTED)

            observers.forEach {
                it.onActivityLifecycleChanged(activity, STARTED)
            }
        }

        override fun onActivityResumed(activity: Activity) {
            lastActivityRef = WeakReference(activity)
            changeItem(activity.hashCode(), activity.javaClass.name, RESUMED)

            observers.forEach {
                it.onActivityLifecycleChanged(activity, RESUMED)
            }
        }

        override fun onActivityPaused(activity: Activity) {
            if (activity.isFinishing) {
            } else {
                changeItem(activity.hashCode(), activity.javaClass.name, PAUSED)
            }

            observers.forEach {
                it.onActivityLifecycleChanged(activity, PAUSED)
            }
        }

        override fun onActivityStopped(activity: Activity) {
            if (activity.isFinishing) {
            } else {
                changeItem(activity.hashCode(), activity.javaClass.name, STOPPED)
            }

            observers.forEach {
                it.onActivityLifecycleChanged(activity, STOPPED)
            }
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (lastActivityRef?.get() == activity) {
                lastActivityRef = null
            }
            removeItem(activity.hashCode(), activity.javaClass.name)

            observers.forEach {
                it.onActivityLifecycleChanged(activity, DESTROYED)
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            //2021-7-6 注释
            //changeItem(activity.hashCode(), activity.javaClass.name, SAVE)

            observers.forEach {
                it.onActivityLifecycleChanged(activity, SAVE)
            }
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
    fun registerObserver(observer: OnBackgroundObserver, notifyFirst: Boolean = false) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
        if (notifyFirst) {
            observer.onActivityChanged(stack, isBackground())
        }
    }

    /**移除监听*/
    fun unregisterObserver(observer: OnBackgroundObserver) {
        observers.remove(observer)
    }

    //region ---静态方法---

    /**
     * 将本应用置顶到最前端, 将应用前台显示
     * 当本应用位于后台时，则将它切换到最前端
     *
     * @param context
     */
    fun moveAppToFront(context: Context = app()) {
        if (!isRunningForeground(context)) {
            //获取ActivityManager
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            //获得当前运行的task(任务)
            val taskInfoList = activityManager.getRunningTasks(100)
            for (taskInfo in taskInfoList) {
                //找到本应用的 task，并将它切换到前台
                if (taskInfo.topActivity?.packageName == context.packageName) {
                    val id = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        taskInfo.taskId
                    } else {
                        taskInfo.id
                    }
                    activityManager.moveTaskToFront(id, 0)
                    break
                }
            }
        }
    }

    /**
     * 判断本应用是否已经位于最前端
     *
     * @param context
     * @return 本应用已经位于最前端时，返回 true；否则返回 false
     */
    fun isRunningForeground(context: Context = app()): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcessInfoList = activityManager.runningAppProcesses
        //枚举进程
        for (appProcessInfo in appProcessInfoList) {
            if (appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName == context.applicationInfo.processName) {
                    return true
                }
            }
        }
        return false
    }

    //endregion ---静态方法---

}

/**观察者*/
abstract class OnBackgroundObserver {

    /**前后台改变*/
    open fun onActivityChanged(stack: SparseArray<String>, background: Boolean) {
    }

    /**生命周期改变
     * [com.angcyo.library.component.RBackground.CREATE]
     * [com.angcyo.library.component.RBackground.STARTED]
     * [com.angcyo.library.component.RBackground.RESUMED]
     * [com.angcyo.library.component.RBackground.PAUSED]
     * [com.angcyo.library.component.RBackground.STOPPED]
     * [com.angcyo.library.component.RBackground.DESTROYED]
     * [com.angcyo.library.component.RBackground.SAVE]
     * */
    open fun onActivityLifecycleChanged(activity: Activity, state: String) {
    }
}
