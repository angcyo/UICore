package com.angcyo.library.component

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2024/04/18
 *
 * 任务管理器
 */
class TaskManager {

    /**所有的任务*/
    val taskList = mutableListOf<ITask>()

    /**任务监听*/
    var taskListener = mutableListOf<TaskListener>()

    /**当前正在指定的任务*/
    var currentTask: ITask? = null

    /**是否取消任务*/
    var cancel = false

    /**当前任务的索引*/
    val currentTaskIndex get() = taskList.indexOf(currentTask) + 1

    /**任务总数*/
    val taskCount get() = taskList.size

    /**添加一个任务*/
    fun addTask(task: ITask) {
        task.taskManager = this
        taskList.add(task)
    }

    /**从当前位置, 开始执行任务*/
    fun startTask() {
        if (taskList.isEmpty() || cancel) {
            return
        }
        if (currentTask == null) {
            currentTask = taskList.first()
        }
        if (currentTask?.taskState == TaskState.Cancel) {
            //被取消的任务, 跳过
            nextTask(currentTask!!)
            return
        }
        currentTask?.taskState = TaskState.Start
        taskListener.forEach {
            it.onTaskStart(currentTask!!)
        }
        currentTask?.runTask()
    }

    /**从头开始执行任务*/
    fun restartTask() {
        currentTask = null
        startTask()
    }

    /**取消任务的执行, 不能中断当前正在执行的任务*/
    fun cancel() {
        currentTask?.taskState = TaskState.Cancel
        cancel = true
    }

    /**执行下一个任务
     * [task] 当前执行的任务
     * [error] 任务执行异常
     * [continueOnError] 异常时是否继续执行下一个任务
     * */
    fun nextTask(
        task: ITask,
        error: Throwable? = null,
        continueOnError: Boolean = false,
        delay: Long = 0
    ) {
        if (cancel) {
            return
        }
        if (delay > 0) {
            _delay(delay) {
                nextTask(task, error, continueOnError)
            }
            return
        }
        if (task.taskState != TaskState.Cancel) {
            if (error == null) {
                task.taskState = TaskState.End
                taskListener.forEach {
                    it.onTaskEnd(task)
                }
            } else {
                task.taskState = TaskState.Error
                taskListener.forEach {
                    it.onTaskError(task, error)
                }
            }
        }
        if (error == null || continueOnError) {
            val index = taskList.indexOf(task)
            if (index == -1) {
                //所有任务结束
                taskListener.forEach {
                    it.onTaskAllEnd()
                }
            } else {
                if (index + 1 < taskList.size) {
                    currentTask = taskList[index + 1]
                    startTask()
                } else {
                    //所有任务结束
                    taskListener.forEach {
                        it.onTaskAllEnd()
                    }
                }
            }
        }
    }

    /**任务监听*/
    abstract class TaskListener {
        /**开始执行任务*/
        open fun onTaskStart(task: ITask) {}

        /**执行任务异常*/
        open fun onTaskError(task: ITask, error: Throwable) {}

        /**执行任务结束*/
        open fun onTaskEnd(task: ITask) {}

        /**所有任务完成结束*/
        open fun onTaskAllEnd() {}
    }

    /**定义一个任务*/
    abstract class ITask {

        /**任务管理对象, 自动赋值
         * 调用next方法, 继续执行任务
         * [com.angcyo.library.component.TaskManager.nextTask]*/
        var taskManager: TaskManager? = null

        /**当前任务的状态*/
        var taskState = TaskState.Normal

        abstract fun runTask()
    }

    enum class TaskState {
        Normal,
        Start,
        End,
        Cancel,
        Error,
    }
}

