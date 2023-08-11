package com.angcyo.canvas.render.core.component

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.library.L
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.distance
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.getScaleY
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

    @CanvasInsideCoordinate
    private val tempMovePointInside = PointF()

    /**当元素过小时, 缩放倍数直接使用理想值*/
    var minSizeThreshold = 1f

    /**是否激活反向缩放*/
    var reverseScale = false

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
        }
        return true
    }

    override fun onTouchMoveEvent(event: MotionEvent) {
        val tx = getTouchTranslateDxInside()
        val ty = getTouchTranslateDyInside()

        if (isControlHappen ||
            tx.absoluteValue >= translateThreshold ||
            ty.absoluteValue >= translateThreshold
        ) {
            //已经发生过移动, 或者移动距离大于阈值
            if (tx != 0f && ty != 0f) {
                var targetSx = 1f
                var targetSy = 1f

                tempMovePointInside.set(touchMovePointInside)
                var newSmartWidth: Float? = null
                var newSmartHeight: Float? = null
                if (smartAssistantComponent.isEnableComponent &&
                    controlRendererAngle == 0f && /*未旋转时才推荐宽高*/
                    (touchMovePointInside.x > anchorPoint.x && touchMovePointInside.y > anchorPoint.y) /*正向拖拽才有宽高提示*/
                ) {
                    smartAssistantComponent.findSmartWidth(
                        controlRendererBounds,
                        tx,
                        getTouchMoveDx()
                    )?.let {
                        newSmartWidth = it
                    }
                    if (!isLockScaleRatio) {
                        //不等比的情况下, 才推荐高度
                        smartAssistantComponent.findSmartHeight(
                            controlRendererBounds,
                            ty,
                            getTouchMoveDy()
                        )?.let {
                            newSmartHeight = it
                        }
                    }
                }

                if (isLockScaleRatio) {
                    //等比缩放, 使用C边的长度计算, 返回的长度一定是正值
                    val oldC = distance(anchorPoint, touchDownPointInside, false)
                    val newC = distance(anchorPoint, tempMovePointInside, false)

                    //负值表示反向了
                    var s = if (rendererBounds.width() <= minSizeThreshold &&
                        rendererBounds.height() <= minSizeThreshold
                    ) {
                        //如果宽高都小于一个很小的值, 则倍数要很大的值才理想
                        newC.toFloat()
                    } else {
                        (newC / oldC).toFloat()
                    }

                    //智能调整
                    if (newSmartWidth != null) {
                        s = if (rendererBounds.width() <= minSizeThreshold) newSmartWidth!!
                        else newSmartWidth!! / rendererBounds.width()
                    }

                    /*//x轴是否反向
                    val xReverse =
                        if (touchDownPointInside.x > anchorPoint.x) touchMovePointInside.x < anchorPoint.x else touchMovePointInside.x > anchorPoint.x

                    //y轴是否反向
                    val yReverse =
                        if (touchDownPointInside.y > anchorPoint.y) touchMovePointInside.y < anchorPoint.y else touchMovePointInside.y > anchorPoint.y

                    if (xReverse || yReverse) {
                        //拖动到反方为了
                        s = -s
                    }*/

                    targetSx = s
                    targetSy = s
                } else {
                    invertTouchMovePoint.set(tempMovePointInside)
                    invertRotateMatrix.mapPoint(invertTouchMovePoint)

                    val oldWidth = invertTouchDownPoint.x - invertAnchorPoint.x
                    val oldHeight = invertTouchDownPoint.y - invertAnchorPoint.y

                    val newWidth = invertTouchMovePoint.x - invertAnchorPoint.x
                    val newHeight = invertTouchMovePoint.y - invertAnchorPoint.y

                    //负数即表示反向了
                    var sx = if (rendererBounds.width() <= minSizeThreshold) newWidth
                    else newWidth / oldWidth
                    var sy = if (rendererBounds.height() <= minSizeThreshold) newHeight
                    else newHeight / oldHeight

                    //智能调整
                    if (newSmartWidth != null) {
                        sx = if (rendererBounds.width() <= minSizeThreshold) newSmartWidth!!
                        else newSmartWidth!! / rendererBounds.width()
                    }
                    if (newSmartHeight != null) {
                        sy =
                            if (rendererBounds.height() <= minSizeThreshold) newSmartHeight!!
                            else newSmartHeight!! / rendererBounds.height()
                    }

                    targetSx = sx
                    targetSy = sy
                }
                scale(targetSx, targetSy)
            }
        }

    }

    /**在按下的基础上, 缩放了多少*/
    private fun scale(sx: Float, sy: Float) {
        if (!reverseScale) {
            if (sx <= 0 || sy <= 0) {
                L.w("不支持反向缩放元素:sx:$sx sy:$sy")
                return
            }
        }
        L.d("缩放元素:sx:$sx sy:$sy")
        controlRendererInfo?.let { controlInfo ->
            controlInfo.state.renderProperty?.let { property ->
                updateControlHappen(true)

                //需要缩放的矩阵
                controlMatrix.setScale(
                    sx,
                    sy,
                    property.anchorX,
                    property.anchorY
                )

                //limit
                controlManager.delegate.dispatchLimitControlMatrix(
                    this,
                    controlInfo.controlRenderer,
                    controlMatrix,
                    CONTROL_TYPE_SCALE
                )

                //计算缩放后的矩阵
                controlMatrix.setScale(
                    controlMatrix.getScaleX(),
                    controlMatrix.getScaleY(),
                    property.anchorX,
                    property.anchorY
                )

                applyScale(Reason.preview, controlManager.delegate)
            }
        }
    }

    override fun endControl() {
        if (isNeedApply()) {
            controlRendererInfo?.let {
                applyScale(Reason.user.apply {
                    controlType = CONTROL_TYPE_SCALE
                }, controlManager.delegate)
            }
        }
        super.endControl()
    }

}