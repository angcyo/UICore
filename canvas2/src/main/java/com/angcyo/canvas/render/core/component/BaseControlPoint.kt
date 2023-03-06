package com.angcyo.canvas.render.core.component

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.annotation.CanvasOutsideCoordinate
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.data.RendererParams
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.library.ex.*

/**
 * 控制点
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/22
 */
abstract class BaseControlPoint(controlManager: CanvasControlManager) : BaseControl(controlManager),
    IRenderer {

    companion object {
        /**控制点类型: 删除[DeleteControlPoint]*/
        const val CONTROL_TYPE_DELETE = 0x1

        /**控制点类型: 旋转[RotateControlPoint]*/
        const val CONTROL_TYPE_ROTATE = CONTROL_TYPE_DELETE shl 1

        /**控制点类型: 缩放[ScaleControlPoint]*/
        const val CONTROL_TYPE_SCALE = CONTROL_TYPE_ROTATE shl 1

        /**控制点类型: 锁定宽高比[LockControlPoint]*/
        const val CONTROL_TYPE_LOCK = CONTROL_TYPE_SCALE shl 1
    }

    /**控制点的类型*/
    var controlType: Int = 0

    /**控制点的位置, 相对于画板左上角*/
    @CanvasOutsideCoordinate
    val bounds: RectF = emptyRectF()

    /**图标, 不包含背景*/
    var drawable: Drawable? = null

    /**图标padding的大小*/
    var controlPointPadding: Int = 4 * dpi

    /**控制点的大小, 背景圆的直径*/
    var controlPointSize = 24 * dp

    /**相对于目标点的偏移距离
     * 正数向外偏移, 负数向内偏移.
     * 会根据左右上下方向, 自动取反*/
    var controlPointOffset = -4 * dp

    /**用来绘制控制点图标的背景笔*/
    val controlPointPaint = createRenderPaint("#333333".toColor(), style = Paint.Style.FILL)

    /**绘制按下的控制点背景*/
    val controlTouchPointPaint =
        createRenderPaint(_color(R.color.transparent50), style = Paint.Style.FILL)

    override var renderFlags: Int = 0xf

    protected val _tempPoint = PointF()

    override fun renderOnOutside(canvas: Canvas, params: RendererParams) {
        if (isEnable) {
            drawable?.let {

                //控制点的背景绘制
                canvas.drawCircle(
                    bounds.centerX(),
                    bounds.centerY(),
                    controlPointSize / 2,
                    if (handleControl) controlTouchPointPaint else controlPointPaint
                )

                it.setBounds(
                    (bounds.left + controlPointPadding).toInt(),
                    (bounds.top + controlPointPadding).toInt(),
                    (bounds.right - controlPointPadding).toInt(),
                    (bounds.bottom - controlPointPadding).toInt()
                )
                it.draw(canvas)
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (isEnable) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                handleControl = bounds.contains(event.x, event.y)
            }
        }
        return handleControl
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                //
            }
            MotionEvent.ACTION_UP -> {
                if (handleControl && bounds.contains(event.x, event.y)) {
                    onClickControlPoint()
                }
            }
        }
        super.onTouchEvent(event)
        return true
    }

    //region---操作---

    /**当点击控制点时回调*/
    open fun onClickControlPoint() {

    }

    //endregion---操作---

}