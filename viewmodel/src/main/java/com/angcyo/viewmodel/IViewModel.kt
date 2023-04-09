package com.angcyo.viewmodel

import com.angcyo.library.component.ICancel

/**
 * [ViewModel]基础方法
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/25
 */
interface IViewModel : ICancel {

    /**重置状态*/
    fun reset() {

    }

    /**释放资源*/
    fun release() {

    }

}