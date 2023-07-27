package com.angcyo.library.component.sync

import androidx.annotation.WorkerThread
import com.angcyo.library.annotation.ThreadSync

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/27
 */

/**任务状态改变通知*/
interface ISyncTask<Entity : ISyncEntity> {

    /**获取当前同步的状态*/
    var syncState: Int

    /**开始同步, 此步骤可以拉取云端数据, 同步到本地, 然后再进行更新/下载/上传等操作
     * [changeAction] 当当前的任务状态改变时, 回调*/
    fun startSync(changeAction: (task: ISyncTask<Entity>) -> Unit)

    //region ---同步操作---

    /**开始同步记录
     * 请在工作线程中调用
     * */
    @WorkerThread
    fun startSyncEntity()

    /**获取下一个需要同步的记录, 如果有*/
    fun getNextSyncEntity(): Entity?

    /**开始同步实体*/
    @WorkerThread
    @ThreadSync
    fun syncEntity(entity: Entity)

    //endregion ---同步操作---

    companion object {
        /**同步状态*/
        const val STATE_NONE = 0
        const val STATE_START = 1

        //const val STATE_PROGRESS = 2
        const val STATE_FINISH = 3
        const val STATE_ERROR = 4
    }
}