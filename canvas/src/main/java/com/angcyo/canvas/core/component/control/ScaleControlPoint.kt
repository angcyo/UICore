package com.angcyo.canvas.core.component.control

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.Reason
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.core.renderer.SelectGroupRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex._drawable
import com.angcyo.library.gesture.RectScaleGestureHandler

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
class ScaleControlPoint : ControlPoint() {

    var isLockScaleRatio: Boolean = true
        set(value) {
            field = value
            drawable = if (value) {
                _drawable(R.drawable.canvas_control_point_scale)
            } else {
                _drawable(R.drawable.canvas_control_point_scale_any)
            }
        }

    val rectScaleGestureHandler = RectScaleGestureHandler()

    init {
        drawable = _drawable(R.drawable.canvas_control_point_scale)
    }

    override fun onTouch(
        canvasDelegate: CanvasDelegate,
        itemRenderer: BaseItemRenderer<*>,
        event: MotionEvent
    ): Boolean {
        val canvasViewBox = canvasDelegate.getCanvasViewBox()
        val point = acquireTempPointF()
        point.set(event.x, event.y)
        canvasViewBox.viewPointToCoordinateSystemPoint(point, point)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val bounds = itemRenderer.getBounds()
                rectScaleGestureHandler.apply {
                    keepScaleRatio = if (itemRenderer.isLineShape()) {
                        false
                    } else {
                        isLockScaleRatio
                    }
                    onLimitHeightScaleAction = { scaleY ->
                        if (itemRenderer.isLineShape()) {
                            1f
                        } else {
                            scaleY
                        }
                    }
                    onRectScaleChangeAction = { rect, end ->
                        canvasDelegate.smartAssistant.smartChangeBounds(
                            itemRenderer,
                            isLockScaleRatio,
                            rect.width(),
                            rect.height(),
                            _touchDx,
                            _touchDY,
                            itemRenderer.getBoundsScaleAnchor()
                        )
                        if (end) {
                            addChangeBoundsUndoAction(canvasDelegate, itemRenderer)
                        }
                        itemRenderer.onScaleControlFinish(this@ScaleControlPoint, rect, end)
                    }
                    initializeAnchorWithRotate(bounds, itemRenderer.rotate, bounds.left, bounds.top)
                    onTouchDown(point.x, point.y)
                }
                itemRenderer.onScaleControlStart(this)
            }
            MotionEvent.ACTION_MOVE -> {
                rectScaleGestureHandler.onTouchMove(point.x, point.y)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                rectScaleGestureHandler.onTouchFinish(point.x, point.y)
            }
        }
        point.release()
        return true
    }

    /**添加回退操作*/
    fun addChangeBoundsUndoAction(
        canvasDelegate: CanvasDelegate,
        itemRenderer: BaseItemRenderer<*>
    ) {
        val itemList = mutableListOf<BaseItemRenderer<*>>()
        if (itemRenderer is SelectGroupRenderer) {
            itemList.addAll(itemRenderer.selectItemList)
        }
        canvasDelegate.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
            val item = itemRenderer
            val originBounds = RectF(rectScaleGestureHandler.targetRect)
            val newBounds = RectF(itemRenderer.getBounds())
            val anchor = PointF().apply {
                set(itemRenderer.getBoundsScaleAnchor())
            }
            val rotate = itemRenderer.rotate

            override fun runUndo() {
                if (item is SelectGroupRenderer) {
                    canvasDelegate.itemsOperateHandler.changeBoundsItemList(
                        itemList,
                        newBounds,
                        originBounds,
                        anchor,
                        rotate,
                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_BOUNDS)
                    )
                    if (canvasDelegate.getSelectedRenderer() == item) {
                        item.updateSelectBounds()
                    }
                } else {
                    item.changeBoundsAction {
                        set(originBounds)
                    }
                }
            }

            override fun runRedo() {
                if (item is SelectGroupRenderer) {
                    canvasDelegate.itemsOperateHandler.changeBoundsItemList(
                        itemList,
                        originBounds,
                        newBounds,
                        anchor,
                        rotate,
                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_BOUNDS)
                    )
                    if (canvasDelegate.getSelectedRenderer() == item) {
                        item.updateSelectBounds()
                    }
                } else {
                    item.changeBoundsAction {
                        set(newBounds)
                    }
                }
            }
        })
    }
}