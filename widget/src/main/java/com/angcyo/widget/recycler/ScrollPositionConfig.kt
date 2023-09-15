package com.angcyo.widget.recycler

import androidx.recyclerview.widget.RecyclerView

/**
 * 记录[RecyclerView]的滚动位置状态
 *
 * [androidx.recyclerview.widget.RecyclerView.saveScrollPosition]
 * [androidx.recyclerview.widget.RecyclerView.restoreScrollPosition]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

data class ScrollPositionConfig(

    /**第一个可见item的位置*/
    var adapterPosition: Int = RecyclerView.NO_POSITION,

    /**第一个可见item的left*/
    var left: Int = 0,

    /**第一个可见item的top*/
    var top: Int = 0
)