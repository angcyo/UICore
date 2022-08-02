package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Path
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

    /**路径*/
    var brushPath: Path? = null

    /**根据指定的数据, 创建一个绘制元素*/
    open fun onCreateElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ) {
        brushPath = Path()
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

    /**根据滑动速度, 返回应该绘制的宽度.
     * 速度越快, 宽度越细
     * */
    open fun selectPaintWidth(speed: Float): Float {
        val minSpeed = 0f
        val maxSpeed = 10f
        val currentSpeed = clamp(speed, minSpeed, maxSpeed)

        val speedRatio = (currentSpeed - minSpeed) / (maxSpeed - minSpeed)

        val minWidth = 4
        val maxWidth = brushElementData.paintWidth

        return minWidth + (1 - speedRatio) * (maxWidth - minWidth)
    }

    //endregion ---operate---
}