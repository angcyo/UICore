package com.angcyo.widget.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class GridLayoutManagerWrap : GridLayoutManager {

    constructor(context: Context, spanCount: Int) : super(context, spanCount)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context,
        spanCount: Int,
        orientation: Int,
        reverseLayout: Boolean
    ) : super(context, spanCount, orientation, reverseLayout)

    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: java.lang.Exception) {
            L.e("GridLayoutManagerWrap onLayoutChildren异常-> " + e.message)
            e.printStackTrace()
        }
    }
}