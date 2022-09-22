package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Reason
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.DataBitmapItem
import com.angcyo.canvas.items.DataItem
import com.angcyo.canvas.items.DataTextItem
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue

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

    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        super.itemBoundsChanged(reason, oldBounds)
        getRendererRenderItem()?.dataBean?.apply {
            updateByBounds(getBounds())
        }
        if (reason.flag.have(Reason.REASON_FLAG_BOUNDS)) {
            if (canvasView is CanvasDelegate && !canvasView.isTouchHold) {
                val bounds = getBounds()
                val width = bounds.width().absoluteValue
                val height = bounds.height().absoluteValue
                if (width > 0 && height > 0) {
                    //getRendererRenderItem()?.updateDrawable(paint, width, height)
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

    //</editor-fold desc="核心回调">

    //<editor-fold desc="操作方法">

    val dataItem: DataItem?
        get() = getRendererRenderItem()

    val dataTextItem: DataTextItem?
        get() = getRendererRenderItem() as? DataTextItem

    val dataBitmapItem: DataBitmapItem?
        get() = getRendererRenderItem() as? DataBitmapItem

    //</editor-fold desc="操作方法">

}