package com.angcyo.item.style

import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.library.ex.ResultThrowable

/**
 * 需要动态加载数据的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface ILoadItem : IDslItem {

    /**触发此方法, 异步加载数据
     * [error] 是否加载异常*/
    var itemLoadAction: ((result: ResultThrowable) -> Unit)?

    fun configItemLoadAction(action: (result: ResultThrowable?) -> Unit) {
        //配置成员
        itemLoadAction = {
            action(it)
        }
        //触发回调
        action(null)
    }
}