package com.angcyo.doodle.element

import android.graphics.Canvas
import com.angcyo.doodle.core.IDoodleItem
import com.angcyo.doodle.layer.BaseLayer

/**
 * 绘制的最小单位
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IElement : IDoodleItem {

    fun onDraw(layer: BaseLayer, canvas: Canvas)

}