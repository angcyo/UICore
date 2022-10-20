package com.angcyo.canvas.utils

import com.angcyo.canvas.utils.CanvasConstant.DATA_MODE_BLACK_WHITE
import com.angcyo.canvas.utils.CanvasConstant.DATA_MODE_DITHERING
import com.angcyo.canvas.utils.CanvasConstant.DATA_MODE_GCODE
import com.angcyo.canvas.utils.CanvasConstant.DATA_MODE_GREY
import com.angcyo.canvas.utils.CanvasConstant.DATA_MODE_PRINT
import com.angcyo.canvas.utils.CanvasConstant.DATA_MODE_SEAL
import com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_BARCODE
import com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_BITMAP
import com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_GCODE
import com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_GROUP
import com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_PATH
import com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_QRCODE
import com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_SVG
import com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_TEXT
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
object CanvasConstant {

    //region ---数据类型---

    /**数据类型, 图片数据*/
    const val DATA_TYPE_BITMAP = 10010

    /**数据类型, 文本*/
    const val DATA_TYPE_TEXT = 10001

    /**数据类型, 二维码*/
    const val DATA_TYPE_QRCODE = 10015

    /**数据类型, 条形维码*/
    const val DATA_TYPE_BARCODE = 10016

    /**数据类型, 矩形*/
    const val DATA_TYPE_RECT = 10005

    /**数据类型, 椭圆*/
    const val DATA_TYPE_OVAL = 10006

    /**数据类型, 线条*/
    const val DATA_TYPE_LINE = 10008

    /**数据类型, 钢笔*/
    const val DATA_TYPE_PEN = 10002

    /**数据类型, 画笔*/
    const val DATA_TYPE_BRUSH = 10003

    /**数据类型, SVG数据/导入进来的矢量元素*/
    const val DATA_TYPE_SVG = 10004

    /**数据类型, 多边形*/
    const val DATA_TYPE_POLYGON = 10007

    /**数据类型, 星星*/
    const val DATA_TYPE_PENTAGRAM = 10009

    /**数据类型, 爱心*/
    const val DATA_TYPE_LOVE = 10012

    /**数据类型, 单线字*/
    const val DATA_TYPE_SINGLE_WORD = 10013

    /**数据类型, GCODE数据*/
    const val DATA_TYPE_GCODE = 10020

    /**数据类型, Path数据*/
    const val DATA_TYPE_PATH = 7

    /**数据类型, 一组数据*/
    const val DATA_TYPE_GROUP = 10

    //endregion ---数据类型---

    //region ---数据处理模式---

    /**数数据模式, 黑白, 发送线段数据
     * [com.angcyo.bluetooth.fsc.laserpacker.command.DataCmd.ENGRAVE_TYPE_BITMAP_PATH]
     * */
    const val DATA_MODE_BLACK_WHITE = 101

    /**数据模式, 抖动, 发送抖动数据
     * [com.angcyo.bluetooth.fsc.laserpacker.command.DataCmd.ENGRAVE_TYPE_BITMAP_DITHERING]
     * */
    const val DATA_MODE_DITHERING = 105

    /**数据模式, GCode, 发送GCode数据
     * [com.angcyo.bluetooth.fsc.laserpacker.command.DataCmd.ENGRAVE_TYPE_GCODE]
     * */
    const val DATA_MODE_GCODE = 106

    //

    /**数据模式, 版画*/
    const val DATA_MODE_PRINT = 4

    /**数据模式, 灰度*/
    const val DATA_MODE_GREY = 3

    /**数据模式, 印章*/
    const val DATA_MODE_SEAL = 2

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

    //region ---支持的文件类型---

    /**创作支持的数据类型*/
    const val GCODE_EXT = ".gcode"

    const val SVG_EXT = ".svg"

    /**文本后缀, 检查文本内容*/
    const val TXT_EXT = ".txt"

    /**工程文件后缀*/
    const val PROJECT_EXT = ".lp"

    //endregion ---支持的文件类型---

    //region ---目录分配---

    /**雕刻缓存文件的文件夹*/
    const val ENGRAVE_FILE_FOLDER = "engrave"

    /**GCode/Svg矢量缓存目录*/
    const val VECTOR_FILE_FOLDER = "vector"

    /**工程文件存储目录*/
    const val PROJECT_FILE_FOLDER = "project"

    //endregion ---目录分配---

}

fun Int.toDataTypeStr() = when (this) {
    DATA_TYPE_BITMAP -> "Bitmap"
    DATA_TYPE_QRCODE -> "QrCode"
    DATA_TYPE_BARCODE -> "BarCode"
    DATA_TYPE_TEXT -> "Text"
    DATA_TYPE_SVG -> "Svg"
    DATA_TYPE_GCODE -> "GCode"
    DATA_TYPE_PATH -> "Path"
    DATA_TYPE_GROUP -> "Group"
    else -> "DataType-${this}"
}

fun Int.toDataModeStr() = when (this) {
    DATA_MODE_PRINT -> "版画"
    DATA_MODE_BLACK_WHITE -> "黑白"
    DATA_MODE_DITHERING -> "抖动"
    DATA_MODE_GREY -> "灰度"
    DATA_MODE_SEAL -> "印章"
    DATA_MODE_GCODE -> "GCode"
    else -> "DataMode-${this}"
}