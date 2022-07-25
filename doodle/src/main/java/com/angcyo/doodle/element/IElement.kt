package com.angcyo.doodle.element

import android.graphics.Canvas

/**
 * 绘制的最小单位
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IElement {

    fun onDraw(canvas: Canvas)

}