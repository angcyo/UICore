package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.WorkerThread
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.data.toPaintStyleInt
import com.angcyo.canvas.data.toPixel
import com.angcyo.canvas.items.data.DataItemRenderer
import com.angcyo.canvas.items.data.DataTextItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.ShapesHelper
import com.angcyo.gcode.GCodeWriteHandler
import com.angcyo.library.annotation.MM
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.flip
import com.angcyo.library.ex.size
import com.angcyo.library.ex.toBase64Data
import java.io.StringWriter
import kotlin.math.pow
import kotlin.random.Random.Default.nextInt

/**
 * 扩展方法
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */

//region ---形状---

/**添加一根线
 * [length] 线的长度
 * */
fun CanvasDelegate.addLineRender(@Pixel length: Float = ShapesHelper.defaultWidth): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_LINE
    bean.width = length.toMm()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个椭圆, 宽高相等时, 就是圆了*/
fun CanvasDelegate.addOvalRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_OVAL
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    bean.rx = width / 2
    bean.ry = height / 2

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个矩形, 支持圆角*/
fun CanvasDelegate.addRectRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_RECT
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    //bean.rx = 0f
    //bean.ry = 0f

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个多边形, 支持任意边数
 * [com.angcyo.canvas.data.CanvasProjectItemBean.side]*/
fun CanvasDelegate.addPolygonRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_POLYGON
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    //多边形的边数[3-50]
    //bean.side = 3

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个星星, 支持任意边数, 和深度
 * [com.angcyo.canvas.data.CanvasProjectItemBean.side]
 * [com.angcyo.canvas.data.CanvasProjectItemBean.depth]
 * */
fun CanvasDelegate.addPentagramRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_PENTAGRAM
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    //多边形的边数[3-50]
    bean.side = 5
    //星星的深度[1-100]
    //bean.depth = 40

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个爱心*/
fun CanvasDelegate.addLoveRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_LOVE
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

//endregion ---形状---

//region ---矢量---

/**SVG数据转[CanvasProjectItemBean]*/
fun String?.toSvgItemData(): CanvasProjectItemBean? {
    this ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_SVG
    bean.data = this
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()
    return bean
}

/**添加一个SVG渲染*/
fun CanvasDelegate.addSvgRender(svg: String?) =
    GraphicsHelper.addRenderItemDataBean(this, svg.toSvgItemData())

/**GCode数据转[CanvasProjectItemBean]*/
fun String?.toGCodeItemData(): CanvasProjectItemBean? {
    this ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_GCODE
    bean.data = this
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()
    return bean
}

/**添加一个GCode渲染*/
fun CanvasDelegate.addGCodeRender(gcode: String?) =
    GraphicsHelper.addRenderItemDataBean(this, gcode.toGCodeItemData())

//endregion ---矢量---

//region ---算法---

/**翻转图片*/
fun Bitmap.flipEngraveBitmap(bean: CanvasProjectItemBean) = flip(bean._flipScaleX, bean._flipScaleY)

/**翻转Path*/
fun Path.flipEngravePath(bean: CanvasProjectItemBean) = flip(bean._flipScaleX, bean._flipScaleY)

fun List<Path>.flipEngravePath(bean: CanvasProjectItemBean) =
    flip(bean._flipScaleX, bean._flipScaleY)

/**数据索引
 * [com.angcyo.canvas.data.CanvasProjectItemBean.index]*/
val IRenderer.dataItemIndex: Int?
    get() = if (this is DataItemRenderer) {
        this.rendererItem?.dataBean?.index
    } else {
        null
    }

//endregion ---算法---

//region ---图/文---

fun Bitmap?.toBitmapItemData(action: CanvasProjectItemBean.() -> Unit = {}): CanvasProjectItemBean? {
    this ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_BITMAP
    bean.imageOriginal = toBase64Data()
    bean.action()
    return bean
}

/**添加一个[bitmap]数据渲染*/
fun CanvasDelegate.addBitmapRender(
    bitmap: Bitmap?,
    action: CanvasProjectItemBean.() -> Unit = {}
): DataItemRenderer? {
    bitmap ?: return null
    val bean = bitmap.toBitmapItemData(action) ?: return null
    bean.action()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

fun CharSequence?.toTextItemData(): CanvasProjectItemBean? {
    this ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_TEXT
    bean.text = "$this"
    bean.paintStyle = Paint.Style.FILL.toPaintStyleInt()
    return bean
}

/**添加一个[text]数据渲染
 *
 * [CanvasConstant.DATA_TYPE_TEXT]
 * [CanvasConstant.DATA_TYPE_BARCODE]
 * [CanvasConstant.DATA_TYPE_QRCODE]
 * */
fun CanvasDelegate.addTextRender(
    text: CharSequence?,
    type: Int = CanvasConstant.DATA_TYPE_TEXT
): DataItemRenderer? {
    text ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = type
    bean.text = "$text"
    bean.paintStyle = Paint.Style.FILL.toPaintStyleInt()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

//endregion ---图/文---

//region ---其他---

/**添加 功率 深度, 雕刻参数对照表. 耗时操作, 建议在子线程中执行
 * [bounds] 限制显示的区域
 * [gridCount] 格子横纵数量
 * [powerDepth] 阈值, 当 功率*深度 <= this 时才需要添加到参数表
 * */
@WorkerThread
fun CanvasDelegate.addParameterComparisonTable(
    @Pixel bounds: RectF,
    gridCount: Int,
    powerDepth: Int
) {
    val padding = 5f.toPixel()

    val gridMargin = (2f / (gridCount / 10f)).toPixel()
    val textMargin = 1f.toPixel()

    val numberTextItem = DataTextItem(CanvasProjectItemBean().apply {
        text = "100"
        fontSize = 3f / (gridCount / 10f)
        charSpacing = 0.2f

        //参数, 使用最后一次的默认
        //com.angcyo.engrave.EngraveFlowDataHelper.generateEngraveConfig
    })

    //功率/深度文本的宽高
    val powerTextItem = DataTextItem(CanvasProjectItemBean().apply {
        mtype = CanvasConstant.DATA_TYPE_TEXT
        fontSize = 5f
        text = "Power(%)"

        printPrecision = numberTextItem.dataBean.printPrecision
        printCount = numberTextItem.dataBean.printCount
    })
    val depthTextItem = DataTextItem(CanvasProjectItemBean().apply {
        mtype = CanvasConstant.DATA_TYPE_TEXT
        fontSize = powerTextItem.dataBean.fontSize
        text = "Depth(%)"
        angle = -90f

        printPrecision = numberTextItem.dataBean.printPrecision
        printCount = numberTextItem.dataBean.printCount
    })

    //CanvasProjectItemBean
    fun createItemBean(): CanvasProjectItemBean = CanvasProjectItemBean().apply {
        mtype = CanvasConstant.DATA_TYPE_TEXT
        fontSize = numberTextItem.dataBean.fontSize
        charSpacing = numberTextItem.dataBean.charSpacing

        //参数
        printPrecision = numberTextItem.dataBean.printPrecision
        printCount = numberTextItem.dataBean.printCount
        printPower = numberTextItem.dataBean.printPower
        printDepth = numberTextItem.dataBean.printDepth
    }

    val powerTextHeight = powerTextItem.getTextHeight()
    val depthTextWidth = depthTextItem.getTextWidth()
    val depthTextHeight = depthTextItem.getTextHeight()
    val leftTextWidth = depthTextHeight + numberTextItem.getTextHeight() + textMargin + textMargin
    val topTextHeight =
        powerTextHeight + numberTextItem.getTextHeight() + textMargin + textMargin

    //格子开始的地方
    val gridLeft = bounds.left + padding + leftTextWidth
    val gridTop = bounds.top + padding + topTextHeight

    //格子总共占用的宽高
    val gridWidthSum = bounds.width() - leftTextWidth - padding * 2
    val gridHeightSum = bounds.height() - topTextHeight - padding * 2

    //每个格子的宽高, 不包含margin
    val gridWidth = gridWidthSum / gridCount
    val gridHeight = gridHeightSum / gridCount

    //最终结果
    val beanList = mutableListOf<CanvasProjectItemBean>()

    //---数值
    val numberBeanList = mutableListOf<CanvasProjectItemBean>()

    //横竖线
    @Pixel
    val powerList = mutableListOf<Float>() //功率分割线, 竖线
    val depthList = mutableListOf<Float>() //深度分割线, 横线

    val max = 100
    val step = max / gridCount

    //--格子
    @Pixel
    var x = gridLeft
    var y = gridTop
    for (power in 0 until gridCount) {
        //功率
        x = gridLeft + power * gridWidth

        if (power > 0) {
            powerList.add(x)
        }
        val powerNumberItem = DataTextItem(createItemBean().apply {
            text = "${(power + 1) * step}"
        })
        powerNumberItem.dataBean.left =
            (x + gridWidth / 2 - powerNumberItem.getTextWidth() / 2).toMm()
        powerNumberItem.dataBean.top = (bounds.top + padding + powerTextHeight + textMargin).toMm()
        numberBeanList.add(powerNumberItem.dataBean)

        for (depth in 0 until gridCount) {
            //深度
            y = gridTop + depth * gridHeight

            if (power == 0) {
                if (depth > 0) {
                    depthList.add(y)
                }
                val depthNumberItem = DataTextItem(createItemBean().apply {
                    text = "${(depth + 1) * step}"
                })
                depthNumberItem.dataBean.left =
                    (bounds.left + padding + depthTextHeight + textMargin + depthNumberItem.getTextHeight() / 2 - depthNumberItem.getTextWidth() / 2).toMm()
                depthNumberItem.dataBean.top =
                    (y + gridHeight / 2 - depthNumberItem.getTextHeight() / 2).toMm()
                numberBeanList.add(depthNumberItem.dataBean)
            }

            val powerValue = (power + 1) * step
            val depthValue = (depth + 1) * step
            if (powerValue * depthValue <= powerDepth) {
                beanList.add(CanvasProjectItemBean().apply {
                    mtype = CanvasConstant.DATA_TYPE_RECT
                    paintStyle = Paint.Style.FILL.toPaintStyleInt()
                    width = (gridWidth - gridMargin * 2).toMm()
                    height = (gridHeight - gridMargin * 2).toMm()
                    left = (x + gridMargin).toMm()
                    top = (y + gridMargin).toMm()

                    //参数
                    printPrecision = numberTextItem.dataBean.printPrecision
                    printCount = numberTextItem.dataBean.printCount
                    printPower = powerValue
                    printDepth = depthValue
                })
            }
        }
    }

    //---横竖线
    val gCodeHandler = GCodeWriteHandler()
    gCodeHandler.unit = CanvasProjectItemBean.MM_UNIT
    gCodeHandler.isAutoCnc = false
    val gCodeWriter = StringWriter()
    gCodeHandler.writer = gCodeWriter
    gCodeHandler.onPathStart()

    //功率分割线, 竖线
    powerList.forEach {
        gCodeHandler.closeCnc()
        gCodeWriter.appendLine("G0X${it.toMm()}Y${gridTop.toMm()}")
        gCodeHandler.openCnc()
        gCodeWriter.appendLine("G1Y${(gridTop + gridHeightSum).toMm()}")
    }
    //深度分割线, 横线
    depthList.forEach {
        gCodeHandler.closeCnc()
        gCodeWriter.appendLine("G0X${gridLeft.toMm()}Y${it.toMm()}")
        gCodeHandler.openCnc()
        gCodeWriter.appendLine("G1X${(gridLeft + gridWidthSum).toMm()}")
    }

    gCodeHandler.onPathEnd()
    gCodeWriter.flush()
    gCodeWriter.close()

    val gcode = gCodeWriter.toString()
    beanList.add(createItemBean().apply {
        mtype = CanvasConstant.DATA_TYPE_GCODE
        paintStyle = Paint.Style.STROKE.toPaintStyleInt()
        data = gcode
        left = gridLeft.toMm()
        top = gridTop.toMm()
    })

    //---文本
    powerTextItem.dataBean.left =
        (gridLeft + gridWidthSum / 2 - powerTextItem.getTextWidth() / 2).toMm()
    powerTextItem.dataBean.top = (bounds.top + padding).toMm()
    beanList.add(powerTextItem.dataBean)

    depthTextItem.dataBean.left =
        (bounds.left + padding + depthTextHeight / 2 - depthTextWidth / 2).toMm()
    depthTextItem.dataBean.top =
        (gridTop + gridHeightSum / 2 - depthTextHeight / 2).toMm()
    beanList.add(depthTextItem.dataBean)

    //---数值
    beanList.addAll(numberBeanList)

    GraphicsHelper.renderItemDataBeanList(this, beanList, true, Strategy.normal)
}

/**乘法口诀表*/
@WorkerThread
fun CanvasDelegate.addMultiplicationTable(@Pixel bounds: RectF) {
    @Pixel
    val padding = 5f.toPixel()
    val horizontalGap = 3f.toPixel()
    val verticalGap = 2f.toPixel()

    val count = 9
    val textWidthSum = bounds.width() - padding * 2 - (count - 1) * horizontalGap
    val textHeightSum = bounds.height() - padding * 2 - (count - 1) * verticalGap

    /* @Pixel
     val textWidth = textWidthSum / count
     val textHeight = textHeightSum / count*/

    val textItem = DataTextItem(CanvasProjectItemBean().apply {
        mtype = CanvasConstant.DATA_TYPE_TEXT
        text = "9 × 9=81"
        fontSize = 4f
        charSpacing = 0.2f

        /*width = textWidth.toMm()
        height = textHeight.toMm()*/
    })

    @Pixel
    val textWidth = textItem.getTextWidth()
    val textHeight = textItem.getTextHeight()

    val textLeft = bounds.left + padding
    val textTop = bounds.centerY() - (textHeight * count + verticalGap * (count - 1)) / 2

    //CanvasProjectItemBean
    fun createItemBean(): CanvasProjectItemBean = CanvasProjectItemBean().apply {
        mtype = CanvasConstant.DATA_TYPE_TEXT
        fontSize = textItem.dataBean.fontSize
        charSpacing = textItem.dataBean.charSpacing
        width = textItem.dataBean.width
        height = textItem.dataBean.height
    }

    val beanList = mutableListOf<CanvasProjectItemBean>()
    for (x in 1..count) { //1~9 列
        val left = textLeft + (x - 1) * (textWidth + horizontalGap)
        for (y in x..count) { //x~9 行
            val top = textTop + (y - 1) * (textHeight + verticalGap)
            beanList.add(createItemBean().apply {
                text = "$x × $y=${x * y}"
                this.left = left.toMm()
                this.top = top.toMm()
            })
        }
    }

    GraphicsHelper.renderItemDataBeanList(this, beanList, true, Strategy.normal)
}

/**视力表*/
@WorkerThread
fun CanvasDelegate.addVisualChart(@Pixel bounds: RectF) {
    @Pixel
    val padding = 5f.toPixel()
    val horizontalGap = 3f.toPixel()
    val verticalGap = 4f.toPixel()

    var textLeft = bounds.left + padding
    var textTop = bounds.top + padding

    //每一行, 有多少个E, 从上到下
    val lineSizeList = listOf(1, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 8, 8, 8)

    @MM
    var maxFontSize = 20f //最上面一行的字体大小
    val scaleFactor = 0.8f //每一行字体大小是上一行的多少倍
    val lineCount = lineSizeList.size() //总共的行数
    val minFontSize = maxFontSize * scaleFactor.pow(lineCount - 1)

    //随机旋转的角度
    val angleList = mutableListOf<Float>()
    fun randomAngle(): Float {
        if (angleList.size() >= 4) {
            angleList.clear()
        }
        while (true) {
            val angle = when (nextInt(0, 4)) { //[0~4)
                1 -> 90f
                2 -> 180f
                3 -> 270f
                else -> 0f
            }
            if (!angleList.contains(angle)) {
                angleList.add(angle)
                break
            }
        }
        return angleList.last()
    }

    val textItem = DataTextItem(CanvasProjectItemBean().apply {
        mtype = CanvasConstant.DATA_TYPE_TEXT
        text = "E"
        fontSize = minFontSize

        /*width = textWidth.toMm()
        height = textHeight.toMm()*/
    })

    //计算最后一行需要占用的宽度
    val lastLineSize = lineSizeList.last()

    @Pixel
    val textAllWidth = lastLineSize * textItem.getTextWidth() + (lastLineSize - 1) * horizontalGap
    textLeft = bounds.centerX() - textAllWidth / 2

    //CanvasProjectItemBean
    fun createItemBean(): CanvasProjectItemBean = CanvasProjectItemBean().apply {
        mtype = CanvasConstant.DATA_TYPE_TEXT
        fontSize = textItem.dataBean.fontSize
        text = textItem.dataBean.text
        angle = randomAngle()
        width = textItem.dataBean.width
        height = textItem.dataBean.height
    }

    val beanList = mutableListOf<CanvasProjectItemBean>()
    for (line in 0 until lineCount) {
        val lineSize = lineSizeList[line]
        val lineTop = textTop
        var lineHeight = 0f
        for (size in 0 until lineSize) {
            beanList.add(createItemBean().apply {
                fontSize = maxFontSize

                val eWidth = textAllWidth / lineSize
                val centerX = textLeft + eWidth * size + eWidth / 2
                val item = DataTextItem(this)
                val itemWidth = item.getTextWidth()
                lineHeight = item.getTextHeight()

                left = (centerX - itemWidth / 2).toMm()
                top = lineTop.toMm()

                height = lineHeight.toMm()
                width = itemWidth.toMm()
            })
        }
        textTop = lineTop + lineHeight + verticalGap
        maxFontSize *= scaleFactor
    }
    GraphicsHelper.renderItemDataBeanList(this, beanList, true, Strategy.normal)
}

//endregion ---其他---