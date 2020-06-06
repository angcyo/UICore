package com.angcyo.library.component

import com.angcyo.library.L
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

/**
 * 并发处理工具类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/01
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class RConcurrent(
    val concurrentCount: Int = 5 /*并发数量*/,
    val onTaskEmpty: () -> Unit /*当任务全部执行完*/
) {

    /**并发线程池*/
    private val executor: ThreadPoolExecutor
    private val taskQueue: ConcurrentLinkedQueue<Runnable>
    private val runTaskQueue: ConcurrentLinkedQueue<Runnable>
    private val reentrantLock: ReentrantLock = ReentrantLock()
    private val condition: Condition

    init {
        condition = reentrantLock.newCondition()

        val threadSize = concurrentCount + 1
        executor = ThreadPoolExecutor(
            threadSize, Int.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            SynchronousQueue()
        )
        taskQueue = ConcurrentLinkedQueue()
        runTaskQueue = ConcurrentLinkedQueue()

        executor.execute {
            while (true) {
                try {
                    reentrantLock.lock()

                    if (taskQueue.isEmpty()) {
                        L.i("等待任务中...")
                        condition.await()
                    }

                    if (runTaskQueue.size >= concurrentCount) {
                        L.i("任务执行队列已满...")
                        condition.await()
                    }

                    if (runTaskQueue.size < concurrentCount) {
                        val task = taskQueue.poll()
                        if (task != null) {
                            runTaskQueue.add(task)
                            //L.w("分发任务:${task.taskName}  ${taskQueue.size}:${runTaskQueue.size}")
                            executor.execute {
                                try {
                                    task.run()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                try {
                                    reentrantLock.lock()
                                    runTaskQueue.remove(task)
                                    //L.w("remove:${task.taskName}  ${taskQueue.size}:${runTaskQueue.size}")
                                    condition.signalAll()

                                    if (taskQueue.isEmpty()) {
                                        onTaskEmpty()
                                    }
                                } catch (e: Exception) {
                                } finally {
                                    reentrantLock.unlock()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    reentrantLock.unlock()
                }
            }
        }
    }

    /**添加一个任务到列表*/
    fun addTask(task: Runnable) {
        if (executor.isShutdown) {
            throw IllegalAccessException("资源已释放.")
        }
        taskQueue.add(task)
        notifyStart()
    }

    fun release() {
        taskQueue.clear()
        runTaskQueue.clear()
        if (!executor.isShutdown) {
            executor.shutdownNow()
        }
        //L.d("释放资源.")
    }

    private fun notifyStart() {
        try {
            reentrantLock.lock()

            if (taskQueue.isNotEmpty() || runTaskQueue.size < concurrentCount) {
                condition.signalAll()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reentrantLock.unlock()
        }
    }
}