package com.angcyo.doodle.element

import android.graphics.Canvas
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.ex.clamp

/**
 * 笔刷绘制元素, 数据收集和处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseBrushElement(val brushElementData: BrushElementData) : BaseElement() {

    //region ---core---

    /**根据指定的数据, 创建一个绘制元素*/
    open fun onCreateElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ) {

    }

    /**更新绘制元素*/
    open fun onUpdateElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
    }

    /**完成绘制元素数据*/
    open fun onFinishElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {

    }

    /**核心绘制方法*/
    override fun onDraw(layer: BaseLayer, canvas: Canvas) {

    }

    //endregion ---core---

    //region ---operate---

    //endregion ---operate---
}