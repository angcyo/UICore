package com.angcyo.library.component.sync

import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
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

    /**开始同步任务, 一个任务完成后, 继续下一个任务*/
    @CallPoint
    fun startSyncTask(): Boolean {
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
                    L.i("同步任务状态改变:${syncTask.classHash()}:${it.syncState}")
                    if (it.syncState == ISyncTask.STATE_FINISH ||
                        it.syncState == ISyncTask.STATE_ERROR
                    ) {
                        if (syncState == ISyncTask.STATE_START) {
                            //一个任务完成后, 继续下一个任务
                            startSyncTask()
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

    /**取消所有任务*/
    fun cancelSyncTask() {
        syncState = ISyncTask.STATE_NONE
        for (syncTask in syncTaskList) {
            syncTask.syncState = ISyncTask.STATE_NONE
        }
    }

}