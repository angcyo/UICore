package com.angcyo.canvas.data

import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release

/**
 * 控制手势按下时的一些信息
 * [com.angcyo.canvas.core.component.ControlHandler]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/01
 */
data class ControlTouchInfo(
    /**当前选中的item, 如果有*/
    var itemRenderer: BaseItemRenderer<*>? = null,
    /**当前[itemRenderer]的Bounds, 如果有[itemRenderer]*/
    var itemBounds: RectF = acquireTempRectF(),
    /**操作的控制点, 如果有*/
    var controlPoint: ControlPoint? = null,
    /**手指id, 哪个手指触发的事件*/
    var touchPointerId: Int = -1,
    /**手势当前的坐标, 原始坐标*/
    var touchPoint: PointF = acquireTempPointF(),
    /**[touchPoint]相对于坐标系统原点的坐标*/
    var touchSystemPoint: PointF = acquireTempPointF(),
    /**按下时, 下面所有的渲染器*/
    var touchItemRendererList: List<BaseItemRenderer<*>>? = null,
) {
    /**释放资源*/
    fun release() {
        itemBounds.release()
        touchPoint.release()
        touchSystemPoint.release()
    }

    /**更新手势点坐标[touchPoint], 并且同步更新[touchSystemPoint]*/
    fun updateTouchPoint(x: Float, y: Float, canvasViewBox: CanvasViewBox) {
        touchPoint.set(x, y)
        canvasViewBox.viewPointToCoordinateSystemPoint(
            touchPoint,
            touchSystemPoint
        )
    }
}
