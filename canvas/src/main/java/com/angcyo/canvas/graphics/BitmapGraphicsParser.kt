package com.angcyo.canvas.graphics

import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.items.data.DataBitmapItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.parseGCode
import com.angcyo.gcode.GCodeHelper
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.ex.deleteSafe
import com.angcyo.library.ex.toBase64Data
import com.angcyo.library.ex.toBitmapOfBase64
import com.angcyo.library.ex.toBlackWhiteHandle
import com.angcyo.opencv.OpenCV
import com.hingin.rn.image.ImageProcess

/**
 * 图片数据解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
class BitmapGraphicsParser : IGraphicsParser {

    override fun parse(bean: CanvasProjectItemBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_BITMAP && !bean.imageOriginal.isNullOrEmpty()) {
            try {
                val originBitmap = bean.imageOriginal?.toBitmapOfBase64()
                originBitmap?.let {
                    if (bean._width <= 0) {
                        bean.width = it.width.toMm()
                    }
                    if (bean._height <= 0) {
                        bean.height = it.height.toMm()
                    }
                }
                if (bean.imageFilter == CanvasConstant.DATA_MODE_GCODE) {
                    //图片转成了GCode
                    var gcode = bean.data ?: bean.src
                    if (gcode.isNullOrBlank() && originBitmap != null) {
                        //自动应用算法
                        OpenCV.bitmapToGCode(
                            app(),
                            originBitmap,
                            (bean._width / 2).toDouble(),
                            lineSpace = bean.gcodeLineSpace.toDouble(),
                            direction = bean.gcodeDirection,
                            angle = bean.gcodeAngle.toDouble(),
                            type = if (bean.gcodeOutline) 1 else 3
                        ).let {
                            val gCodeText = it.readText()
                            it.deleteSafe()
                            gcode = gCodeText
                        }
                        bean.data = gcode
                    }

                    //
                    if (gcode.isNullOrEmpty()) {
                        //bean.src gcode数据
                        L.w("GCode数据为空, 无法渲染...")
                        return null
                    } else {
                        val gcodeDrawable = GCodeHelper.parseGCode(gcode) ?: return null
                        val item = DataBitmapItem(bean)
                        item.originBitmap = originBitmap
                        item.gCodeDrawable = gcodeDrawable
                        item.modifyBitmap = null

                        val bound = gcodeDrawable.gCodeBound
                        val width = bound.width().toInt()
                        val height = bound.height().toInt()
                        item.drawable = wrapScalePictureDrawable(width, height) {
                            gcodeDrawable.setBounds(0, 0, width, height)
                            gcodeDrawable.draw(this)
                        }

                        bean._dataMode = CanvasConstant.DATA_MODE_GCODE

                        return item
                    }
                } else {
                    //其他
                    if (bean.src.isNullOrBlank() && originBitmap != null) {
                        //只有原图, 没有算法处理后的图片, 则需要主动应用算法处理图片
                        bean.src = when (bean.imageFilter) {
                            CanvasConstant.DATA_MODE_BLACK_WHITE -> originBitmap.toBlackWhiteHandle(
                                bean.blackThreshold.toInt(),
                                bean.inverse
                            )
                            CanvasConstant.DATA_MODE_SEAL -> OpenCV.bitmapToSeal(
                                app(),
                                originBitmap,
                                bean.sealThreshold.toInt()
                            )
                            CanvasConstant.DATA_MODE_GREY -> OpenCV.bitmapToGrey(originBitmap)
                            CanvasConstant.DATA_MODE_PRINT -> OpenCV.bitmapToPrint(
                                app(),
                                originBitmap,
                                bean.printsThreshold.toInt()
                            )
                            CanvasConstant.DATA_MODE_DITHERING -> OpenCV.bitmapToDithering(
                                app(),
                                originBitmap,
                                bean.inverse,
                                bean.contrast.toDouble(),
                                bean.brightness.toDouble(),
                            )
                            else -> null
                        }?.toBase64Data()
                    }

                    val item = DataBitmapItem(bean)
                    item.originBitmap = originBitmap
                    item.modifyBitmap = bean.src?.toBitmapOfBase64()
                    item.gCodeDrawable = null

                    //扭曲后, 其他算法操作应该在扭曲上的图片操作
                    if (bean.isMesh) {
                        item.meshBitmap = ImageProcess.imageMesh(
                            originBitmap,
                            bean.minDiameter,
                            bean.maxDiameter,
                            bean.meshShape ?: "CONE"
                        )
                    }

                    val bitmap =
                        item.modifyBitmap ?: item.meshBitmap ?: item.originBitmap ?: return null
                    wrapBitmap(item, bitmap)

                    bean._dataMode = when (bean.imageFilter) {
                        CanvasConstant.DATA_MODE_DITHERING -> CanvasConstant.DATA_MODE_DITHERING
                        CanvasConstant.DATA_MODE_GREY -> CanvasConstant.DATA_MODE_GREY //灰度
                        else -> CanvasConstant.DATA_MODE_BLACK_WHITE
                    }
                    return item
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.parse(bean, canvasView)
    }

}