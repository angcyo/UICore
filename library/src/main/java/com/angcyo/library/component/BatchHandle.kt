package com.angcyo.library.component

import com.angcyo.library.annotation.DSL

/**
 * 批量处理数据, 调用在哪个线程, 回调就在那个线程
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/21
 */

class BatchHandle<T>(
    /**需要处理的数据*/
    val dataPool: List<T>,
    /**处理方法*/
    val onHandle: T.(BatchHandle<T>) -> Unit,
    /**结束的回调*/
    val onFinish: (BatchHandle<T>) -> Unit
) {

    /**最大数据处理数*/
    var maxCount: Int = dataPool.size

    /**当前处理数据数*/
    var index = 0

    //---

    /**是否被取消*/
    var isCancel: Boolean = false

    /**是否完成*/
    var isFinish: Boolean = false

    /**异常信息*/
    var error: Throwable? = null

    /**发生异常时, 是否中断*/
    var errorInterrupt: Boolean = true

    //---

    /**开始处理*/
    fun start() {
        isCancel = false
        isFinish = false
        error = null
        index = 0

        next()
    }

    /**调用此方法, 继续循环处理*/
    fun next() {
        if (isFinish) {
            return
        }
        if (isCancel || (error != null && errorInterrupt)) {
            isFinish = true
            onFinish(this)
            return
        }
        if (index >= maxCount) {
            //结束
            isFinish = true
            onFinish(this)
        } else {
            val data = dataPool.getOrNull(index)
            if (data == null) {
                index++
                next()
            } else {
                try {
                    data.onHandle(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                    error = e

                    if (errorInterrupt) {
                        isFinish = true
                        onFinish(this)
                    }
                }
            }
        }
    }
}

/**开始批处理数据
 * listOf("").batchHandle({ handle ->
 *     handle.next()
 *   }) {
 *   L.i(it.error, it.isFinish)
 * }
 * */
@DSL
fun <T> List<T>.batchHandle(
    onHandle: T.(BatchHandle<T>) -> Unit,
    onFinish: (BatchHandle<T>) -> Unit
) = BatchHandle(this, onHandle, onFinish).apply {
    start()
}