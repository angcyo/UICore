package com.angcyo.canvas.core.renderer.items

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.items.BaseItem
import com.angcyo.canvas.core.renderer.BaseRenderer
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.mapRectF
import com.angcyo.library.ex.contains

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseItemRenderer<T : BaseItem>(canvasViewBox: CanvasViewBox) :
    BaseRenderer(canvasViewBox), IItemRenderer<T> {

    /**需要渲染的数据*/
    override var rendererItem: T? = null
        set(value) {
            val old = field
            field = value
            if (old != value && value != null) {
                onUpdateRendererItem(value)
            }
        }

    /**旋转的[Matrix]*/
    var rotateMatrix: Matrix? = null
        get() {
            return rendererItem?.run {
                if (field == null) {
                    field = Matrix()
                }
                field?.reset()
                field?.postRotate(
                    rotate,
                    getRendererBounds().centerX(),
                    getRendererBounds().centerY()
                )
                field
            }
        }

    override fun mapRotatePoint(point: PointF, result: PointF): PointF {
        return rotateMatrix?.mapPoint(point, result) ?: point
    }

    override fun mapRotateRect(rect: RectF, result: RectF): RectF {
        return rotateMatrix?.mapRectF(rect, result) ?: result
    }

    val rotatePath: Path = Path()

    override fun containsPoint(point: PointF): Boolean {
        val rendererBounds = getRendererBounds()
        return rotateMatrix?.run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(point.x.toInt(), point.y.toInt())
        } ?: rendererBounds.contains(point.x, point.y)
    }

    override fun containsRect(rect: RectF): Boolean {
        val rendererBounds = getRendererBounds()
        return rotateMatrix?.run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(rect)
        } ?: rendererBounds.contains(rect)
    }
}