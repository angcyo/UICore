package com.angcyo.canvas.utils

import com.angcyo.canvas.core.IValueUnit
import com.angcyo.canvas.core.InchValueUnit
import com.angcyo.canvas.core.MmValueUnit
import com.angcyo.canvas.core.PixelValueUnit
import com.angcyo.library.component.HawkPropertyValue

/**
 * 常量
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/15
 */
object CanvasConstant {

    //region ---图片处理模式---

    /**图片模式, 版画*/
    const val BITMAP_MODE_PRINT = 1

    /**GCode*/
    const val BITMAP_MODE_GCODE = 2

    /**黑白*/
    const val BITMAP_MODE_BLACK_WHITE = 3

    /**抖动*/
    const val BITMAP_MODE_DITHERING = 4

    /**灰度*/
    const val BITMAP_MODE_GREY = 5

    /**印章*/
    const val BITMAP_MODE_SEAL = 6

    //endregion ---图片处理模式---

    //region ---Canvas---

    /**像素单位*/
    const val CANVAS_VALUE_UNIT_PIXEL = 1

    /**厘米单位*/
    const val CANVAS_VALUE_UNIT_MM = 2

    /**英寸单位*/
    const val CANVAS_VALUE_UNIT_INCH = 3

    /**单温状态, 持久化*/
    var CANVAS_VALUE_UNIT: Int by HawkPropertyValue<Any, Int>(2)

    /**是否开启智能指南, 持久化*/
    var CANVAS_SMART_ASSISTANT: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**单位*/
    val valueUnit: IValueUnit
        get() = when (CANVAS_VALUE_UNIT) {
            CANVAS_VALUE_UNIT_PIXEL -> PixelValueUnit()
            CANVAS_VALUE_UNIT_INCH -> InchValueUnit()
            else -> MmValueUnit()
        }

    //endregion ---Canvas---
}