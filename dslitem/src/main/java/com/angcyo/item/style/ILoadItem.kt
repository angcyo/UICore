package com.angcyo.item.style

import android.view.View
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.library.ex.ResultThrowable
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.visible

/**
 * 需要动态加载数据的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface ILoadItem : IAutoInitItem {

    /**配置类*/
    var loadItemConfig: LoadItemConfig

    fun initLoadingItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.v<View>(loadItemConfig.itemLoadingViewId)?.visible(loadItemConfig.itemIsLoading)
    }

    /**配置动态加载数据的action*/
    fun configItemLoadAction(
        enableCache: Boolean = loadItemConfig.itemUseLoadCache,
        action: LoadResultAction? = null
    ) {
        //是否使用缓存
        loadItemConfig.itemUseLoadCache = enableCache
        //配置成员
        loadItemConfig.itemLoadAction = {
            //加载之后的回调
            action?.invoke(it)
        }
        //触发回调
        startItemLoading(true)
    }

    /**设置开始加载*/
    fun startItemLoading(loading: Boolean = true, result: ResultThrowable? = null) {
        if (loadItemConfig.itemIsLoading != loading) {
            loadItemConfig.itemIsLoading = loading
            val item = this

            if (loading) {
                loadItemConfig.itemLoadAction?.invoke {
                    startItemLoading(false)
                    result?.invoke(it)
                }
            }

            if (item is DslAdapterItem) {
                item.updateAdapterItem()
            }
        }
    }
}

typealias LoadResultAction = (result: ResultThrowable?) -> Unit

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

    /**item是否正在加载中*/
    var itemIsLoading: Boolean = false

    /**
     * 加载提示的控件
     * [R.id.lib_loading_view]*/
    var itemLoadingViewId: Int = R.id.lib_loading_view

    /**配置此方法, 用来实现异步加载
     * 触发此方法, 异步加载数据
     * [error] 是否加载异常,
     * 异步加载完成之后, 调用[ResultThrowable]结束异步请求*/
    var itemLoadAction: LoadResultAction? = null
}