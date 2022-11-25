package com.angcyo.canvas.items.data

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.AnyThread
import androidx.core.graphics.scale
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.CanvasProjectItemBean.Companion.MM_UNIT
import com.angcyo.canvas.data.toPaintStyle
import com.angcyo.canvas.data.toPaintStyleInt
import com.angcyo.canvas.data.toTypeNameString
import com.angcyo.canvas.graphics.GraphicsHelper
import com.angcyo.canvas.graphics.IEngraveProvider
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.ex.bitmapCanvas
import com.angcyo.library.ex.have
import com.angcyo.library.ex.rotate

/**
 * [com.angcyo.canvas.data.CanvasProjectItemBean]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
open class DataItem(val dataBean: CanvasProjectItemBean) : BaseItem(), IEngraveProvider {

    companion object {

        /**默认笔的宽度 */
        const val DEFAULT_PAINT_WIDTH = 1f
    }

    //region ---属性---

    /**画笔*/
    val itemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = DEFAULT_PAINT_WIDTH
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    /**
     * 通过改变此对象, 呈现出不同的可视图画
     * 可绘制的对象, 此对象不带旋转和缩放
     * 此对象为[null], 则不应该渲染在界面上?
     * [com.angcyo.canvas.graphics.GraphicsHelper.renderItemDataBean]
     * */
    var drawable: Drawable? = null

    //endregion ---属性---

    init {
        itemLayerName = dataBean.name ?: dataBean.mtype.toTypeNameString()
    }

    override fun getItemScaleX(renderer: BaseItemRenderer<*>): Float {
        val width = MM_UNIT.convertValueToPixel(dataBean._width)
        return renderer.getBounds().width() / width
    }

    override fun getItemScaleY(renderer: BaseItemRenderer<*>): Float {
        val height = MM_UNIT.convertValueToPixel(dataBean._height)
        return renderer.getBounds().height() / height
    }

    /**当渲染的bounds改变时, 是否需要刷新[updateRenderItem]*/
    open fun needUpdateOfBoundsChanged(reason: Reason): Boolean {
        if (this is DataPathItem) {
            return reason.reason == Reason.REASON_USER && reason.flag.have(Reason.REASON_FLAG_BOUNDS)
        }
        if (this is DataBitmapItem) {
            if (dataBean.imageFilter == CanvasConstant.DATA_MODE_GCODE) {
                return reason.reason == Reason.REASON_USER && reason.flag.have(Reason.REASON_FLAG_BOUNDS)
            }
        }
        return false
    }

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

            /*val result = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
                withRotation(rotate, width / 2, height / 2) {
                    withTranslation( width / 2 - renderWidth / 2, height / 2 - renderHeight / 2 ) {
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
            */

            val result = bitmapCanvas(width.toInt(), height.toInt()) {
                rotate(rotate, width / 2, height / 2)
                translate(width / 2 - renderWidth / 2, height / 2 - renderHeight / 2)
                drawable.setBounds(
                    0,
                    0,
                    renderBounds.width().toInt(),
                    renderBounds.height().toInt()
                )
                drawable.draw(this)
            }
            return result
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