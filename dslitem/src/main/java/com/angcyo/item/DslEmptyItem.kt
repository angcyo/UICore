package com.angcyo.item

import android.view.ViewGroup
import com.angcyo.dsladapter.DslAdapterItem

/**
 * 空的占位item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslEmptyItem : DslAdapterItem() {
    init {
        itemLayoutId = R.layout.lib_empty_item
        itemWidth = ViewGroup.LayoutParams.MATCH_PARENT
    }
}