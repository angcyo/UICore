package com.angcyo.library.component.sync

/**
 * 需要同步的实体
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/27
 */
interface ISyncEntity {

    /**当前记录的同步是否已完成
     * 已完成的记录, 不会进行任务操作*/
    fun isSync(): Boolean
}