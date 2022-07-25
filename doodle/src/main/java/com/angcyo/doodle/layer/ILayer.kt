package com.angcyo.doodle.layer

import android.graphics.Canvas
import com.angcyo.doodle.core.IDoodleItem
import com.angcyo.doodle.element.IElement

/**
 * 层, 管理着上面的所有元素
 *
 * [IElement]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ILayer : IDoodleItem {

    fun onDraw(canvas: Canvas)

}