package com.angcyo.canvas.core

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.core.renderer.SelectGroupRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.library.ex.*
import java.lang.Math.tan

/**
 * [BaseItemRenderer]操作处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/05/15
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class BoundsOperateHandler {

    companion object {

        //region ---can---

        /**是否可以改变Bounds到
         * @return true 表示允许改变*/
        fun canChangeBounds(itemRenderer: BaseItemRenderer<*>, toRect: RectF): Boolean {
            return canChangeBounds(itemRenderer, toRect.width(), toRect.height())
        }

        fun canChangeBounds(
            itemRenderer: BaseItemRenderer<*>,
            toWidth: Float,
            toHeight: Float
        ): Boolean {

            if (itemRenderer is SelectGroupRenderer) {
                //群组选择, 不允许反向
                if (toWidth <= 1f || toHeight <= 1f) {
                    //不允许反向调整
                    return false
                }
                /*var haveLineShape = false
                itemRenderer.selectItemList.forEach {
                    if (it.isLineShape()) {
                        haveLineShape = true
                        return@forEach
                    }
                }
                if (haveLineShape) {
                    //群组内, 有线段时, 不允许反向
                    if (toWidth <= 0f || toHeight <= 0f) {
                        //不允许反向调整
                        return false
                    }
                }*/
            }

            if (toWidth.abs() < 1f || toHeight.abs() < 1f) {
                //不允许设置小于1像素
                return false
            }

            if (toWidth.abs() > 40_000f) {
                //限制宽度最大值
                return false
            }

            if (toHeight.abs() > 40_000f) {
                //限制高度最大值
                return false
            }

            return true
        }

        //endregion ---can---

    }

    /**批量旋转[BaseItemRenderer]*/
    fun rotateItemList(
        list: Iterable<BaseItemRenderer<*>>,
        degrees: Float, //需要旋转多少度. by
        pivotX: Float,
        pivotY: Float
    ) {
        val changeReason = Reason(Reason.REASON_CODE, flag = Reason.REASON_FLAG_ROTATE)
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
        if (newBounds.width() == 0f || newBounds.height() == 0f) {
            //no op
            return
        }
        if (!oldBounds.isNoSize() && oldBounds.width() != 0f && oldBounds.height() != 0f) {
            val changeReason = Reason(Reason.REASON_CODE, flag = Reason.REASON_FLAG_BOUNDS)

            //平移
            val offsetLeft: Float = newBounds.left - oldBounds.left
            val offsetTop: Float = newBounds.top - oldBounds.top
            if (offsetLeft.isFinite() && offsetTop.isFinite() && (offsetLeft != 0f || offsetTop != 0f)) {
                list.forEach { item ->
                    item.changeBounds(changeReason) {
                        offset(offsetLeft, offsetTop)
                    }
                }
            }

            //缩放
            val offsetWidth = newBounds.width() - oldBounds.width()
            val offsetHeight = newBounds.height() - oldBounds.height()
            if (offsetWidth.isFinite() && offsetHeight.isFinite() && (offsetWidth != 0f || offsetHeight != 0f)) {
                val scaleX = newBounds.width() / oldBounds.width()
                val scaleY = newBounds.height() / oldBounds.height()

                val tempBounds = RectF()
                list.forEach { item ->
                    tempBounds.set(item.getBounds())
                    tempBounds.scale(scaleX, scaleY, newBounds.left, newBounds.top)
                    //L.i("缩放矩形:sx:$scaleX sy:$scaleY ->$this")
                    if (canChangeBounds(item, tempBounds)) {
                        item.changeBounds(changeReason) {
                            set(tempBounds)
                        }
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

    /**计算当边界Bounds改变后, 真实的Bounds应该怎么变化
     * 支持旋转后的Bounds
     * [bounds] 真实的Bounds, 也是返回值
     * [com.angcyo.canvas.core.BoundsOperateHandler.calcBoundsWidthHeightWithFrame]*/
    @Deprecated("此算法有问题")
    fun calcBoundsWithFrame(frameFrom: RectF, frameTo: RectF, bounds: RectF) {
        //平移
        val offsetLeft = frameTo.left - frameFrom.left
        val offsetTop = frameTo.top - frameFrom.top
        if (offsetLeft.isFinite() && offsetTop.isFinite() && (offsetLeft != 0f || offsetTop != 0f)) {
            bounds.offset(offsetLeft, offsetTop)
        }

        //缩放
        val offsetWidth = frameTo.width() - frameFrom.width()
        val offsetHeight = frameTo.height() - frameFrom.height()
        if (offsetWidth.isFinite() && offsetHeight.isFinite() && (offsetWidth != 0f || offsetHeight != 0f)) {
            bounds.scale(
                frameTo.width() / frameFrom.width(),
                frameTo.height() / frameFrom.height(),
                frameTo.left,
                frameTo.top
            )
        }
    }

    /**计算当边界Bounds改变后, 计算真实Bounds的宽高
     * 支持旋转后的Bounds
     * [originBounds] 原始真实的Bounds
     * [frameFrom] [frameTo] 边界改变Bounds
     * [rotate] 旋转的角度
     * [lockRatio] 是否要锁定[originBounds]的比例
     * 返回宽高
     * */
    fun calcBoundsWidthHeightWithFrame(
        originBounds: RectF,
        frameFrom: RectF,
        frameTo: RectF,
        rotate: Float,
        lockRatio: Boolean,
        result: FloatArray = _tempValues
    ): FloatArray {
        //左上角的点
        val lt = PointF(originBounds.left, originBounds.top)
        //右下角的点
        val rb = PointF(originBounds.right, originBounds.bottom)

        //将点旋转
        val rotateMatrix = Matrix()
        rotateMatrix.postRotate(rotate, originBounds.centerX(), originBounds.centerY())
        rotateMatrix.mapPoint(lt, lt)
        rotateMatrix.mapPoint(rb, rb)

        //以左上角的点为固定点, 偏移右下角的点, 从而计算出新的宽高
        val offsetRight = frameTo.right - frameFrom.right
        val offsetBottom = frameTo.bottom - frameFrom.bottom

        if (lockRatio) {
            val scale = if (offsetRight == 0f) {
                //主动调整高度
                frameTo.height() / frameFrom.height()
            } else {
                frameTo.width() / frameFrom.width()
            }
            val newHeight = originBounds.height() * scale
            val newWidth = originBounds.width() * scale

            result[0] = newWidth
            result[1] = newHeight

            return result
        } else if (offsetRight == 0f && offsetBottom != 0f) {
            //主动调整高度
            rb.offset(
                (offsetBottom / tan(rotate.toRadians().toDouble())).toFloat(),
                offsetBottom
            )
        } else if (offsetBottom == 0f && offsetRight != 0f) {
            //主动调整宽度
            rb.offset(offsetRight, (offsetRight * tan(rotate.toRadians().toDouble())).toFloat())
        } else {
            rb.offset(offsetRight, offsetBottom)
        }

        //逆旋转回去, 算出两点之间的距离, 就是宽高
        rotateMatrix.reset()
        rotateMatrix.postRotate(rotate, frameTo.centerX(), frameTo.centerY())
        rotateMatrix.invert(rotateMatrix)

        rotateMatrix.mapPoint(lt, lt)
        rotateMatrix.mapPoint(rb, rb)

        result[0] = rb.x - lt.x
        result[1] = rb.y - lt.y

        return result
    }

    /**批量偏移[bounds]*/
    fun offsetItemList(
        canvasDelegate: CanvasDelegate,
        selectGroupRenderer: SelectGroupRenderer?,
        offsetList: List<OffsetItemData>,
        strategy: Strategy
    ) {
        val undoOffsetList = mutableListOf<OffsetItemData>()

        offsetList.forEach { item ->
            undoOffsetList.add(OffsetItemData(item.item, -item.dx, -item.dy))
        }

        val step = object : ICanvasStep {
            override fun runUndo() {
                undoOffsetList.forEach { item ->
                    item.item.changeBounds(
                        Reason(
                            Reason.REASON_CODE,
                            false,
                            Reason.REASON_FLAG_TRANSLATE
                        )
                    ) {
                        offset(item.dx, item.dy)
                    }
                }
                selectGroupRenderer?.updateSelectBounds()
            }

            override fun runRedo() {
                offsetList.forEach { item ->
                    item.item.changeBounds(
                        Reason(
                            Reason.REASON_CODE,
                            false,
                            Reason.REASON_FLAG_TRANSLATE
                        )
                    ) {
                        offset(item.dx, item.dy)
                    }
                }
                selectGroupRenderer?.updateSelectBounds()
            }
        }

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasDelegate.getCanvasUndoManager().addUndoAction(step)
        }
        step.runRedo()
    }
}