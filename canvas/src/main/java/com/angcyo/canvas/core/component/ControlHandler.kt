package com.angcyo.canvas.core.component

import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.contains
import com.angcyo.canvas.R
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.renderer.items.IItemRenderer
import com.angcyo.canvas.utils.mapRectF
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.dpi

/**
 * 控制渲染的数据组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class ControlHandler : BaseComponent() {

    /**当前选中的[IItemRenderer]*/
    var selectedItemRender: IItemRenderer? = null

    /**绘制宽高时的偏移量*/
    var sizeOffset = 4 * dp

    //<editor-fold desc="4个控制点">

    /**所有的控制点*/
    val controlPointList = mutableListOf<ControlPoint>()

    /**控制点的大小, 背景圆的直径*/
    var controlPointSize = 20 * dp

    /**图标padding的大小*/
    var controlPointPadding: Int = 4 * dpi

    /**相对于目标点的偏移距离*/
    var controlPointOffset = 4 * dp

    //缓存
    val controlPointOffsetRect = RectF()

    //</editor-fold desc="4个控制点">

    /**通过坐标, 找到对应的元素*/
    fun findItemRenderer(canvasViewBox: CanvasViewBox, touchPoint: PointF): IItemRenderer? {
        val point = canvasViewBox.mapCoordinateSystemPoint(touchPoint)
        canvasViewBox.canvasView.itemsRendererList.reversed().forEach {
            if (it.getRendererBounds().contains(point)) {
                return it
            }
        }
        return null
    }

    /**计算4个控制点的矩形位置坐标
     * [itemRect] 目标元素坐标系的矩形坐标*/
    fun calcControlPointLocation(canvasViewBox: CanvasViewBox, itemRenderer: IItemRenderer) {
        val srcRect = itemRenderer.getRendererBounds()
        val _srcRect = canvasViewBox.matrix.mapRectF(srcRect, _tempRect)
        controlPointOffsetRect.set(_srcRect)

        val inset = controlPointOffset + controlPointSize / 2
        controlPointOffsetRect.inset(-inset, -inset)

        val closeControl = controlPointList.find { it.type == ControlPoint.POINT_TYPE_CLOSE }
            ?: createControlPoint(ControlPoint.POINT_TYPE_CLOSE)
        val rotateControl = controlPointList.find { it.type == ControlPoint.POINT_TYPE_ROTATE }
            ?: createControlPoint(ControlPoint.POINT_TYPE_ROTATE)
        val scaleControl = controlPointList.find { it.type == ControlPoint.POINT_TYPE_SCALE }
            ?: createControlPoint(ControlPoint.POINT_TYPE_CLOSE)
        val lockControl = controlPointList.find { it.type == ControlPoint.POINT_TYPE_LOCK }
            ?: createControlPoint(ControlPoint.POINT_TYPE_CLOSE)
        updateControlPoint(
            closeControl,
            canvasViewBox,
            itemRenderer,
            controlPointOffsetRect.left,
            controlPointOffsetRect.top
        )
        updateControlPoint(
            rotateControl,
            canvasViewBox,
            itemRenderer,
            controlPointOffsetRect.right,
            controlPointOffsetRect.top,
        )
        updateControlPoint(
            scaleControl,
            canvasViewBox,
            itemRenderer,
            controlPointOffsetRect.right,
            controlPointOffsetRect.bottom,
        )
        updateControlPoint(
            lockControl,
            canvasViewBox,
            itemRenderer,
            controlPointOffsetRect.left,
            controlPointOffsetRect.bottom,
        )

        controlPointList.clear()
        controlPointList.add(closeControl)
        controlPointList.add(rotateControl)
        controlPointList.add(scaleControl)
        controlPointList.add(lockControl)
    }

    /**创建一个控制点*/
    fun createControlPoint(type: Int): ControlPoint {
        return ControlPoint(
            RectF(), type, when (type) {
                ControlPoint.POINT_TYPE_CLOSE -> _drawable(R.drawable.control_point_close)
                ControlPoint.POINT_TYPE_ROTATE -> _drawable(R.drawable.control_point_rotate)
                ControlPoint.POINT_TYPE_SCALE -> _drawable(R.drawable.control_point_scale)
                ControlPoint.POINT_TYPE_LOCK -> _drawable(R.drawable.control_point_lock)
                else -> null
            }
        )
    }

    /**更新控制点的位置*/
    fun updateControlPoint(
        controlPoint: ControlPoint,
        canvasViewBox: CanvasViewBox,
        itemRenderer: IItemRenderer,
        x: Float,
        y: Float
    ) {
        _tempPoint.set(x, y)
        val point = itemRenderer.transformer.mapPointF(_tempPoint, _tempPoint)

        controlPoint.bounds.set(
            point.x - controlPointSize / 2,
            point.y - controlPointSize / 2,
            point.x + controlPointSize / 2,
            point.y + controlPointSize / 2
        )
    }
}

/**控制点信息*/
data class ControlPoint(
    /**控制点坐标系的坐标*/
    val bounds: RectF,
    /**控制点的类型*/
    var type: Int,
    /**图标*/
    val drawable: Drawable?
) {
    companion object {
        /**控制点类型: 删除*/
        const val POINT_TYPE_CLOSE = 1

        /**控制点类型: 旋转*/
        const val POINT_TYPE_ROTATE = 2

        /**控制点类型: 缩放*/
        const val POINT_TYPE_SCALE = 3

        /**控制点类型: 锁定缩放比例*/
        const val POINT_TYPE_LOCK = 4
    }
}