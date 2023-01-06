package com.angcyo.canvas.core.renderer

import android.graphics.*
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.CanvasEntryPoint
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.RenderParams
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.library.ex.*
import kotlin.math.max
import kotlin.math.min

/**
 * 支持同时选择多个[BaseItemRenderer]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/09
 */
class SelectGroupRenderer(canvasView: CanvasDelegate) : GroupRenderer(canvasView) {

    /**选择框的颜色*/
    var paintColor: Int = _color(R.color.canvas_select, canvasDelegate.view.context)

    val selectRect = emptyRectF()
    val selectRectMap = emptyRectF()
    val _startPoint = PointF()
    var _isStart = false

    init {
        paint.strokeWidth = 1 * dp
        paint.style = Paint.Style.STROKE
    }

    override fun onItemRendererRemove(itemRenderer: IItemRenderer<*>, strategy: Strategy) {
        super.onItemRendererRemove(itemRenderer, strategy)
        if (subItemList.contains(itemRenderer)) {
            canvasDelegate.selectedItem(null)
        }
    }

    override fun changeBoundsAction(reason: Reason, block: RectF.() -> Unit): Boolean {
        return super.changeBoundsAction(reason, block)
    }

    override fun onChangeBoundsAfter(reason: Reason) {
        super.onChangeBoundsAfter(reason)
    }

    override fun onRenderItemVisibleChanged(itemRenderer: IRenderer, visible: Boolean) {
        if (!visible) {
            if (subItemList.contains(itemRenderer)) {
                removeSelectedRenderer(itemRenderer)
            }
        }
    }

    override fun onSelectedItem(
        itemRenderer: IItemRenderer<*>?,
        oldItemRenderer: IItemRenderer<*>?
    ) {
        super.onSelectedItem(itemRenderer, oldItemRenderer)
        if (itemRenderer == this) {
            //选中的是自己
        } else if (itemRenderer != null) {
            reset()
        }
    }

    override fun onClearSelectItem(itemRenderer: IItemRenderer<*>) {
        super.onClearSelectItem(itemRenderer)
        if (itemRenderer == this) {
            reset()
        }
    }

    /**重置*/
    fun reset() {
        subItemList.clear()
        selectRect.setEmpty()
        //重置滚动
        rotate = 0f
        rendererItem = null
    }

    override fun render(canvas: Canvas, renderParams: RenderParams) {
        if (_isStart) {
            paint.style = Paint.Style.FILL
            paint.color = paintColor.alpha(32)
            canvas.drawRect(selectRect, paint)

            paint.style = Paint.Style.STROKE
            paint.color = paintColor
            canvas.drawRect(selectRect, paint)
        }
    }

    override fun containsPoint(point: PointF): Boolean {
        if (subItemList.size() > 1) {
            return super.containsPoint(point)
        }
        return false
    }

    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldMatrix: Matrix,
        isEnd: Boolean
    ) {
        //super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldValue)
        updateGroupBounds()
    }

    override fun onControlFinish(controlPoint: ControlPoint) {
        super.onControlFinish(controlPoint)
        /*if (controlPoint is RotateControlPoint) {
            updateSelectBounds()
        }*/
    }

    //---

    /**开始多选*/
    @CanvasEntryPoint
    fun startSelect(x: Float, y: Float) {
        _startPoint.set(x, y)
        _isStart = true
    }

    /**移动多选框*/
    fun moveSelect(x: Float, y: Float) {
        if (_isStart) {
            selectRect.set(
                min(_startPoint.x, x),
                min(_startPoint.y, y),
                max(_startPoint.x, x),
                max(_startPoint.y, y),
            )

            canvasViewBox.mapCoordinateSystemRect(selectRect, selectRectMap)
            subItemList.clear()
            canvasDelegate.itemsRendererList.forEach {
                if (it.isVisible() && it.intersectRect(selectRectMap)) {
                    subItemList.add(it)
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
            if (subItemList.isNotEmpty()) {
                //不为空, 则表示选中了元素
                if (subItemList.size() == 1) {
                    //只选中了一个
                    canvasDelegate.selectedItem(subItemList.first())
                } else {
                    //否则选中自己
                    updateGroupBounds()
                    canvasDelegate.selectedItem(this)
                }
            }
        }
    }

    /**重新选择元素列表*/
    fun selectedRendererList(list: List<BaseItemRenderer<*>>, strategy: Strategy) {
        if (list.isEmpty()) {
            return
        }
        if (list.size() == 1) {
            //只有一个
            canvasDelegate.selectedItem(list.first())
            return
        }
        val oldList = subItemList.toList()
        subItemList.clear()
        subItemList.addAll(list)
        updateGroupBounds()
        if (oldList.isEmpty()) {
            canvasDelegate.selectedItem(this)
        }

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasDelegate.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    selectedRendererList(oldList, Strategy.undo)
                }

                override fun runRedo() {
                    selectedRendererList(list, Strategy.redo)
                }
            })
        }
    }

    /**主动添加一个渲染器*/
    fun addSelectedRenderer(itemRenderer: BaseItemRenderer<*>) {
        if (itemRenderer == this) {
            return
        }
        val selectedRenderer = canvasDelegate.getSelectedRenderer()
        if (selectedRenderer == null) {
            //还未选中过
            canvasDelegate.selectedItem(itemRenderer)
        } else if (selectedRenderer == this) {
            subItemList.add(itemRenderer)
            updateGroupBounds()
        } else if (selectedRenderer != itemRenderer) {
            subItemList.clear()
            subItemList.add(selectedRenderer)
            subItemList.add(itemRenderer)
            updateGroupBounds()
            canvasDelegate.selectedItem(this)
        }
    }

    /**清空选中的列表*/
    fun clearSelectedList(strategy: Strategy) {
        val oldList = subItemList.toList()
        subItemList.clear()
        canvasDelegate.selectedItem(null)

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasDelegate.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    selectedRendererList(oldList, Strategy.undo)
                }

                override fun runRedo() {
                    clearSelectedList(Strategy.redo)
                }
            })
        }
    }

    /**主动移除一个渲染器*/
    fun removeSelectedRenderer(itemRenderer: IRenderer) {
        if (itemRenderer == this) {
            reset()
            canvasDelegate.selectedItem(null)
            return
        }
        subItemList.remove(itemRenderer)
        if (subItemList.size() == 1) {
            //只剩下一个
            canvasDelegate.selectedItem(subItemList.first())
        } else if (subItemList.isEmpty()) {
            //全部没了
            canvasDelegate.selectedItem(null)
        } else {
            updateGroupBounds()
        }
    }

    //---
}