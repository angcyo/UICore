package com.angcyo.canvas.data

import android.graphics.Paint
import android.graphics.RectF
import android.widget.LinearLayout
import com.angcyo.canvas.data.CanvasProjectItemBean.Companion.MM_UNIT
import com.angcyo.canvas.graphics.GraphicsHelper
import com.angcyo.canvas.graphics.PathGraphicsParser
import com.angcyo.canvas.items.data.DataTextItem.Companion.TEXT_STYLE_BOLD
import com.angcyo.canvas.items.data.DataTextItem.Companion.TEXT_STYLE_DELETE_LINE
import com.angcyo.canvas.items.data.DataTextItem.Companion.TEXT_STYLE_ITALIC
import com.angcyo.canvas.items.data.DataTextItem.Companion.TEXT_STYLE_NONE
import com.angcyo.canvas.items.data.DataTextItem.Companion.TEXT_STYLE_UNDER_LINE
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.annotation.Implementation
import com.angcyo.library.annotation.MM
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.add
import com.angcyo.library.ex.ensure
import com.angcyo.library.ex.have
import com.angcyo.library.unit.MmValueUnit
import kotlin.math.max

/**
 * 渲染的数据, 用来保存和恢复. 长度单位统一使用mm, 毫米
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/20
 */
data class CanvasProjectItemBean(

    //region ---bounds---

    /**数据所在位置*/
    @MM
    var left: Float = 0f,

    @MM
    var top: Float = 0f,

    /**数据原始的宽高, 线条的长度*/
    @MM
    var width: Float = 0f,

    @MM
    var height: Float = 0f,

    /**旋转的角度*/
    var angle: Float = 0f,

    /**数据绘制时的缩放比例*/
    var scaleX: Float = 1f,

    var scaleY: Float = 1f,

    //endregion ---bounds---

    //region ---公共属性---

    /**数据id*/
    var id: Long = -1,

    /**数据唯一标识符*/
    var uuid: String? = null,

    /**图层代表图标, 如果有base64图片*/
    var icon: String? = null,

    /**数据类型, 线条类型的长度放在[width]属性中
     * [com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_TEXT]
     * [com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_RECT]
     * [com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_OVAL]
     * [com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_LINE]
     * */
    var mtype: Int = -1,

    /**图层名称, 如果不指定, 则通过[mtype]类型获取*/
    var name: String? = null,

    /**填充颜色, 形状的颜色*/
    var fill: String? = null,

    /**描边颜色*/
    var stroke: String? = null,

    /**
     * 0 [Paint.Style.FILL]
     * 1 [Paint.Style.STROKE]
     * 2 [Paint.Style.FILL_AND_STROKE]
     * */
    var paintStyle: Int = 0,

    /**原始的数据, 如svg文件内容, gcode文件内容*/
    var data: String? = null,

    /**是否可见*/
    var isVisible: Boolean = true,

    //endregion ---公共属性---

    //region ---文本类型---

    /**文本的内容
     * [com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_QRCODE]
     * [com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_BARCODE]
     * */
    var text: String? = null,

    /**二维码编码格式, 编码格式（qrcode）, 编码格式（code128）*/
    @Implementation
    var coding: String? = null,

    /**纠错级别*/
    @Implementation
    var eclevel: String? = null,

    /**文本的对齐方式
     * [Paint.Align.toAlignString]
     * */
    var textAlign: String? = null,

    /**字体大小*/
    @MM
    var fontSize: Float = 10f,

    /**字间距*/
    @MM
    var charSpacing: Float = 0f,

    /**行间距*/
    @MM
    var lineSpacing: Float = 0f,

    /**字体名称*/
    var fontFamily: String? = null,

    /**是否加粗*/
    var fontWeight: String? = null,

    /**是否斜体*/
    var fontStyle: String? = null,

    /**下划线*/
    var underline: Boolean = false,

    /**删除线*/
    var linethrough: Boolean = false,

    /**文本排列方向*/
    var orientation: Int = LinearLayout.HORIZONTAL,

    /**绘制紧凑文本, 这种绘制方式的文本边框留白少*/
    var isCompactText: Boolean = true,

    /**文本颜色*/
    var textColor: String? = null,

    //endregion ---文本类型---

    //region ---形状---

    /**水平圆角半径, 矩形/椭圆
     * 椭圆矩形的宽度 = [rx] * 2
     * */
    @MM
    var rx: Float = 0f,

    /**垂直圆角半径, 矩形/椭圆
     * 椭圆矩形的高度 = [ry] * 2
     * */
    @MM
    var ry: Float = 0f,

    /**多边形的边数 5 [3-50]*/
    var side: Int = 3,

    /**星星的深度 40 [1-100], 深度越大内圈半径越小
     * 固定外圈半径, 那么 内圈半径 = 固定外圈半径 * (1-[depth] / 100)
     * */
    var depth: Int = 40,

    //endregion ---形状---

    //region ---SVG path数据---

    /**SVG数据
     * "[['M',0,0],['L',11,11]]"
     * */
    var path: String? = null,

    /**虚线线宽*/
    @MM
    var dashWidth: Float = 1f,

    /**虚线线距*/
    @MM
    var dashGap: Float = 1f,

    //endregion ---SVG path数据---

    //region ---图片数据---

    /** 原图数据 (data:image/xxx;base64,xxx) */
    var imageOriginal: String? = null,

    /**滤镜后显示图 string, 带协议头
     * [data] gcode数据*/
    var src: String? = null,

    /**图片滤镜
     * 图片滤镜 'black'(黑白) | 'seal'(印章) | 'gray'(灰度) | 'prints'(版画) | 'Jitter(抖动)' | 'gcode'
     * imageFilter 图片滤镜 1:黑白 | 2:印章 | 3:灰度 | 4:版画 | 5:抖动 | 6:gcode `2022-9-21`
     * */
    var imageFilter: Int = CanvasConstant.DATA_MODE_GREY,

    /** 对比度*/
    @Implementation
    var contrast: Float = 0f,

    /**亮度*/
    @Implementation
    var brightness: Float = 0f,

    /**黑白阈值*/
    @Implementation
    var blackThreshold: Float = DEFAULT_THRESHOLD,

    /**印章阈值*/
    @Implementation
    var sealThreshold: Float = DEFAULT_THRESHOLD,

    /**版画阈值*/
    @Implementation
    var printsThreshold: Float = DEFAULT_THRESHOLD,

    /**是否反色*/
    @Implementation
    var inverse: Boolean = false,

    /**gcode线距*/
    @Implementation
    var gcodeLineSpace: Float = 5f,

    /**gcode角度[0-90]*/
    @Implementation
    var gcodeAngle: Float = 0f,

    /**gcode方向 0:0 1:90 2:180 3:270*/
    @Implementation
    var gcodeDirection: Int = 0,

    /**是否扭曲*/
    var isMesh: Boolean = false,

    /**最小直径, 最大直径*/
    @MM
    var minDiameter: Float = 0f,

    @MM
    var maxDiameter: Float = 0f,

    /**扭曲类型,
     * "CONE" 圆锥
     * "BALL" 球体
     * */
    var meshShape: String? = null,

    //endregion ---图片数据---

    //region ---私有属性---

    /**数据处理的模式, 处理成机器需要的数据
     * [com.angcyo.canvas.utils.CanvasConstant.DATA_MODE_BLACK_WHITE]
     * [com.angcyo.canvas.utils.CanvasConstant.DATA_MODE_GCODE]
     * [com.angcyo.canvas.utils.CanvasConstant.DATA_MODE_DITHERING]
     *
     * [com.angcyo.canvas.graphics.IGraphicsParser.initDataMode]
     * */
    @Transient
    var _dataMode: Int? = null,

    //endregion ---私有属性---
) {

    companion object {

        /**毫米单位计算*/
        val MM_UNIT = MmValueUnit()

        /**默认的阈值*/
        const val DEFAULT_THRESHOLD = 240f

        /**默认的GCode线距*/
        const val DEFAULT_LINE_SPACE = 5.0f
    }

    /**设置渲染的位置
     * [bounds] 返回值*/
    fun updateToRenderBounds(@Pixel bounds: RectF): RectF {
        val valueUnit = MM_UNIT
        val l = valueUnit.convertValueToPixel(left)
        val t = valueUnit.convertValueToPixel(top)
        var w = valueUnit.convertValueToPixel(width)
        var h = valueUnit.convertValueToPixel(height)

        //限制大小
        w = max(PathGraphicsParser.MIN_PATH_SIZE, w)
        h = max(PathGraphicsParser.MIN_PATH_SIZE, h)

        val sx = if (scaleX == 0f) 1f else scaleX
        val sy = if (scaleY == 0f) 1f else scaleY
        bounds.set(l, t, l + w * sx, t + h * sy)
        return bounds
    }

    /**更新缩放比例
     * [w] 界面上显示的大小, 像素
     * [h] 界面上显示的大小, 像素*/
    fun updateScale(@Pixel w: Float, @Pixel h: Float) {
        val valueUnit = MM_UNIT
        if (w != 0f && width != 0f) {
            scaleX = (valueUnit.convertPixelToValue(w) / width).ensure()
        }
        if (h != 0f && height != 0f) {
            scaleY = (valueUnit.convertPixelToValue(h) / height).ensure()
        }
    }

    /**更新坐标, 缩放比例数据*/
    fun updateByBounds(@Pixel bounds: RectF) {
        val valueUnit = MM_UNIT
        left = valueUnit.convertPixelToValue(bounds.left)
        top = valueUnit.convertPixelToValue(bounds.top)

        val width = bounds.width()
        val height = bounds.height()
        updateScale(width, height)
    }

    /**复制元素
     * [offset] 是否开启偏移, 会在原数据的基础上+上偏移量*/
    fun copyBean(offset: Boolean = false): CanvasProjectItemBean {
        val newBean = copy()
        if (offset) {
            newBean.left += GraphicsHelper.POSITION_STEP
            newBean.top += GraphicsHelper.POSITION_STEP
        }
        return newBean
    }
}

//---

/**对齐方式*/
fun Paint.Align.toAlignString(): String = when (this) {
    Paint.Align.CENTER -> "center"
    Paint.Align.LEFT -> "left"
    Paint.Align.RIGHT -> "right"
    else -> "left"
}

fun String?.toPaintAlign(): Paint.Align = when (this) {
    "center" -> Paint.Align.CENTER
    "left" -> Paint.Align.LEFT
    "right" -> Paint.Align.RIGHT
    else -> Paint.Align.LEFT
}

//---

/**文本样式*/
fun Paint.Style.toPaintStyleInt(): Int = when (this) {
    Paint.Style.FILL -> 0
    Paint.Style.STROKE -> 1
    Paint.Style.FILL_AND_STROKE -> 2
    else -> 0
}

fun Int?.toPaintStyle(): Paint.Style = when (this) {
    0 -> Paint.Style.FILL
    1 -> Paint.Style.STROKE
    2 -> Paint.Style.FILL_AND_STROKE
    else -> Paint.Style.FILL
}

//---

/**文本演示*/
fun CanvasProjectItemBean.textStyle(): Int {
    var result = TEXT_STYLE_NONE
    if (isBold()) {
        result = result.add(TEXT_STYLE_BOLD)
    }
    if (isItalic()) {
        result = result.add(TEXT_STYLE_ITALIC)
    }
    if (underline) {
        result = result.add(TEXT_STYLE_UNDER_LINE)
    }
    if (linethrough) {
        result = result.add(TEXT_STYLE_DELETE_LINE)
    }
    return result
}

/**设置文本样式*/
fun CanvasProjectItemBean.setTextStyle(style: Int) {
    fontWeight = if (style.have(TEXT_STYLE_BOLD)) "bold" else null
    fontStyle = if (style.have(TEXT_STYLE_ITALIC)) "italic" else null
    underline = style.have(TEXT_STYLE_UNDER_LINE)
    linethrough = style.have(TEXT_STYLE_DELETE_LINE)
}

/**是否加粗*/
fun CanvasProjectItemBean.isBold() = fontWeight == "bold"

/**是否斜体*/
fun CanvasProjectItemBean.isItalic() = fontStyle == "italic"

/**毫米转像素*/
fun Float?.toPixel() = MM_UNIT.convertValueToPixel(this ?: 0f)

/**MmValueUnit*/
fun Int.toMm() = toFloat().toMm()

/**像素转毫米*/
fun Float?.toMm() = MM_UNIT.convertPixelToValue(this ?: 0f)