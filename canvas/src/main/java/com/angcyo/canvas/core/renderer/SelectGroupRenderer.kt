package com.angcyo.canvas.core.renderer

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.core.graphics.drawable.toDrawable
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.CanvasEntryPoint
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.OffsetItemData
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.data.RendererBounds
import com.angcyo.canvas.items.SelectGroupItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataItemRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.drawable.*
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
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

    /**选择框的颜色*/
    var paintColor: Int = _color(R.color.canvas_select, canvasDelegate.view.context)

    val canvasDelegate: CanvasDelegate
        get() = canvasView as CanvasDelegate

    val selectRect = emptyRectF()
    val selectRectMap = emptyRectF()
    val _startPoint = PointF()
    var _isStart = false

    init {
        paint.strokeWidth = 1 * dp
        paint.style = Paint.Style.STROKE

        canvasDelegate.addCanvasListener(this)
    }

    override fun onItemRendererRemove(itemRenderer: IItemRenderer<*>) {
        super.onItemRendererRemove(itemRenderer)
        if (selectItemList.contains(itemRenderer)) {
            canvasDelegate.selectedItem(null)
        }
    }

    override fun changeBoundsAction(reason: Reason, block: RectF.() -> Unit): Boolean {
        return super.changeBoundsAction(reason, block)
    }

    override fun onChangeBoundsAfter(reason: Reason) {
        super.onChangeBoundsAfter(reason)
    }

    override fun onItemBoundsChanged(itemRenderer: IRenderer, reason: Reason, oldBounds: RectF) {
        if (selectItemList.contains(itemRenderer)) {
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
        selectItemList.clear()
        selectRect.setEmpty()
        //重置滚动
        rotate = 0f
        rendererItem = null
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

    /**预览*/
    override fun preview(): Drawable? {
        val bounds = getRotateBounds()
        return canvasDelegate.getBitmap(bounds).toDrawable(canvasDelegate.view.resources)
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        /*return when (type) {
            ControlPoint.POINT_TYPE_DELETE -> true
            ControlPoint.POINT_TYPE_SCALE -> true
            else -> false
        }*/
        //return super.isSupportControlPoint(type)
        /*if (type == ControlPoint.POINT_TYPE_LOCK) {
            //不支持任意比例缩放
            return false
        }
        return true*/
        return super.isSupportControlPoint(type)
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
        //super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldValue)
        updateSelectBounds()
    }

    override fun onControlFinish(controlPoint: ControlPoint) {
        super.onControlFinish(controlPoint)
        /*if (controlPoint is RotateControlPoint) {
            updateSelectBounds()
        }*/
    }

    override fun itemRotateChanged(oldRotate: Float, rotateFlag: Int) {
        super.itemRotateChanged(oldRotate, rotateFlag)
        val degrees = rotate - oldRotate
        canvasDelegate.boundsOperateHandler.rotateItemList(
            selectItemList,
            degrees,
            getBounds().centerX(),
            getBounds().centerY(),
            Reason(Reason.REASON_CODE, flag = Reason.REASON_FLAG_ROTATE)
        )
    }

    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        super.itemBoundsChanged(reason, oldBounds)
        if (selectItemList.isEmpty()) {
            return
        }
        val renderers = selectItemList.toList()
        if (reason.reason == Reason.REASON_USER) {
            if (reason.flag.have(Reason.REASON_FLAG_TRANSLATE) || reason.flag.have(Reason.REASON_FLAG_BOUNDS)) {
                if (groupTouchDownItemBoundsList.isEmpty()) {
                    //如果不是手势调整的Bounds
                    canvasDelegate.boundsOperateHandler.changeBoundsItemList(
                        renderers,
                        oldBounds,
                        getBounds(),
                        getBoundsScaleAnchor(),
                        rotate,
                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_BOUNDS)
                    )
                } else {
                    //如果是手势调整的Bounds
                    val itemOriginBoundsList = mutableListOf<RectF>()
                    for (render in renderers) {
                        groupTouchDownItemBoundsList.find { it.renderer == render }?.let {
                            itemOriginBoundsList.add(it.bounds)
                        }
                    }
                    canvasDelegate.boundsOperateHandler.changeBoundsItemList(
                        renderers,
                        itemOriginBoundsList,
                        groupTouchDownBounds,
                        getBounds(),
                        getBoundsScaleAnchor(),
                        rotate,
                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_BOUNDS)
                    )
                }
            }
        }
    }

    /**按下时, 最开始的Bounds*/
    val groupTouchDownBounds = acquireTempRectF()

    /**按下时, 选中元素开始的Bounds*/
    val groupTouchDownItemBoundsList = mutableListOf<RendererBounds>()

    override fun onScaleControlStart(controlPoint: ScaleControlPoint) {
        super.onScaleControlStart(controlPoint)
        //记录开始时所有item的bounds
        groupTouchDownBounds.set(getBounds())
        groupTouchDownItemBoundsList.clear()
        selectItemList.forEach {
            groupTouchDownItemBoundsList.add(RendererBounds(it))
        }
    }

    override fun onScaleControlFinish(controlPoint: ScaleControlPoint, rect: RectF, end: Boolean) {
        super.onScaleControlFinish(controlPoint, rect, end)
        if (end) {
            for (render in selectItemList) {
                if (render is DataItemRenderer) {
                    val renderItem = render.rendererItem
                    if (renderItem is DataItem && renderItem.needUpdateOfBoundsChanged(
                            Reason(Reason.REASON_USER, false, Reason.REASON_FLAG_BOUNDS)
                        )
                    ) {
                        renderItem.updateRenderItem(render)
                    }
                }
            }
            groupTouchDownItemBoundsList.clear()
        }
    }

    /**更新选中的bounds大小, 需要包含所有选中的元素*/
    fun updateSelectBounds(resetRotate: Boolean = true) {
        if (selectItemList.isEmpty()) {
            return
        }
        if (resetRotate) {
            rotate = 0f//重置旋转
        }
        changeBoundsAction(Reason(Reason.REASON_CODE, true)) {
            var l = 0f
            var t = 0f
            var r = 0f
            var b = 0f
            selectItemList.forEachIndexed { index, renderer ->
                val rotateBounds = renderer.getRotateBounds().adjustFlipRect(acquireTempRectF())
                if (index == 0) {
                    l = rotateBounds.left
                    t = rotateBounds.top

                    r = rotateBounds.right
                    b = rotateBounds.bottom
                } else {
                    l = min(l, rotateBounds.left)
                    t = min(t, rotateBounds.top)

                    r = max(r, rotateBounds.right)
                    b = max(b, rotateBounds.bottom)
                }
                rotateBounds.release()
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

    /**重新选择元素列表*/
    fun selectedRendererList(list: List<BaseItemRenderer<*>>, strategy: Strategy) {
        if (list.isEmpty()) {
            return
        }
        val oldList = selectItemList.toList()
        selectItemList.clear()
        selectItemList.addAll(list)
        updateSelectBounds()
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

    /**清空选中的列表*/
    fun clearSelectedList(strategy: Strategy) {
        val oldList = selectItemList.toList()
        selectItemList.clear()
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

    /**更新选中子项的对齐方式
     * [align] [Gravity.LEFT]*/
    fun updateAlign(align: Int = Gravity.LEFT, strategy: Strategy = Strategy.normal) {
        val list = selectItemList
        if (list.size() <= 1) {
            return
        }

        //寻找定位锚点item
        var anchorItemRenderer: BaseItemRenderer<*>? = null
        if (align.isGravityCenter()) {
            //找出距离中心点最近的Item
            val centerX = getBounds().centerX()
            val centerY = getBounds().centerY()

            //2点之间的最小距离
            var minR = Float.MAX_VALUE

            list.forEach {
                val bounds = it.getRotateBounds()
                val r = c(centerX, centerY, bounds.centerX(), bounds.centerY()).absoluteValue
                if (r < minR) {
                    anchorItemRenderer = it
                    minR = r.toFloat()
                }
            }
        } else if (align.isGravityCenterHorizontal()) {
            //水平居中, 找出最大的高度item
            var maxHeight = Float.MIN_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.height() > maxHeight) {
                    anchorItemRenderer = it
                    maxHeight = bounds.height()
                }
            }
        } else if (align.isGravityCenterVertical()) {
            //垂直居中, 找出最大的宽度item
            var maxWidth = Float.MIN_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.width() > maxWidth) {
                    anchorItemRenderer = it
                    maxWidth = bounds.width()
                }
            }
        } else if (align.isGravityTop()) {
            var minTop = Float.MAX_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.top < minTop) {
                    anchorItemRenderer = it
                    minTop = bounds.top
                }
            }
        } else if (align.isGravityBottom()) {
            var maxBottom = Float.MIN_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.bottom > maxBottom) {
                    anchorItemRenderer = it
                    maxBottom = bounds.bottom
                }
            }
        } else if (align.isGravityLeft()) {
            var minLeft = Float.MAX_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.left < minLeft) {
                    anchorItemRenderer = it
                    minLeft = bounds.left
                }
            }
        } else if (align.isGravityRight()) {
            var maxRight = Float.MIN_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.right > maxRight) {
                    anchorItemRenderer = it
                    maxRight = bounds.right
                }
            }
        }

        if (anchorItemRenderer == null) {
            return
        }

        val offsetList = mutableListOf<OffsetItemData>()

        val anchorBounds = anchorItemRenderer!!.getRotateBounds()
        for (item in list) {
            if (item != anchorItemRenderer) {
                val itemBounds = item.getRotateBounds()
                //开始调整
                var dx = 0f
                var dy = 0f

                if (align.isGravityCenter()) {
                    dx = anchorBounds.centerX() - itemBounds.centerX()
                    dy = anchorBounds.centerY() - itemBounds.centerY()
                } else if (align.isGravityCenterHorizontal()) {
                    dy = anchorBounds.centerY() - itemBounds.centerY()
                } else if (align.isGravityCenterVertical()) {
                    dx = anchorBounds.centerX() - itemBounds.centerX()
                } else if (align.isGravityTop()) {
                    dy = anchorBounds.top - itemBounds.top
                } else if (align.isGravityBottom()) {
                    dy = anchorBounds.bottom - itemBounds.bottom
                } else if (align.isGravityLeft()) {
                    dx = anchorBounds.left - itemBounds.left
                } else if (align.isGravityRight()) {
                    dx = anchorBounds.right - itemBounds.right
                }
                offsetList.add(OffsetItemData(item, dx, dy))
            }
        }
        canvasDelegate.boundsOperateHandler.offsetItemList(
            canvasDelegate,
            this,
            offsetList,
            strategy
        )
    }
}