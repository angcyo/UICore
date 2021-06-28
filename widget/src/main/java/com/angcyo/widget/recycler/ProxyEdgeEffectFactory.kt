package com.angcyo.widget.recycler

import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ProxyEdgeEffectFactory(
    val left: EdgeEffect? = null,
    val top: EdgeEffect? = null,
    val right: EdgeEffect? = null,
    val bottom: EdgeEffect? = null
) : RecyclerView.EdgeEffectFactory() {

    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
        return when (direction) {
            DIRECTION_LEFT -> left ?: EdgeEffect(view.context)
            DIRECTION_TOP -> top ?: EdgeEffect(view.context)
            DIRECTION_RIGHT -> right ?: EdgeEffect(view.context)
            DIRECTION_BOTTOM -> bottom ?: EdgeEffect(view.context)
            else -> EdgeEffect(view.context)
        }
    }
}