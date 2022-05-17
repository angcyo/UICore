package com.angcyo.canvas.core

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.Reason
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils._tempValues
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.toRadians
import com.angcyo.library.ex._tempRectF
import com.angcyo.library.ex.isNoSize
import com.angcyo.library.ex.rotate
import com.angcyo.library.ex.scale
import java.lang.Math.tan

/**
 * [BaseItemRenderer]操作处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/05/15
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class OperateHandler {

    /**批量旋转[BaseItemRenderer]*/
    fun rotateItemList(
        list: Iterable<BaseItemRenderer<*>>,
        degrees: Float, //需要旋转多少度. by
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
                list.forEach { item ->
                    item.changeBounds(changeReason) {
                        scale(
                            newBounds.width() / oldBounds.width(),
                            newBounds.height() / oldBounds.height(),
                            newBounds.left,
                            newBounds.top
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

    /**计算当边界Bounds改变后, 真实的Bounds应该怎么变化
     * 支持旋转后的Bounds
     * [bounds] 真实的Bounds, 也是返回值
     * [com.angcyo.canvas.core.OperateHandler.calcBoundsWidthHeightWithFrame]*/
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

}