package com.angcyo.library.component.sync

import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.sync.ISyncTask.Companion.toSyncStateStr
import com.angcyo.library.ex.classHash

/**
 * 同步管理器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/27
 */
class SyncManager {

    /**需要同步的任务列表*/
    val syncTaskList = mutableListOf<ISyncTask<*>>()

    /**同步管理的状态*/
    var syncState: Int = ISyncTask.STATE_NONE

    /**开始同步任务, 一个任务完成后, 继续下一个任务
     * [restart] 当全部的任务已完成时, 是否重新开始
     * @return 启动操作是否完成*/
    @CallPoint
    fun startSyncTask(restart: Boolean = true): Boolean {
        if (isAllFinish()) {
            if (restart) {
                syncState = ISyncTask.STATE_NONE
                updateAllTaskState(ISyncTask.STATE_NONE)
            } else {
                return false
            }
        }
        syncState = ISyncTask.STATE_START
        for (syncTask in syncTaskList) {
            if (syncTask.syncState == ISyncTask.STATE_START) {
                //已有任务在同步中
                L.w("已有同步任务运行中:${syncTask.classHash()}")
                return true
            }
        }
        for (syncTask in syncTaskList) {
            if (syncTask.syncState == ISyncTask.STATE_NONE) {
                L.i("开始同步任务:${syncTask.classHash()}")
                syncTask.startSync {
                    L.i("同步任务状态改变:${syncTask.classHash()}:${it.syncState.toSyncStateStr()}")
                    if (it.syncState == ISyncTask.STATE_FINISH ||
                        it.syncState == ISyncTask.STATE_ERROR
                    ) {
                        if (syncState == ISyncTask.STATE_START) {
                            //一个任务完成后, 继续下一个任务
                            startSyncTask(false)//此时不需要重新开始
                        } else {
                            //已经取消了同步
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    /**检查所有的任务是否已完成*/
    fun isAllFinish(): Boolean {
        for (syncTask in syncTaskList) {
            if (syncTask.syncState != ISyncTask.STATE_FINISH &&
                syncTask.syncState != ISyncTask.STATE_ERROR
            ) {
                return false
            }
        }
        return true
    }

    /**取消所有任务*/
    fun cancelSyncTask() {
        syncState = ISyncTask.STATE_NONE
        updateAllTaskState(ISyncTask.STATE_NONE)
    }

    /**更新所有任务状态*/
    fun updateAllTaskState(state: Int) {
        for (syncTask in syncTaskList) {
            syncTask.syncState = state
        }
    }

}