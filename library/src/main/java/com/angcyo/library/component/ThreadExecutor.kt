package com.angcyo.library.component

import com.angcyo.library.isMain
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

/**
 * 线程调度器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

object ThreadExecutor : Executor {

    private val executorLock = Any()
    private var threadPoolExecutor: ThreadPoolExecutor

    private val THREAD_FACTORY: ThreadFactory = object : ThreadFactory {
        val THREAD_NAME_STEM: String = "ThreadExecutor_%d"
        private val mThreadId = AtomicInteger(0)

        override fun newThread(runnable: Runnable): Thread {
            val t = Thread(runnable)
            t.name = String.format(Locale.US, THREAD_NAME_STEM, mThreadId.getAndIncrement())
            return t
        }
    }

    init {
        val number = Runtime.getRuntime().availableProcessors()
        val corePoolSize = max(1, number)

        threadPoolExecutor = ThreadPoolExecutor(
            corePoolSize, Int.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(), THREAD_FACTORY
        )
    }

    override fun execute(command: Runnable) {
        synchronized(executorLock) {
            if (threadPoolExecutor.isShutdown) {
                throw IllegalStateException("ThreadExecutor is Shutdown")
            }
            threadPoolExecutor.execute(command)
        }
    }

    fun onMain(command: Runnable) {
        synchronized(executorLock) {
            MainExecutor.execute(command)
        }
    }
}

/**在主进程执行*/
fun onMain(check: Boolean = true, command: Runnable) {
    if (check && isMain()) {
        command.run()
    } else {
        ThreadExecutor.onMain(command)
    }
}