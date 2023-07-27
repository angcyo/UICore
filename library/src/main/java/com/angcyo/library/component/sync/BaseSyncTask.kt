package com.angcyo.library.component.sync

import com.angcyo.library.annotation.ThreadSync

/**
 * 同步任务
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/27
 */
abstract class BaseSyncTask<Entity : ISyncEntity> : ISyncTask<Entity> {

    /**当前任务的同步状态*/
    override var syncState: Int = ISyncTask.STATE_NONE

    protected var changeAction: ((task: ISyncTask<Entity>) -> Unit)? = null

    override fun startSync(changeAction: (task: ISyncTask<Entity>) -> Unit) {
        this.changeAction = changeAction
        syncState = ISyncTask.STATE_START
    }

    override fun startSyncEntity() {
        var entity = getNextSyncEntity()
        while (entity != null) {
            syncEntity(entity)
            entity = getNextSyncEntity()
        }

        //end
        finishSync()
    }

    override fun syncEntity(entity: Entity) {
        if (syncState == ISyncTask.STATE_START) {
            if (entity.isSync()) {
                //已经同步过了
            } else {
                //开始同步
                checkPush(entity)
                checkPull(entity)
            }
        }
    }

    /**完成当前任务的同步, 进行下一个任务*/
    protected open fun finishSync() {
        syncState = ISyncTask.STATE_FINISH
        changeAction?.invoke(this)
    }

    /**将合适的数据推到远程, 比如上传附件后更新url等*/
    @ThreadSync
    abstract fun checkPush(entity: Entity)

    /**将合适的数据拉下来, 比如附件需要下载等*/
    @ThreadSync
    abstract fun checkPull(entity: Entity)
}