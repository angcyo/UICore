package com.angcyo.doodle.brush.element

import android.graphics.Path
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseElement
import com.angcyo.library.ex.bezier

/**
 * 笔刷绘制元素, 数据收集和处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseBrushElement : BaseElement() {

    /**路径*/
    var brushPath: Path? = null

    /**激活曲线*/
    var enableBezier: Boolean = true

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
        brushPath?.apply {
            if (point.isFirst) {
                moveTo(point.eventX, point.eventY)
            } else {
                if (enableBezier) {
                    val prevPoint = pointList[pointList.lastIndex - 1]
                    val c1x = (prevPoint.eventX + point.eventX) / 2
                    val c1y = (prevPoint.eventY + point.eventY) / 2
                    bezier(c1x, c1y, point.eventX, point.eventY)
                } else {
                    lineTo(point.eventX, point.eventY)
                }
            }
        }
    }

    /**完成绘制元素数据*/
    open fun onFinishElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {

    }

}