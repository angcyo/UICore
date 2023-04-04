package com.angcyo.canvas.utils

import androidx.annotation.Keep
import com.angcyo.library.component.HawkPropertyValue
import com.angcyo.library.unit.IValueUnit
import com.angcyo.library.unit.InchValueUnit
import com.angcyo.library.unit.MmValueUnit
import com.angcyo.library.unit.PixelValueUnit

/**
 * 常量
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/15
 */

@Keep
object CanvasConstant {

    //region ---Canvas---

    /**像素单位*/
    const val CANVAS_VALUE_UNIT_PIXEL = 1

    /**厘米单位*/
    const val CANVAS_VALUE_UNIT_MM = 2

    /**英寸单位*/
    const val CANVAS_VALUE_UNIT_INCH = 3

    //endregion ---Canvas---

    //region ---Canvas设置项---

    /**单温状态, 持久化*/
    var CANVAS_VALUE_UNIT: Int by HawkPropertyValue<Any, Int>(2)

    /**是否开启智能指南, 持久化*/
    var CANVAS_SMART_ASSISTANT: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**是否开启网格绘制, 持久化*/
    var CANVAS_DRAW_GRID: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**单位*/
    val valueUnit: IValueUnit
        get() = when (CANVAS_VALUE_UNIT) {
            CANVAS_VALUE_UNIT_PIXEL -> PixelValueUnit()
            CANVAS_VALUE_UNIT_INCH -> InchValueUnit()
            else -> MmValueUnit()
        }

    //endregion ---Canvas设置项---
}