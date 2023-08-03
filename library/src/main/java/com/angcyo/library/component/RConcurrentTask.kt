package com.angcyo.library.component

import android.app.PendingIntent.CanceledException
import com.angcyo.library.annotation.ThreadSync
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max

/**
 * 多任务并发执行, 结束后回调
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class RConcurrentTask(
    @ThreadSync
    val taskQueue: ConcurrentLinkedQueue<out Runnable> /*需要并发处理的任务队列*/,
    val concurrentCount: Int = max(2, Runtime.getRuntime().availableProcessors()) /*并发数量*/,
    val onFinish: (error: Exception?) -> Unit /*任务处理完成*/
) {
    /**并发线程池*/
    private val executor: ThreadPoolExecutor
    private val runTaskQueue: ConcurrentLinkedQueue<Runnable>
    private val reentrantLock: ReentrantLock
    private val condition: Condition
    val isCancel: AtomicBoolean = AtomicBoolean(false)

    init {
        val taskSize = taskQueue.size
        val threadSize = taskSize + 1
        executor = ThreadPoolExecutor(
            threadSize, Int.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            SynchronousQueue()
        )

        runTaskQueue = ConcurrentLinkedQueue()
        reentrantLock = ReentrantLock()
        condition = reentrantLock.newCondition()

        executor.execute {
            while (taskQueue.isNotEmpty()) {
                if (isCancel.get()) {
                    break
                }
                if (runTaskQueue.size < concurrentCount) {
                    val task = taskQueue.poll()
                    runTaskQueue.add(task)
                    executor.execute {
                        try {
                            task.run()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            reentrantLock.lock()
                            condition.signalAll()

                            runTaskQueue.remove(task)

                            if (isCancel.get()) {
                                //任务取消
                            } else if (taskQueue.isEmpty() && runTaskQueue.isEmpty()) {
                                release()
                                onFinish(null)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            reentrantLock.unlock()
                        }
                    }
                } else {
                    try {
                        reentrantLock.lock()
                        condition.await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        reentrantLock.unlock()
                    }
                }
            }
        }
    }

    private fun release() {
        taskQueue.clear()
        runTaskQueue.clear()
        if (!executor.isShutdown) {
            executor.shutdownNow()
        }
        //L.d("释放资源.")
    }

    /**取消任务*/
    fun cancel() {
        if (!isCancel.get()) {
            isCancel.set(true)
            onFinish(CanceledException())
            release()
        }
    }

}