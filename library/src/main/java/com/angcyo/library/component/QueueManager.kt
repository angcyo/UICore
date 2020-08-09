package com.angcyo.library.component

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 将所有操作, 按照FIFO的方式处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/09
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
object QueueManager {

    val isRun = AtomicBoolean(false)
    private val taskQueue: ConcurrentLinkedQueue<QueueTask> = ConcurrentLinkedQueue()

    @Synchronized
    fun add(task: QueueTask) {
        taskQueue.add(task)
        if (!isRun.get()) {
            _runInner()
        }
    }

    @Synchronized
    fun add(run: (next: QueueNext) -> Unit) {
        val doNext: QueueNext = {
            _runNext(it)
        }
        add(QueueTask(doNext) {
            run(doNext)
        })
    }

    //内部执行代码
    fun _runInner() {
        val task: QueueTask? = taskQueue.poll()
        if (task == null) {
            isRun.set(false)
        } else {
            isRun.set(true)
            try {
                task.onRun()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //下个
    fun _runNext(error: Throwable?) {
        ThreadExecutor.onMain(Runnable { _runInner() })
    }
}

typealias QueueNext = (Throwable?) -> Unit

class QueueTask(
    var result: QueueNext /*执行完毕后的通知回调*/,
    val onRun: () -> Unit /*执行*/
)