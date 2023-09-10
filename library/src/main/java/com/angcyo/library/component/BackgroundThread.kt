package com.angcyo.library.component

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import androidx.annotation.AnyThread

/**
 * 后台线程, 和ui主线程组成双线程模式
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/09/10
 */
class BackgroundThread private constructor() :
    HandlerThread("BackgroundThread-${Process.myPid()}") {

    companion object {

        /**单例*/
        val instance: BackgroundThread by lazy {
            BackgroundThread()
        }
    }

    /**处理器*/
    var backgroundHandler: Handler? = null

    init {
        start()
    }

    override fun run() {
        super.run()
    }

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        backgroundHandler = Handler(looper)
    }

    override fun getLooper(): Looper {
        return super.getLooper()
    }

    /**直接退出*/
    override fun quit(): Boolean {
        return super.quit()
    }

    /**任务处理完再退出*/
    override fun quitSafely(): Boolean {
        return super.quitSafely()
    }

    override fun getThreadId(): Int {
        return super.getThreadId()
    }
}

/**在后台线程中执行
 * [android.app.Instrumentation.runOnMainSync]
 * [android.app.Activity.runOnUiThread]
 * */
@AnyThread
fun runOnBackground(action: () -> Unit) {
    if (BackgroundThread.instance.backgroundHandler == null) {
        //临时使用线程池执行任务
        ThreadExecutor.execute(action)
    } else {
        BackgroundThread.instance.backgroundHandler?.post(action)
    }
}

/**在主线程中执行*/
@AnyThread
fun runOnMainThread(action: () -> Unit) {
    MainExecutor.execute(action)
}