package com.angcyo.library

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import android.text.TextUtils
import android.util.SparseArray
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.angcyo.library.component.OnBackgroundObserver
import com.angcyo.library.component.RBackground
import com.angcyo.library.ex.isShowDebug
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * Created by angcyo on 2019/12/23.
 * Copyright (c) 2016, angcyo@126.com All Rights Reserved.
 * *                                                   #
 * #                                                   #
 * #                       _oo0oo_                     #
 * #                      o8888888o                    #
 * #                      88" . "88                    #
 * #                      (| -_- |)                    #
 * #                      0\  =  /0                    #
 * #                    ___/`---'\___                  #
 * #                  .' \\|     |# '.                 #
 * #                 / \\|||  :  |||# \                #
 * #                / _||||| -:- |||||- \              #
 * #               |   | \\\  -  #/ |   |              #
 * #               | \_|  ''\---/''  |_/ |             #
 * #               \  .-\__  '-'  ___/-. /             #
 * #             ___'. .'  /--.--\  `. .'___           #
 * #          ."" '<  `.___\_<|>_/___.' >' "".         #
 * #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 * #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 * #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 * #                       `=---='                     #
 * #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 * #                                                   #
 * #               佛祖保佑         永无BUG              #
 * #                                                   #
 */

open class LibApplication : Application(), LifecycleOwner {

    override fun onCreate() {
        super.onCreate()

        //必须第一个初始化
        Library.init(this, isShowDebug())
        L.init(getAppString("app_name") ?: "Log", isShowDebug())

        //初始化
        initLibApplication()
    }

    open fun initLibApplication() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        if (isMainProgress()) {
            onCreateMain()
        }
    }

    /**主进程初始化*/
    open fun onCreateMain() {
        RBackground.init(this, object : OnBackgroundObserver() {
            override fun onActivityChanged(stack: SparseArray<String>, background: Boolean) {
                L.i(stack)
                if (background) {
                    L.i("程序已在后台运行.")
                }
            }
        })
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onTerminate() {
        super.onTerminate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    //<editor-fold desc="Lifecycle支持">

    val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    //</editor-fold desc="Lifecycle支持">
}

fun Context?.isMainProgress() = isMainProcess(this)

/**是否是主进程*/
fun isMainProcess(context: Context?): Boolean {
    if (context == null) {
        return false
    }
    val packageName = context.applicationContext.packageName
    val processName = getProcessName(context)
    return packageName == processName
}

fun getProcessName(context: Context): String? {
    var processName = getProcessFromFile()
    if (processName == null) { // 如果装了xposed一类的框架，上面可能会拿不到，回到遍历迭代的方式
        processName = getProcessNameByAM(context)
    }
    return processName
}

fun getProcessFromFile(): String? {
    var reader: BufferedReader? = null
    return try {
        val pid = Process.myPid()
        val file = "/proc/$pid/cmdline"
        reader = BufferedReader(
            InputStreamReader(
                FileInputStream(file),
                "iso-8859-1"
            )
        )
        var c: Int
        val processName = StringBuilder()
        while (reader.read().also { c = it } > 0) {
            processName.append(c.toChar())
        }
        processName.toString()
    } catch (e: Exception) {
        null
    } finally {
        if (reader != null) {
            try {
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

fun getProcessNameByAM(context: Context): String? {
    var processName: String? = null
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    while (true) {
        val plist = am.runningAppProcesses
        if (plist != null) {
            for (info in plist) {
                if (info.pid == Process.myPid()) {
                    processName = info.processName
                    break
                }
            }
        }
        if (!TextUtils.isEmpty(processName)) {
            return processName
        }
        try {
            Thread.sleep(100L) // take a rest and again
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }
}