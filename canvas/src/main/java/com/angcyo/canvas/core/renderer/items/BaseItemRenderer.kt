package com.angcyo.canvas.core.renderer.items

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.items.BaseItem
import com.angcyo.canvas.core.renderer.BaseRenderer
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.mapRectF

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

    val _tempMatrix = Matrix()

    override fun mapRotatePoint(point: PointF, result: PointF): PointF {
        return rendererItem?.run {
            _tempMatrix.reset()
            _tempMatrix.postRotate(
                rotate,
                getRendererBounds().centerX(),
                getRendererBounds().centerY()
            )
            _tempMatrix.mapPoint(point, result)
        } ?: point
    }

    override fun mapRotateRect(rect: RectF, result: RectF): RectF {
        return rendererItem?.run {
            _tempMatrix.reset()
            _tempMatrix.postRotate(
                rotate,
                getRendererBounds().centerX(),
                getRendererBounds().centerY()
            )
            _tempMatrix.mapRectF(rect, result)
        } ?: rect
    }
}