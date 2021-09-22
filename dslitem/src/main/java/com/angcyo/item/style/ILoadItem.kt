package com.angcyo.item.style

import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.library.ex.ResultThrowable

/**
 * 需要动态加载数据的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface ILoadItem : IDslItem {

    var loadItemConfig: LoadItemConfig

    fun configItemLoadAction(action: (result: ResultThrowable?) -> Unit) {
        //配置成员
        loadItemConfig.itemLoadAction = {
            action(it)
        }
        //触发回调
        action(null)
    }
}

class LoadItemConfig : IDslItemConfig {

    /**触发此方法, 异步加载数据
     * [error] 是否加载异常*/
    var itemLoadAction: ((result: ResultThrowable) -> Unit)? = null
}