package com.angcyo.doodle.layer

import android.graphics.Canvas
import com.angcyo.doodle.DoodleDelegate
import com.angcyo.doodle.element.BaseElement

/**
 * 基础层
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseLayer(val doodleDelegate: DoodleDelegate) : ILayer {

    /**所有待绘制的元素列表*/
    val elementList = mutableListOf<BaseElement>()

    override fun onDraw(canvas: Canvas) {
        for (element in elementList) {
            element.onDraw(canvas)
        }
    }

}