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

    /**配置类*/
    var loadItemConfig: LoadItemConfig

    /**配置动态加载数据的action*/
    fun configItemLoadAction(
        enableCache: Boolean = loadItemConfig.itemUseLoadCache,
        action: (result: ResultThrowable?) -> Unit
    ) {
        //是否使用缓存
        loadItemConfig.itemUseLoadCache = enableCache
        //配置成员
        loadItemConfig.itemLoadAction = {
            action(it)
        }
        //触发回调
        action(null)
    }
}

typealias LoadResultAction = (result: ResultThrowable) -> Unit

var ILoadItem.itemUseLoadCache: Boolean
    get() = loadItemConfig.itemUseLoadCache
    set(value) {
        loadItemConfig.itemUseLoadCache = value
    }

var ILoadItem.itemLoadAction: LoadResultAction?
    get() = loadItemConfig.itemLoadAction
    set(value) {
        loadItemConfig.itemLoadAction = value
    }

class LoadItemConfig : IDslItemConfig {

    /**是否使用加载后的缓存*/
    var itemUseLoadCache: Boolean = true

    /**触发此方法, 异步加载数据
     * [error] 是否加载异常*/
    var itemLoadAction: LoadResultAction? = null
}