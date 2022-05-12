package com.angcyo.canvas.core.renderer

import android.graphics.*
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.Reason
import com.angcyo.canvas.core.CanvasEntryPoint
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.*
import kotlin.math.max
import kotlin.math.min

/**
 * 支持同时选择多个[BaseItemRenderer]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/09
 */
class SelectGroupRenderer(canvasView: CanvasDelegate) :
    BaseItemRenderer<SelectGroupItem>(canvasView), ICanvasListener {

    /**保存选中渲染项*/
    val selectItemList = mutableSetOf<BaseItemRenderer<*>>()

    /**画笔*/
    val paint = createPaint().apply {
        strokeWidth = 1 * dp
    }

    val canvasDelegate: CanvasDelegate
        get() = canvasView as CanvasDelegate

    var paintColor: Int = _color(R.color.colorAccent)

    val selectRect = RectF()
    val selectRectMap = RectF()
    val _startPoint = PointF()
    var _isStart = false

    companion object {

        /**批量旋转[BaseItemRenderer]*/
        fun rotateItemList(
            list: Iterable<BaseItemRenderer<*>>,
            degrees: Float,
            pivotX: Float,
            pivotY: Float
        ) {
            val changeReason = Reason(Reason.REASON_CODE)
            list.forEach { item ->
                _tempRectF.set(item.getBounds())
                _tempRectF.rotate(degrees, pivotX, pivotY)
                item.rotate += degrees
                item.changeBounds(changeReason) {
                    offset(_tempRectF.centerX() - centerX(), _tempRectF.centerY() - centerY())
                }
            }
        }

        /**批量平移/缩放[BaseItemRenderer]*/
        fun changeBoundsItemList(
            list: Iterable<BaseItemRenderer<*>>,
            oldBounds: RectF,
            newBounds: RectF
        ) {
            if (!oldBounds.isNoSize() && oldBounds.width() != 0f && oldBounds.height() != 0f) {
                val changeReason = Reason(Reason.REASON_CODE)
                val bounds = newBounds

                //平移
                val offsetLeft: Float = bounds.left - oldBounds.left
                val offsetTop: Float = bounds.top - oldBounds.top
                if (offsetLeft.isFinite() && offsetTop.isFinite() && (offsetLeft != 0f || offsetTop != 0f)) {
                    list.forEach { item ->
                        item.changeBounds(changeReason) {
                            offset(offsetLeft, offsetTop)
                        }
                    }
                }

                //缩放
                val offsetWidth = bounds.width() - oldBounds.width()
                val offsetHeight = bounds.height() - oldBounds.height()
                if (offsetWidth.isFinite() && offsetHeight.isFinite() && (offsetWidth != 0f || offsetHeight != 0f)) {
                    list.forEach { item ->
                        item.changeBounds(changeReason) {
                            scale(
                                bounds.width() / oldBounds.width(),
                                bounds.height() / oldBounds.height(),
                                bounds.left,
                                bounds.top
                            )
                        }
                    }
                }

                //旋转
                /*list.forEach { item ->
                    _tempRectF.set(item.getBounds())
                    _tempRectF.rotate(rotate, bounds.centerX(), bounds.centerY())
                    item.rotate = rotate
                    item.changeBounds(changeReason) {
                        offset(_tempRectF.centerX() - centerX(), _tempRectF.centerY() - centerY())
                    }
                }*/
            }
        }
    }

    init {
        canvasDelegate.addCanvasListener(this)
    }

    override fun onItemRendererRemove(itemRenderer: IItemRenderer<*>) {
        super.onItemRendererRemove(itemRenderer)
        if (selectItemList.contains(itemRenderer)) {
            canvasDelegate.selectedItem(null)
        }
    }

    override fun onItemBoundsChanged(item: IRenderer, reason: Reason, oldBounds: RectF) {
        if (selectItemList.contains(item)) {
            if (reason.reason == Reason.REASON_USER) {
                updateSelectBounds()
            }
        }
    }

    override fun onItemVisibleChanged(itemRenderer: IRenderer, visible: Boolean) {
        if (!visible) {
            if (selectItemList.contains(itemRenderer)) {
                removeSelectedRenderer(itemRenderer)
            }
        }
    }

    override fun onCancelSelected(toSelectedItem: BaseItemRenderer<*>?) {
        super.onCancelSelected(toSelectedItem)
        reset()
    }

    fun reset() {
        selectItemList.clear()
        selectRect.setEmpty()
        //重置滚动
        rotate = 0f
    }

    override fun render(canvas: Canvas) {
        if (_isStart) {
            paint.style = Paint.Style.FILL
            paint.color = paintColor.alpha(32)
            canvas.drawRect(selectRect, paint)

            paint.style = Paint.Style.STROKE
            paint.color = paintColor
            canvas.drawRect(selectRect, paint)
        }
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        /*return when (type) {
            ControlPoint.POINT_TYPE_DELETE -> true
            ControlPoint.POINT_TYPE_SCALE -> true
            else -> false
        }*/
        //return super.isSupportControlPoint(type)
        return true
    }

    override fun containsPoint(point: PointF): Boolean {
        if (selectItemList.size() > 1) {
            return super.containsPoint(point)
        }
        return false
    }

    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldValue: Matrix
    ) {
        super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldValue)
        updateSelectBounds()
    }

    override fun itemRotateChanged(oldRotate: Float) {
        super.itemRotateChanged(oldRotate)
        val degrees = rotate - oldRotate
        rotateItemList(selectItemList, degrees, getBounds().centerX(), getBounds().centerY())
    }

    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        super.itemBoundsChanged(reason, oldBounds)

        if (reason.reason == Reason.REASON_USER) {
            changeBoundsItemList(selectItemList, oldBounds, getBounds())
        }
    }

    /**更新选中的bounds大小, 需要包含所有选中的元素*/
    fun updateSelectBounds() {
        changeBounds(Reason(Reason.REASON_CODE, true)) {
            var l = Float.MAX_VALUE
            var t = Float.MAX_VALUE
            var r = Float.MIN_VALUE
            var b = Float.MIN_VALUE
            selectItemList.forEach {
                val rotateBounds = it.getRotateBounds().adjustFlipRect(_tempRectF)
                l = min(l, rotateBounds.left)
                t = min(t, rotateBounds.top)

                r = max(r, rotateBounds.right)
                b = max(b, rotateBounds.bottom)
            }
            set(l, t, r, b)
        }
    }

    /**开始多选*/
    @CanvasEntryPoint
    fun startSelect(x: Float, y: Float) {
        _startPoint.set(x, y)
        _isStart = true
    }

    fun moveSelect(x: Float, y: Float) {
        if (_isStart) {
            selectRect.set(
                min(_startPoint.x, x),
                min(_startPoint.y, y),
                max(_startPoint.x, x),
                max(_startPoint.y, y),
            )

            canvasViewBox.mapCoordinateSystemRect(selectRect, selectRectMap)
            selectItemList.clear()
            canvasDelegate.itemsRendererList.forEach {
                if (it.isVisible() && it.intersectRect(selectRectMap)) {
                    selectItemList.add(it)
                    //L.i("相交:$it")
                }
            }
            refresh()
        }
    }

    /**结束选择, 并查找范围内的项目*/
    fun endSelect() {
        if (_isStart) {
            _isStart = false
            selectRect.setEmpty()
            if (selectItemList.isNotEmpty()) {
                //不为空, 则表示选中了元素
                if (selectItemList.size() == 1) {
                    //只选中了一个
                    canvasDelegate.selectedItem(selectItemList.first())
                } else {
                    //否则选中自己
                    updateSelectBounds()
                    canvasDelegate.selectedItem(this)
                }
            }
        }
    }

    /**主动添加一个渲染器*/
    fun addSelectedRenderer(itemRenderer: BaseItemRenderer<*>) {
        if (itemRenderer == this || !itemRenderer.isVisible()) {
            return
        }
        val selectedRenderer = canvasDelegate.getSelectedRenderer()
        if (selectedRenderer == null) {
            //还未选中过
            canvasDelegate.selectedItem(itemRenderer)
        } else if (selectedRenderer == this) {
            selectItemList.add(itemRenderer)
            updateSelectBounds()
        } else if (selectedRenderer != itemRenderer) {
            selectItemList.clear()
            selectItemList.add(selectedRenderer)
            selectItemList.add(itemRenderer)
            updateSelectBounds()
            canvasDelegate.selectedItem(this)
        }
    }

    /**主动移除一个渲染器*/
    fun removeSelectedRenderer(itemRenderer: IRenderer) {
        if (itemRenderer == this) {
            reset()
            canvasDelegate.selectedItem(null)
            return
        }
        selectItemList.remove(itemRenderer)
        if (selectItemList.size() == 1) {
            //只剩下一个
            canvasDelegate.selectedItem(selectItemList.first())
        } else if (selectItemList.isEmpty()) {
            //全部没了
            canvasDelegate.selectedItem(null)
        } else {
            updateSelectBounds()
        }
    }
}