package com.angcyo.library.component.work

import java.util.*
import java.util.concurrent.Executor

/**
 * A [Executor] which delegates to another [Executor] but ensures that tasks are
 * executed serially, like a single threaded executor.
 */
class SerialExecutor(val delegatedExecutor: Executor) : Executor {

    private val tasks: ArrayDeque<Task> = ArrayDeque()
    private val lock: Any = Any()

    @Volatile
    private var active: Runnable? = null

    override fun execute(command: Runnable) {
        synchronized(lock) {
            tasks.add(Task(this, command))
            if (active == null) {
                scheduleNext()
            }
        }
    }

    // Synthetic access
    fun scheduleNext() {
        synchronized(lock) {
            if (tasks.poll().also { active = it } != null) {
                delegatedExecutor.execute(active)
            }
        }
    }

    /**
     * @return `true` if there are tasks to execute in the queue.
     */
    fun hasPendingTasks(): Boolean {
        synchronized(lock) { return !tasks.isEmpty() }
    }

    /**
     * A [Runnable] which tells the [SerialExecutor] to schedule the next command
     * after completion.
     */
    class Task(val serialExecutor: SerialExecutor, val runnable: Runnable) : Runnable {
        override fun run() {
            try {
                runnable.run()
            } finally {
                serialExecutor.scheduleNext()
            }
        }
    }

}