package com.angcyo.canvas.core.component.control

import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.core.component.ControlPoint
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
                        itemRenderer.onScaleControlFinish(this@ScaleControlPoint, rect, end)
                    }
                    initializeAnchor(bounds, itemRenderer.rotate, bounds.left, bounds.top)
                    onTouchDown(point.x, point.y)
                }
                itemRenderer.onScaleControlStart(this)
            }
            MotionEvent.ACTION_MOVE -> {
                rectScaleGestureHandler.onTouchMove(point.x, point.y)
                /*canvasDelegate.smartAssistant.smartChangeBounds(
                    itemRenderer,
                    isLockScaleRatio,
                    newWidth,
                    newHeight,
                    dx,
                    dy,
                    if (isCenterScale) ADJUST_TYPE_CENTER else ADJUST_TYPE_LT
                ).apply {
                    if (this[0]) {
                        _moveStartPoint.x = _movePoint.x
                    }
                    if (this[1]) {
                        _moveStartPoint.y = _movePoint.y
                    }
                }*/
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                rectScaleGestureHandler.onTouchFinish(point.x, point.y)
                /*if (!touchItemBounds.isNoSize() && isScaled) {
                    itemRenderer.let {
                        val itemList = mutableListOf<BaseItemRenderer<*>>()
                        if (it is SelectGroupRenderer) {
                            itemList.addAll(it.selectItemList)
                        }
                        canvasDelegate.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                            val item = it
                            val originBounds = RectF(touchItemBounds)
                            val newBounds = RectF(it.getBounds())

                            override fun runUndo() {
                                if (item is SelectGroupRenderer) {
                                    canvasDelegate.boundsOperateHandler.changeBoundsItemList(
                                        itemList,
                                        newBounds,
                                        originBounds
                                    )
                                    if (canvasDelegate.getSelectedRenderer() == item) {
                                        item.updateSelectBounds()
                                    }
                                } else {
                                    item.changeBounds {
                                        set(originBounds)
                                    }
                                }
                            }

                            override fun runRedo() {
                                if (item is SelectGroupRenderer) {
                                    canvasDelegate.boundsOperateHandler.changeBoundsItemList(
                                        itemList,
                                        originBounds,
                                        newBounds
                                    )
                                    if (canvasDelegate.getSelectedRenderer() == item) {
                                        item.updateSelectBounds()
                                    }
                                } else {
                                    item.changeBounds {
                                        set(newBounds)
                                    }
                                }
                            }
                        })
                    }
                }*/
            }
        }
        point.release()
        return true
    }
}