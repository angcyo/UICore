package com.angcyo.dsladapter.filter

import com.angcyo.dsladapter.DslAdapterItem

/**
 * 当数量不足时, 显示媒体添加item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class AddMediaFilterAfterInterceptor : FilterAfterInterceptor {

    var maxMediaCount = Int.MAX_VALUE

    var addMediaDslAdapterItem: DslAdapterItem? = null

    override fun intercept(chain: FilterAfterChain): List<DslAdapterItem> {
        if (chain.requestList.size >= maxMediaCount) {
            return chain.requestList.subList(0, maxMediaCount)
        }

        addMediaDslAdapterItem?.apply {
            val result = mutableListOf<DslAdapterItem>()
            result.addAll(chain.requestList)
            result.add(this)
            return result
        }

        return chain.requestList
    }
}