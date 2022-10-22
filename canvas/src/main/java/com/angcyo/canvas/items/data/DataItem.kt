package com.angcyo.canvas.items.data

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.AnyThread
import androidx.core.graphics.scale
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.CanvasProjectItemBean.Companion.MM_UNIT
import com.angcyo.canvas.data.toPaintStyle
import com.angcyo.canvas.data.toPaintStyleInt
import com.angcyo.canvas.graphics.GraphicsHelper
import com.angcyo.canvas.graphics.IEngraveProvider
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.rotate
import com.angcyo.library.ex.toBitmap
import com.angcyo.library.ex.withPicture

/**
 * [com.angcyo.canvas.data.CanvasProjectItemBean]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
open class DataItem(val dataBean: CanvasProjectItemBean) : BaseItem(), IEngraveProvider {

    /**
     * 通过改变此对象, 呈现出不同的可视图画
     * 可绘制的对象, 此对象不带旋转和缩放*/
    var drawable: Drawable? = null

    init {
        itemLayerName = dataBean.name ?: when (dataBean.mtype) {
            CanvasConstant.DATA_TYPE_BITMAP -> "Bitmap"
            CanvasConstant.DATA_TYPE_TEXT -> "Text"
            CanvasConstant.DATA_TYPE_QRCODE -> "QRCode"
            CanvasConstant.DATA_TYPE_BARCODE -> "BarCode"
            CanvasConstant.DATA_TYPE_RECT -> "Rect"
            CanvasConstant.DATA_TYPE_OVAL -> "Oval"
            CanvasConstant.DATA_TYPE_LINE -> "Line"
            CanvasConstant.DATA_TYPE_PEN -> "Pen"
            CanvasConstant.DATA_TYPE_BRUSH -> "Brush"
            CanvasConstant.DATA_TYPE_SVG -> "Svg"
            CanvasConstant.DATA_TYPE_POLYGON -> "Polygon"
            CanvasConstant.DATA_TYPE_PENTAGRAM -> "Pentagram"
            CanvasConstant.DATA_TYPE_LOVE -> "Love"
            CanvasConstant.DATA_TYPE_SINGLE_WORD -> "SingleWord"
            CanvasConstant.DATA_TYPE_GCODE -> "GCode"
            CanvasConstant.DATA_TYPE_PATH -> "Path"
            else -> "Unknown"
        }
    }

    override fun getItemScaleX(renderer: BaseItemRenderer<*>): Float {
        val width = MM_UNIT.convertValueToPixel(dataBean.width)
        return renderer.getBounds().width() / width
    }

    override fun getItemScaleY(renderer: BaseItemRenderer<*>): Float {
        val height = MM_UNIT.convertValueToPixel(dataBean.height)
        return renderer.getBounds().height() / height
    }

    /**当渲染的bounds改变时, 是否需要刷新[updateRenderItem]*/
    open fun needUpdateOfBoundsChanged(reason: Reason): Boolean = false

    //---方法---

    /**重新更新需要渲染的界面数据*/
    @AnyThread
    fun updateRenderItem(renderer: DataItemRenderer) {
        //更新
        GraphicsHelper.updateRenderItem(renderer, dataBean)
    }

    //---操作---

    /**更新笔的样式*/
    fun updatePaintStyle(
        style: Paint.Style,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.paintStyle.toPaintStyle()
        val new = style
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.paintStyle = old.toPaintStyleInt()
            updateRenderItem(renderer)
        }) {
            dataBean.paintStyle = new.toPaintStyleInt()
            updateRenderItem(renderer)
        }
    }

    //<editor-fold desc="IEngraveProvider">

    override fun getEngraveRenderer(): IItemRenderer<*>? = null

    override fun getEngraveDataItem(): DataItem? = this

    /**
     * [com.angcyo.canvas.items.data.DataItem.getEngraveBitmap]
     * [com.angcyo.canvas.items.data.DataItemRenderer.getEngraveBitmap]
     * */
    override fun getEngraveBitmap(): Bitmap? {
        val item = this
        val bitmap = if (item is DataBitmapItem) {
            item.modifyBitmap ?: item.originBitmap
        } else {
            null
        }
        val rotate = _rotate
        if (bitmap != null) {
            //这里需要处理缩放和旋转
            val bounds = getEngraveBounds()
            val width = bounds.width().toInt()
            val height = bounds.height().toInt()
            val scaleBitmap = bitmap.scale(width, height)
            return scaleBitmap.rotate(rotate)
        }
        drawable?.let { drawable ->
            val renderBounds = getEngraveBounds()
            val renderWidth = renderBounds.width()
            val renderHeight = renderBounds.height()

            val rotateBounds = getEngraveRotateBounds()
            val width = rotateBounds.width()
            val height = rotateBounds.height()

            val result = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
                withRotation(rotate, width / 2, height / 2) {
                    withTranslation(
                        width / 2 - renderWidth / 2,
                        height / 2 - renderHeight / 2
                    ) {
                        drawable.setBounds(
                            renderBounds.left.toInt(),
                            renderBounds.top.toInt(),
                            renderBounds.right.toInt(),
                            renderBounds.bottom.toInt()
                        )
                        drawable.draw(this)
                    }
                }
            })
            return result.toBitmap()
        }
        return null
    }

    override fun getEngraveBounds(): RectF {
        val rect = RectF()
        dataBean.updateToRenderBounds(rect)
        return rect
    }

    override fun getEngraveRotateBounds(): RectF {
        val rect = RectF()
        getEngraveBounds().rotate(_rotate, result = rect)
        return rect
    }

    //</editor-fold desc="IEngraveProvider">
}