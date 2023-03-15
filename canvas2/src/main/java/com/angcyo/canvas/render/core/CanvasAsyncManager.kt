package com.angcyo.canvas.render.core

import android.os.Handler
import android.os.HandlerThread
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.nowTime
import java.util.*

/**
 * 异步调度管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/07
 */
class CanvasAsyncManager(val delegate: CanvasRenderDelegate) {

    companion object {

        /**状态, 添加一个异步任务*/
        const val STATE_ADD = 0b1

        /**状态, 开始一个异步任务*/
        const val STATE_START = STATE_ADD shl 1

        /**状态, 结束一个异步任务*/
        const val STATE_END = STATE_START shl 1

        /**状态, 失败一个异步任务*/
        const val STATE_ERROR = STATE_END shl 1
    }

    private var thread: HandlerThread? = null

    private var asyncHandler: Handler? = null

    private val tasks: Queue<AsyncTask> = LinkedList()

    private fun next() {
        if (tasks.isEmpty()) return
        tasks.peek()?.apply {
            delegate.dispatchAsyncStateChange(uuid, STATE_START)

            var exception: Exception? = null
            try {
                run() // 运行
            } catch (e: Exception) {
                e.printStackTrace()
                exception = e
            }

            tasks.poll() //移除第一个元素
            updateAsync(uuid, false)
            delegate.dispatchAsyncStateChange(
                uuid,
                if (exception == null) STATE_END else STATE_ERROR
            )
            sendEmptyMessage() //next
        }
    }

    private fun sendEmptyMessage() {
        asyncHandler?.sendEmptyMessage(0x0)
    }

    private fun updateAsync(uuid: String, async: Boolean) {
        updateAsync(delegate.renderManager.findElementRenderer(uuid), async)
    }

    private fun updateAsync(renderer: BaseRenderer?, async: Boolean) {
        renderer?.updateAsync(async, Reason.code, null)
    }

    /**开启异步线程*/
    @CallPoint
    fun startAsync() {
        releaseAsync()
        thread = HandlerThread("CanvasAsyncManager_${nowTime()}").apply {
            start()
            asyncHandler = Handler(looper) {
                next()
                true
            }
        }
    }

    /**退出消息循环, 停止线程*/
    fun releaseAsync() {
        asyncHandler = null
        thread?.quit()
    }

    /**移除一个异步任务*/
    fun removeAsyncTask(uuid: String): Boolean {
        return tasks.removeAll { it.uuid == uuid }
    }

    /**添加一个异步任务*/
    fun addAsyncTask(renderer: BaseRenderer, action: () -> Unit) {
        tasks.add(AsyncTask(renderer.uuid, action))
        updateAsync(renderer, true)
        delegate.dispatchAsyncStateChange(renderer.uuid, STATE_ADD)
        sendEmptyMessage()
    }

    /**添加一个异步任务*/
    fun addAsyncTask(uuid: String, action: () -> Unit) {
        tasks.add(AsyncTask(uuid, action))
        updateAsync(uuid, true)
        delegate.dispatchAsyncStateChange(uuid, STATE_ADD)
        sendEmptyMessage()
    }

    /**判断是否有指定的异步任务*/
    fun hasAsyncTask(uuid: String) = tasks.find { it.uuid == uuid } != null

    /**是否有异步任务未执行完成*/
    fun hasAsyncTask() = tasks.isNotEmpty()

    /**异步任务*/
    inner class AsyncTask(val uuid: String, val action: () -> Unit) : Runnable {
        override fun run() {
            action()
        }
    }

}