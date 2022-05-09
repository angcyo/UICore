package com.angcyo.canvas.core.component

import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.items.renderer.BaseItemRenderer

/**
 * 控制点
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/09
 */
open class ControlPoint : BaseComponent() {

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

    /**控制点视图坐标的位置*/
    val bounds: RectF = RectF()

    /**控制点的类型*/
    var type: Int = -1

    /**图标*/
    var drawable: Drawable? = null

    //是否在控制点按下
    var isTouchDownIn: Boolean = false

    open fun onTouch(
        view: CanvasDelegate,
        itemRenderer: BaseItemRenderer<*>,
        event: MotionEvent
    ): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouchDownIn = bounds.contains(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                //
            }
            MotionEvent.ACTION_UP -> {
                if (isTouchDownIn && bounds.contains(event.x, event.y)) {
                    onClickControlPoint(view, itemRenderer)
                }
            }
        }
        return isTouchDownIn
    }

    /**当点击控制点时回调*/
    open fun onClickControlPoint(view: CanvasDelegate, itemRenderer: BaseItemRenderer<*>) {
        //点击控制点后的操作
    }
}