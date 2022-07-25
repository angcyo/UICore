package com.angcyo.doodle.core

import android.graphics.Canvas
import com.angcyo.doodle.DoodleDelegate
import com.angcyo.library.annotation.CallPoint

/**
 *
 * 图层管理, 所有层在此管理, 层上还会有很多元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleLayerManager(val doodleDelegate: DoodleDelegate) {

    /**透明底层*/
    val alphaElement = AlphaElement()

    @CallPoint
    fun onDraw(canvas: Canvas) {
        alphaElement.onDraw(canvas)
    }

}