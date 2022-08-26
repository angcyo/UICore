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

    //region ---数据类型---

    /**数据类型, 图片数据*/
    const val DATA_TYPE_BITMAP = 1

    /**数据类型, 二维码*/
    const val DATA_TYPE_QRCODE = 2

    /**数据类型, 条形维码*/
    const val DATA_TYPE_BARCODE = 3

    /**数据类型, 文本*/
    const val DATA_TYPE_TEXT = 4

    /**数据类型, SVG数据*/
    const val DATA_TYPE_SVG = 5

    /**数据类型, GCODE数据*/
    const val DATA_TYPE_GCODE = 6

    /**数据类型, Path数据*/
    const val DATA_TYPE_PATH = 7

    /**数据类型, 一组数据*/
    const val DATA_TYPE_GROUP = 10

    //endregion ---数据类型---

    //region ---数据处理模式---

    /**数据模式, 版画*/
    const val DATA_MODE_PRINT = 1

    /**数数据模式, 黑白*/
    const val DATA_MODE_BLACK_WHITE = 2

    /**数据模式, 抖动*/
    const val DATA_MODE_DITHERING = 3

    /**数据模式, 灰度*/
    const val DATA_MODE_GREY = 4

    /**数据模式, 印章*/
    const val DATA_MODE_SEAL = 5

    /**数据模式, GCode*/
    const val DATA_MODE_GCODE = 10

    //endregion ---数据处理模式---

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

    /**单位*/
    val valueUnit: IValueUnit
        get() = when (CANVAS_VALUE_UNIT) {
            CANVAS_VALUE_UNIT_PIXEL -> PixelValueUnit()
            CANVAS_VALUE_UNIT_INCH -> InchValueUnit()
            else -> MmValueUnit()
        }

    //endregion ---Canvas设置项---
}