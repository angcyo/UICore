package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import android.graphics.Color
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.items.data.DataBitmapItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.parseGCode
import com.angcyo.gcode.GCodeHelper
import com.angcyo.library.L
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.app
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.*
import com.angcyo.library.unit.toMm
import com.angcyo.opencv.OpenCV
import com.hingin.rn.image.ImageProcess
import kotlin.io.readText

/**
 * 图片数据解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
class BitmapGraphicsParser : IGraphicsParser {

    companion object {

        /**处理图片抖动*/
        fun handleDithering(bitmap: Bitmap, bean: CanvasProjectItemBean) = handleDithering(
            bitmap,
            bean.inverse,
            bean.contrast.toDouble(),
            bean.brightness.toDouble(),
        )

        /**处理图片抖动*/
        fun handleDithering(
            bitmap: Bitmap,
            invert: Boolean = false,
            contrast: Double = 0.0,
            brightness: Double = 0.0
        ): Bitmap? {
            val bgColor = if (invert) Color.BLACK else Color.WHITE
            val grayBitmap = bitmap.toGrayHandle(bgColor, LibHawkKeys.bgAlphaThreshold)//灰度
            //对于低尺寸的图片需要先放大到 1000
            //val grayBitmapScale = grayBitmap.scaleToMinSize(1000, 1000)
            val grayBitmapScale = grayBitmap
            return OpenCV.bitmapToDithering(
                app(),
                grayBitmapScale,
                invert,
                contrast,
                brightness,
            )
        }
    }

    override fun parse(bean: CanvasProjectItemBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_BITMAP && !bean.imageOriginal.isNullOrEmpty()) {
            try {
                val originBitmap = bean.imageOriginal?.toBitmapOfBase64() ?: return null
                return if (bean.imageFilter == CanvasConstant.DATA_MODE_GCODE) {
                    //图片转成了GCode
                    _parseBitmapGCode(bean, originBitmap)
                } else {
                    //图片其他算法
                    bean.initBeanWidthHeight(originBitmap.width, originBitmap.height)
                    _parseBitmap(bean, originBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.parse(bean, canvasView)
    }

    /**初始化数据的宽高属性, 如果之前没有宽高数据*/
    fun CanvasProjectItemBean.initBeanWidthHeight(@Pixel width: Int, @Pixel height: Int) {
        if (_width <= 0) {
            this.width = width.toMm()
        }
        if (_height <= 0) {
            this.height = height.toMm()
        }
    }

    /**图片转GCode处理*/
    fun _parseBitmapGCode(bean: CanvasProjectItemBean, originBitmap: Bitmap?): DataItem? {
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
            gcodeDrawable.gCodePath = gcodeDrawable.gCodePath.flipEngravePath(bean)

            val item = DataBitmapItem(bean)
            item.originBitmap = originBitmap
            item.gCodeDrawable = gcodeDrawable
            item.modifyBitmap = null

            val bound = gcodeDrawable.gCodeBound
            val width = bound.width().ceil().toInt()
            val height = bound.height().ceil().toInt()

            //init
            bean.initBeanWidthHeight(width, height)

            item.renderDrawable = wrapFlipScalePictureDrawable(
                bean._flipX,
                bean._flipY,
                width, height
            ) {
                gcodeDrawable.setBounds(0, 0, width, height)
                gcodeDrawable.draw(this)
            }

            bean._dataMode = CanvasConstant.DATA_MODE_GCODE

            return item
        }
    }

    /**图片其他算法那*/
    fun _parseBitmap(bean: CanvasProjectItemBean, originBitmap: Bitmap?): DataItem? {
        //其他算法处理后的图片
        /*if (bean.imageFilter == CanvasConstant.DATA_MODE_DITHERING) {
            //抖动数据需要实时更新
            if (originBitmap != null) {
                //com.angcyo.canvas.laser.pecker.CanvasBitmapHandler.handleDithering
                val scaleBitmap = originBitmap.scale(
                    bean._widthScalePixel.floor().toInt(),
                    bean._heightScalePixel.floor().toInt()
                )//抖动数据, 实时缩放图片计算
                bean.src = handleDithering(scaleBitmap, bean)?.toBase64Data()
            }
        } else*/ if (bean.src.isNullOrBlank() && originBitmap != null) {
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
                CanvasConstant.DATA_MODE_GREY, CanvasConstant.DATA_MODE_DITHERING -> originBitmap.toGrayHandle(
                    bean.inverse,
                    bean.contrast,
                    bean.brightness,
                    alphaThreshold = 1 //透明颜色识别阈值
                )
                CanvasConstant.DATA_MODE_PRINT -> OpenCV.bitmapToPrint(
                    app(),
                    originBitmap,
                    bean.printsThreshold.toInt()
                )
                else -> null
            }?.toBase64Data()
        }

        val item = DataBitmapItem(bean)
        item.originBitmap = originBitmap
        item.modifyBitmap = bean.src?.toBitmapOfBase64()?.flipEngraveBitmap(bean)
        item.gCodeDrawable = null

        //扭曲后, 其他算法操作应该在扭曲上的图片操作
        if (bean.isMesh) {
            item.meshBitmap = ImageProcess.imageMesh(
                originBitmap,
                bean.minDiameter,
                bean.maxDiameter,
                bean.meshShape ?: "CONE"
            )?.flipEngraveBitmap(bean)
        }

        val bitmap =
            item.modifyBitmap ?: item.meshBitmap ?: item.originBitmap ?: return null
        wrapBitmapDrawable(item, bitmap)

        bean._dataMode = when (bean.imageFilter) {
            CanvasConstant.DATA_MODE_DITHERING -> CanvasConstant.DATA_MODE_DITHERING
            CanvasConstant.DATA_MODE_GREY -> CanvasConstant.DATA_MODE_GREY //灰度
            else -> CanvasConstant.DATA_MODE_BLACK_WHITE
        }
        return item
    }
}