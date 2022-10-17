package com.angcyo.canvas.items.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.graphics.scale
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Reason
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.component.SmartAssistant
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.*

/**
 * 数据渲染器
 *
 * [com.angcyo.canvas.data.ItemDataBean]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
class DataItemRenderer(canvasView: ICanvasView) : BaseItemRenderer<DataItem>(canvasView) {

    //<editor-fold desc="临时变量">

    val _flipMatrix = Matrix()
    val _flipRect = emptyRectF()

    //</editor-fold desc="临时变量">

    init {
        _name = "Data"
    }

    //<editor-fold desc="核心回调">

    override fun render(canvas: Canvas) {
        rendererItem?.drawable?.let { drawable ->
            val bounds = getRenderBounds()
            //需要处理矩形翻转的情况
            if (drawable is ScalePictureDrawable) {
                drawable.setBounds(
                    bounds.left.toInt(),
                    bounds.top.toInt(),
                    bounds.right.toInt(),
                    bounds.bottom.toInt()
                )
                drawable.draw(canvas)
            } else {
                //用于支持水平/垂直镜像绘制
                bounds.adjustFlipRect(_flipRect)
                var sx = 1f
                var sy = 1f
                if (getBounds().isFlipHorizontal) {
                    sx = -1f
                }
                if (getBounds().isFlipVertical) {
                    sy = -1f
                }
                _flipMatrix.reset()
                _flipMatrix.postScale(sx, sy, _flipRect.centerX(), _flipRect.centerY())
                canvas.withMatrix(_flipMatrix) {
                    drawable.setBounds(
                        _flipRect.left.toInt(),
                        _flipRect.top.toInt(),
                        _flipRect.right.toInt(),
                        _flipRect.bottom.toInt()
                    )
                    drawable.draw(canvas)
                }
            }
        }
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        if (type == ControlPoint.POINT_TYPE_LOCK) {
            if (isLineShape()) {
                //线段不支持任意比例缩放
                return false
            }
        }
        return super.isSupportControlPoint(type)
    }

    override fun isSupportSmartAssistant(type: Int): Boolean {
        if (isLineShape()) {
            //线段不支持调整高度
            return type != SmartAssistant.SMART_TYPE_H
        }
        return super.isSupportSmartAssistant(type)
    }

    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        super.itemBoundsChanged(reason, oldBounds)

        val renderItem = getRendererRenderItem()
        renderItem?.dataBean?.apply {
            updateByBounds(getBounds())

            //
            if (canvasView is CanvasDelegate && !canvasView.isTouchHold) {
                val oldWidth = oldBounds.width()
                val oldHeight = oldBounds.height()
                if (oldWidth != 0f && oldHeight != 0f) {
                    //
                    if (renderItem.needUpdateOfBoundsChanged(reason)) {
                        renderItem.updateRenderItem(this@DataItemRenderer)
                    }
                }
            }
        }
    }

    override fun itemRotateChanged(oldRotate: Float, rotateFlag: Int) {
        super.itemRotateChanged(oldRotate, rotateFlag)
        getRendererRenderItem()?.dataBean?.apply {
            angle = rotate
        }
    }

    override fun getEngraveBitmap(): Bitmap? {
        val item = getRendererRenderItem()
        val result = if (item is DataBitmapItem) {
            item.modifyBitmap ?: item.originBitmap
        } else {
            null
        }
        if (result != null) {
            //这里需要处理缩放和旋转
            val bounds = getBounds()
            val width = bounds.width().toInt()
            val height = bounds.height().toInt()
            val scaleBitmap = result.scale(width, height)
            return scaleBitmap.rotate(rotate)
        }
        return super.getEngraveBitmap()
    }

    //</editor-fold desc="核心回调">

    //<editor-fold desc="操作方法">

    val dataItem: DataItem?
        get() = getRendererRenderItem()

    val dataTextItem: DataTextItem?
        get() = getRendererRenderItem() as? DataTextItem

    val dataBitmapItem: DataBitmapItem?
        get() = getRendererRenderItem() as? DataBitmapItem

    val dataPathItem: DataPathItem?
        get() = getRendererRenderItem() as? DataPathItem

    val dataShapeItem: DataShapeItem?
        get() = getRendererRenderItem() as? DataShapeItem

    //</editor-fold desc="操作方法">

}