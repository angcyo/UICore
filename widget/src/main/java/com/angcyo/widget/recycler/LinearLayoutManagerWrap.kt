package com.angcyo.widget.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.library.L.e

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class LinearLayoutManagerWrap : LinearLayoutManager {

    constructor(context: Context) : super(context)

    constructor(
        context: Context,
        orientation: Int,
        reverseLayout: Boolean
    ) : super(context, orientation, reverseLayout)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
            e("LinearLayoutManagerWrap onLayoutChildren异常-> " + e.message)
            e.printStackTrace()
        }
    }
}