package com.angcyo.canvas.items.data

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.RenderParams
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.component.SmartAssistant
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.library.L
import com.angcyo.library.ex.getScale
import com.angcyo.library.ex.isNoSize

/**
 * 数据渲染器
 *
 * [com.angcyo.canvas.data.CanvasProjectItemBean]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
class DataItemRenderer(canvasView: ICanvasView) : BaseItemRenderer<DataItem>(canvasView),
    ICanvasListener {

    init {
        _name = "Data"
    }

    override fun onAddRenderer(strategy: Strategy) {
        super.onAddRenderer(strategy)
        canvasView.addCanvasListener(this)
    }

    override fun onRemoveRenderer(strategy: Strategy) {
        super.onRemoveRenderer(strategy)
        canvasView.removeCanvasListener(this)
    }

    //<editor-fold desc="核心回调">

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

    /**是否发生过缩放*/
    var _isScaleHappen = false
    override fun onCanvasBoxMatrixChanged(matrix: Matrix, oldValue: Matrix, isEnd: Boolean) {
        super.onCanvasBoxMatrixChanged(matrix, oldValue, isEnd)
        if (isEnd && oldValue.getScale() != matrix.getScale()) {
            if (canvasView is CanvasDelegate) {
                if (canvasView.isTouchHold) {
                    _isScaleHappen = true
                } else {
                    _updateRenderItemIfNeed(Reason.user)
                }
            }
        }
    }

    var _touchDownScale = 1f
    override fun onCanvasTouchEvent(canvasDelegate: CanvasDelegate, event: MotionEvent): Boolean {
        val actionMasked = event.actionMasked
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            _touchDownScale = canvasViewBox.getScaleX()
        } else if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
            if (_isScaleHappen || _touchDownScale != canvasViewBox.getScaleX()) {
                _updateRenderItemIfNeed(Reason.user)
            }
        }
        return super.onCanvasTouchEvent(canvasDelegate, event)
    }

    /**检查是否需要重新刷新可视化的数据*/
    fun _updateRenderItemIfNeed(reason: Reason = Reason(flag = Reason.REASON_FLAG_STYLE)) {
        //L.w("更新....")
        val renderItem = getRendererRenderItem()
        if (isVisible() && !getRotateBounds().isNoSize()) {
            renderItem?.dataBean?.apply {
                if (renderItem is DataPathItem) {
                    if (renderItem.itemPaint.style == Paint.Style.STROKE || renderItem.isLineShape()) {
                        //描边的path, 需要放大边框线, 所以需要重新渲染数据
                        renderItem.updateRenderItem(this@DataItemRenderer, reason)
                    }
                }
            }
        }
        _isScaleHappen = false
    }

    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldMatrix: Matrix,
        isEnd: Boolean
    ) {
        super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldMatrix, isEnd)
    }

    override fun renderItemBoundsChanged(reason: Reason, oldBounds: RectF) {
        super.renderItemBoundsChanged(reason, oldBounds)

        var isUpdateItem = false
        val renderItem = getRendererRenderItem()
        renderItem?.dataBean?.apply {
            updateByBounds(getBounds())

            //
            if (canvasView is CanvasDelegate) {
                val oldWidth = oldBounds.width()
                val oldHeight = oldBounds.height()
                if (oldWidth != 0f && oldHeight != 0f) {
                    //
                    if (renderItem.needUpdateOfBoundsChanged(reason)) {
                        if (canvasView.isTouchHold) {
                            _isScaleHappen = true
                        } else {
                            renderItem.updateRenderItem(this@DataItemRenderer, reason)
                            isUpdateItem = true
                        }
                    }
                }
            }
        }

        if (!isUpdateItem) {
            if (!oldBounds.isNoSize() && reason.flag > Reason.REASON_FLAG_STYLE) {
                //不管是平移, 旋转, 还是缩放, 发生改变之后, 都需要更新数据, 比如索引.
                renderItemDataChanged(reason)
            }
        }
    }

    override fun renderItemRotateChanged(oldRotate: Float, newRotate: Float, rotateFlag: Int) {
        super.renderItemRotateChanged(oldRotate, newRotate, rotateFlag)
        getRendererRenderItem()?.dataBean?.apply {
            angle = rotate
        }
    }

    override fun renderItemDataChanged(reason: Reason) {
        if (reason.flag > 0) {
            val dataBean = getRendererRenderItem()?.dataBean
            val index = dataBean?.index
            dataBean?.index = null
            if ((index ?: 0) > 0) {
                L.i("数据改变,清空索引:${index} $reason")
            }
        }
        super.renderItemDataChanged(reason)
    }

    override fun onRendererVisibleChanged(from: Boolean, to: Boolean, strategy: Strategy) {
        super.onRendererVisibleChanged(from, to, strategy)
        getRendererRenderItem()?.updateVisible(to, this, Strategy.preview)
    }

    //<editor-fold desc="IEngraveProvider">

    /**
     * [com.angcyo.canvas.items.data.DataItem.getEngraveBitmap]
     * [com.angcyo.canvas.items.data.DataItemRenderer.getEngraveBitmap]
     * */
    override fun getEngraveBitmap(renderParams: RenderParams): Bitmap? {
        val item = getRendererRenderItem()
        val bitmap = item?._getEngraveBitmap(renderParams)
        if (bitmap != null) {
            return bitmap
        }
        return super.getEngraveBitmap(renderParams)
    }

    override fun getEngraveDataItem(): DataItem? = dataItem

    //</editor-fold desc="IEngraveProvider">

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