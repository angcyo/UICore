package com.angcyo.canvas.core.renderer

import android.graphics.*
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.core.CanvasEntryPoint
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.component.ControlPoint
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

    init {
        canvasDelegate.addCanvasListener(this)
    }

    override fun onItemRendererRemove(itemRenderer: IItemRenderer<*>) {
        super.onItemRendererRemove(itemRenderer)
        if (selectItemList.contains(itemRenderer)) {
            canvasDelegate.selectedItem(null)
        }
    }

    override fun onItemBoundsChanged(itemRenderer: IItemRenderer<*>) {
        if (selectItemList.contains(itemRenderer)) {
            updateSelectBounds()
        }
    }

    override fun onCancelSelected(toSelectedItem: BaseItemRenderer<*>?) {
        super.onCancelSelected(toSelectedItem)
        reset()
    }

    fun reset() {
        selectItemList.clear()
        //重置滚动
        rotate = 0f
    }

    override fun render(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = paintColor.alpha(32)
        canvas.drawRect(selectRect, paint)

        paint.style = Paint.Style.STROKE
        paint.color = paintColor
        canvas.drawRect(selectRect, paint)
    }

    override fun isVisible(): Boolean {
        return _isStart
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        return when (type) {
            ControlPoint.POINT_TYPE_DELETE -> true
            else -> false
        }
        //return super.isSupportControlPoint(type)
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

    override fun itemBoundsChanged(oldBounds: RectF) {
        super.itemBoundsChanged(oldBounds)
        if (!oldBounds.isNoSize() && oldBounds.width() > 0 && oldBounds.height() > 0) {
            val bounds = getBounds()

            //平移
            val offsetLeft: Float = bounds.left - oldBounds.left
            val offsetTop: Float = bounds.top - oldBounds.top
            if (offsetLeft.isFinite() && offsetTop.isFinite() && (offsetLeft != 0f || offsetTop != 0f)) {
                selectItemList.forEach { item ->
                    item.changeBounds(false) {
                        offset(offsetLeft, offsetTop)
                    }
                }
            }

            //缩放
            val offsetWidth = bounds.width() - oldBounds.width()
            val offsetHeight = bounds.height() - oldBounds.height()
            if (offsetWidth.isFinite() && offsetHeight.isFinite() && (offsetWidth != 0f || offsetHeight != 0f)) {
                //todo 未实现
                selectItemList.forEach { item ->
                    val itemBounds = item.getBounds()
                    val newWidth = itemBounds.width() + offsetWidth
                    val newHeight = itemBounds.height() + offsetHeight
                    item.changeBounds(false) {
                        scale(
                            newWidth / itemBounds.width(),
                            newHeight / itemBounds.height(),
                            bounds.left,
                            bounds.top
                        )
                        //adjustSizeWithRotate(newWidth, newHeight, item.rotate, _adjustType)
                    }
                }
            }
        }
    }

    /**更新选中的bounds大小, 需要包含所有选中的元素*/
    fun updateSelectBounds() {
        changeBounds {
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
                if (it.intersectRect(selectRectMap)) {
                    selectItemList.add(it)
                    //L.i("相交:$it")
                }
            }
            refresh()
        }
    }

    /**取消选择*/
    fun cancelSelect() {
        _isStart = false
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

    /**主动添加一个渲染器*/
    fun addSelectedRenderer(itemRenderer: BaseItemRenderer<*>) {
        if (itemRenderer == this) {
            return
        }
        val selectedRenderer = canvasDelegate.getSelectedRenderer()
        if (selectedRenderer == this) {
            selectItemList.add(itemRenderer)
            updateSelectBounds()
        } else if (selectedRenderer != null && selectedRenderer != itemRenderer) {
            selectItemList.clear()
            selectItemList.add(selectedRenderer)
            selectItemList.add(itemRenderer)
            updateSelectBounds()
            canvasDelegate.selectedItem(this)
        }
    }
}