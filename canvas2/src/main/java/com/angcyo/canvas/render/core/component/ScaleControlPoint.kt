package com.angcyo.canvas.render.core.component

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.canvas.render.core.Reason
import com.angcyo.library.L
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.distance
import com.angcyo.library.ex.mapPoint
import kotlin.math.absoluteValue

/**
 * 缩放控制点
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/22
 */
class ScaleControlPoint(controlManager: CanvasControlManager) : BaseControlPoint(controlManager) {

    /**是否锁定了宽高比*/
    var isLockScaleRatio: Boolean = true
        set(value) {
            field = value
            drawable = if (value) {
                _drawable(R.drawable.canvas_render_control_point_scale)
            } else {
                _drawable(R.drawable.canvas_render_control_point_scale_any)
            }
        }

    /**是否要支持反向缩放, 即缩放比例是否允许变成负值
     * 如果关闭方向缩放, 则缩放到反反向时, 不会触发apply
     * */
    var enableReverseScale: Boolean = true

    /**锚点坐标, 旋转后的坐标*/
    @CanvasInsideCoordinate
    private val anchorPoint = PointF()

    /**锚点坐标, 反向旋转后的坐标*/
    @CanvasInsideCoordinate
    private val invertAnchorPoint = PointF()

    /**第一个按下的点, 反向旋转时的坐标*/
    @CanvasInsideCoordinate
    private val invertTouchDownPoint = PointF()

    @CanvasInsideCoordinate
    private val invertTouchMovePoint = PointF()

    /**反向旋转的矩阵*/
    private val invertRotateMatrix = Matrix()

    private val rendererBounds = RectF()

    /**当元素过小时, 缩放倍数直接使用理想值*/
    var minSizeThreshold = 10

    init {
        controlPointType = CONTROL_TYPE_SCALE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val selectorComponent = controlManager.delegate.selectorManager.selectorComponent
                startControl(selectorComponent)

                selectorComponent.renderProperty?.let {
                    it.getRenderBounds(rendererBounds)
                    anchorPoint.set(it.anchorX, it.anchorY)
                    it.getRenderCenter(_tempPoint)
                    invertRotateMatrix.setRotate(it.angle, _tempPoint.x, _tempPoint.y)
                    invertRotateMatrix.invert(invertRotateMatrix)

                    //反向旋转锚点
                    invertRotateMatrix.mapPoint(anchorPoint, invertAnchorPoint)

                    invertTouchDownPoint.set(touchDownPointInside)
                    invertRotateMatrix.mapPoint(invertTouchDownPoint)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val tx = touchMovePointInside.x - touchDownPointInside.x
                val ty = touchMovePointInside.y - touchDownPointInside.y
                if (isControlHappen ||
                    tx.absoluteValue >= translateThreshold ||
                    ty.absoluteValue >= translateThreshold
                ) {
                    //已经发生过移动, 或者移动距离大于阈值
                    if (tx != 0f && ty != 0f) {
                        var targetSx = 1f
                        var targetSy = 1f

                        if (isLockScaleRatio) {
                            //等比缩放, 使用C边的长度计算, 返回的长度一定是正值
                            val oldC = distance(touchDownPointInside, anchorPoint)
                            val newC = distance(touchMovePointInside, anchorPoint)
                            var s = if (rendererBounds.width() <= minSizeThreshold ||
                                rendererBounds.height() <= minSizeThreshold
                            ) {
                                newC.toFloat()
                            } else {
                                (newC / oldC).toFloat()
                            }

                            //x轴是否反向
                            val xReverse =
                                if (touchDownPointInside.x > anchorPoint.x) touchMovePointInside.x < anchorPoint.x else touchMovePointInside.x > anchorPoint.x

                            //y轴是否反向
                            val yReverse =
                                if (touchDownPointInside.y > anchorPoint.y) touchMovePointInside.y < anchorPoint.y else touchMovePointInside.y > anchorPoint.y

                            if (xReverse && yReverse) {
                                //拖动到反方为了
                                s = -s
                            }
                            targetSx = s
                            targetSy = s
                        } else {
                            invertTouchMovePoint.set(touchMovePointInside)
                            invertRotateMatrix.mapPoint(invertTouchMovePoint)

                            val oldWidth = invertTouchDownPoint.x - invertAnchorPoint.x
                            val oldHeight = invertTouchDownPoint.y - invertAnchorPoint.y

                            val newWidth = invertTouchMovePoint.x - invertAnchorPoint.x
                            val newHeight = invertTouchMovePoint.y - invertAnchorPoint.y

                            val sx =
                                if (rendererBounds.width() <= minSizeThreshold) newWidth else newWidth / oldWidth
                            val sy =
                                if (rendererBounds.height() <= minSizeThreshold) newHeight else newHeight / oldHeight

                            /*//x轴是否反向
                            val xReverse =
                                if (invertTouchDownPoint.x > invertAnchorPoint.x) invertTouchMovePoint.x < invertAnchorPoint.x else invertTouchMovePoint.x > invertAnchorPoint.x

                            //y轴是否反向
                            val yReverse =
                                if (invertTouchDownPoint.y > invertAnchorPoint.y) invertTouchMovePoint.y < invertAnchorPoint.y else invertTouchMovePoint.y > invertAnchorPoint.y

                            if (xReverse) {
                                //拖动到反方为了
                                sx = -sx
                            }
                            if (yReverse) {
                                //拖动到反方为了
                                sy = -sy
                            }*/

                            targetSx = sx
                            targetSy = sy
                        }
                        if (enableReverseScale || (targetSx > 0 && targetSy > 0)) {
                            scale(targetSx, targetSy)
                        }
                    }
                }
            }
        }
        return true
    }

    /**在按下的基础上, 缩放了多少*/
    private fun scale(sx: Float, sy: Float) {
        L.d("缩放元素:sx:$sx sy:$sy")
        controlRendererInfo?.let {
            it.state.renderProperty?.let { property ->
                isControlHappen = true
                controlMatrix.setScale(sx, sy, property.anchorX, property.anchorY)

                applyScale(Reason.preview, controlManager.delegate)
            }
        }
    }

    override fun endControl() {
        if (isControlHappen) {
            controlRendererInfo?.let {
                applyScale(Reason.user.apply {
                    controlType = CONTROL_TYPE_KEEP_GROUP_PROPERTY or
                            CONTROL_TYPE_SCALE
                }, controlManager.delegate)
            }
        }
        super.endControl()
    }

}