package com.angcyo.canvas.core.component

import android.graphics.RectF
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.library.L
import kotlin.math.absoluteValue
import kotlin.math.tan

/**
 * 智能提示助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class SmartAssistant(val canvasView: CanvasView) : BaseComponent(), ICanvasListener {

    /**提示线的坐标*/
    val smartLineList = mutableListOf<RectF>()

    var lastXAssistant: AssistantData? = null
    var lastYAssistant: AssistantData? = null

    /**吸附阈值, 当距离推荐线的距离小于等于此值时, 自动吸附*/
    var adsorbThreshold: Float = 2f

    var xAssistantRect: RectF? = null
    var yAssistantRect: RectF? = null
    var rotateAssistantRect: RectF? = null

    init {
        enable = true
        canvasView.addCanvasListener(this)
    }

    override fun onCanvasTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_CANCEL || event.actionMasked == MotionEvent.ACTION_UP) {
            smartLineList.clear()
            canvasView.refresh()
        }
        return super.onCanvasTouchEvent(event)
    }

    fun resetSmartLine() {
        smartLineList.clear()
        xAssistantRect?.let {
            smartLineList.add(it)
        }
        yAssistantRect?.let {
            smartLineList.add(it)
        }
        rotateAssistantRect?.let {
            smartLineList.add(it)
        }
    }

    /**智能推荐算法平移*/
    fun smartTranslateItemBy(
        itemRenderer: BaseItemRenderer<*>,
        distanceX: Float = 0f,
        distanceY: Float = 0f
    ) {
        if (enable) {
            val renderRotateBounds = itemRenderer.getRenderRotateBounds()

            val left = renderRotateBounds.left
            val top = renderRotateBounds.top

            var dx = distanceX
            var dy = distanceY

            var newLeft = left + distanceX
            var newTop = top + distanceY

            val viewRect =
                canvasView.canvasViewBox.mapCoordinateSystemRect(canvasView.viewBounds, _tempRect)

            var feedback = false

            if (distanceX != 0f) {
                val assistant = findOptimalX(left, newLeft, distanceX > 0)
                if (assistant.isChanged()) {
                    newLeft = assistant.resultValue!!
                    dx = newLeft - left
                    xAssistantRect = RectF(
                        newLeft,
                        viewRect.top,
                        newLeft,
                        viewRect.bottom
                    )
                    feedback = feedback || assistant.isChanged(lastXAssistant)
                    lastXAssistant = assistant
                } else {
                    xAssistantRect = null
                }
            }

            if (distanceY != 0f) {
                val topAssistant = findOptimalY(top, newTop, distanceY > 0)
                if (topAssistant.isChanged()) {
                    newTop = topAssistant.resultValue!!
                    dy = newTop - top
                    yAssistantRect = RectF(
                        viewRect.left,
                        newTop,
                        viewRect.right,
                        newTop
                    )
                    feedback = feedback || topAssistant.isChanged(lastYAssistant)
                    lastYAssistant = topAssistant
                } else {
                    yAssistantRect = null
                }
            }

            if (feedback) {
                //震动反馈
                canvasView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                L.w("智能提示: dx:${distanceX} dy:${distanceY} left:${left} top:${top} newLeft:${newLeft} newTop:${newTop}")
            }

            resetSmartLine()
            canvasView.translateItemBy(itemRenderer, dx, dy)
        } else {
            canvasView.translateItemBy(itemRenderer, distanceX, distanceY)
        }
    }

    /**智能旋转算法*/
    fun smartRotateBy(itemRenderer: BaseItemRenderer<*>, angle: Float): Float {
        if (enable) {
            return angle
        }

        val oldRotate = itemRenderer.rotate
        var rotate = (itemRenderer.rotate + angle).toInt()

        val value = 15 //15个度数取一个值
        if (angle > 0) {
            //顺时针旋转
            while (rotate % value != 0) {
                rotate++
            }
            rotate %= 360
        } else {
            //逆时针旋转
            while (rotate % value != 0) {
                rotate--
            }
            if (rotate < 0) {
                rotate += 360
            }
        }

        val result = rotate - oldRotate
        if (result != angle) {
            //找到了 震动反馈
            val visualBounds = itemRenderer.getVisualBounds()
            smartLineList.clear()
            val left = visualBounds.centerX() + visualBounds.centerY() / tan(rotate.toFloat())
            val top = 0f
            val bottom = canvasView.canvasViewBox.getContentBottom()
            val right =
                visualBounds.centerX() - (bottom - visualBounds.centerY()) / tan(rotate.toFloat())
            smartLineList.add(
                RectF(left, top, right, bottom)
            )
            canvasView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        return result
    }

    /**查找最优的旋转角度
     * [angle] 当前需要旋转的角度
     * @return 返回最优需要旋转的角度*/
    fun findOptimalAngle() {

    }

    /**查找最贴近的横坐标值
     * [origin] 原始的横坐标
     * [newValue] 偏移后的横坐标
     * [forward] true, 表示向正方向查找, 否则向负方向查找
     * */
    fun findOptimalX(origin: Float, newValue: Float, forward: Boolean): AssistantData {
        val result = AssistantData(origin, newValue)
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.canvasViewBox.getScaleX()

            canvasView.xAxis.eachAxisPixelList { index, value ->
                if ((forward && value >= origin) || (!forward && value <= origin)) {
                    val axisLineType =
                        canvasView.xAxis.getAxisLineType(canvasView.canvasViewBox, index, scale)
                    if (axisLineType > 0) {
                        val d = (newValue - value).absoluteValue
                        if (d < min) {
                            result.resultValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newValue).absoluteValue > adsorbThreshold) {
                    //不够接近推荐值
                    result.resultValue = null
                }
            }
        }

        return result
    }

    /**查找最贴近的纵坐标值
     * [origin] 原始的纵坐标
     * [newValue] 偏移后的纵坐标
     * [forward] true, 表示向正方向查找, 否则向负方向查找
     * */
    fun findOptimalY(origin: Float, newValue: Float, forward: Boolean): AssistantData {
        val result = AssistantData(origin, newValue)
        if (canvasView.yAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.canvasViewBox.getScaleY()

            canvasView.yAxis.eachAxisPixelList { index, value ->
                if ((forward && value >= origin) || (!forward && value <= origin)) {
                    val axisLineType =
                        canvasView.yAxis.getAxisLineType(canvasView.canvasViewBox, index, scale)
                    if (axisLineType > 0) {
                        val d = (newValue - value).absoluteValue
                        if (d < min) {
                            result.resultValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newValue).absoluteValue > adsorbThreshold) {
                    //不够接近推荐值
                    result.resultValue = null
                }
            }
        }

        return result
    }

    /**查找当前坐标, 最贴近的提示目标*/
    fun findOptimalLeft(origin: Float, newLeft: Float): AssistantData {
        val result = AssistantData(origin, newLeft)
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.canvasViewBox.getScaleX()

            canvasView.xAxis.eachAxisPixelList { index, value ->
                if (value <= origin) {
                    val axisLineType =
                        canvasView.xAxis.getAxisLineType(canvasView.canvasViewBox, index, scale)
                    if (axisLineType > 0) {
                        val d = newLeft - value
                        if (d < min) {
                            result.resultValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newLeft).absoluteValue > adsorbThreshold) {
                    //不够接近推荐值
                    result.resultValue = null
                }
            }
        }

        return result
    }

    fun findOptimalRight(origin: Float, newRight: Float): AssistantData {
        val result = AssistantData(origin, newRight)
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.canvasViewBox.getScaleX()

            canvasView.xAxis.eachAxisPixelList { index, value ->
                if (value >= newRight) {
                    val axisLineType =
                        canvasView.xAxis.getAxisLineType(canvasView.canvasViewBox, index, scale)
                    if (axisLineType > 0) {
                        val d = value - newRight
                        if (d < min) {
                            result.resultValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newRight).absoluteValue > adsorbThreshold) {
                    //不够接近推荐值
                    result.resultValue = null
                }
            }
        }

        return result
    }

    fun findOptimalTop(origin: Float, newTop: Float): AssistantData {
        val result = AssistantData(origin, newTop)
        if (canvasView.yAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.canvasViewBox.getScaleY()

            canvasView.yAxis.eachAxisPixelList { index, value ->
                if (value <= newTop) {
                    val axisLineType =
                        canvasView.yAxis.getAxisLineType(canvasView.canvasViewBox, index, scale)
                    if (axisLineType > 0) {
                        val d = newTop - value
                        if (d < min) {
                            result.resultValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newTop).absoluteValue > adsorbThreshold) {
                    //不够接近推荐值
                    result.resultValue = null
                }
            }
        }

        return result
    }

    fun findOptimalBottom(origin: Float, newBottom: Float): AssistantData {
        val result = AssistantData(origin, newBottom)
        if (canvasView.yAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.canvasViewBox.getScaleY()

            canvasView.yAxis.eachAxisPixelList { index, value ->
                if (value >= newBottom) {
                    val axisLineType =
                        canvasView.yAxis.getAxisLineType(canvasView.canvasViewBox, index, scale)
                    if (axisLineType > 0) {
                        val d = value - newBottom
                        if (d < min) {
                            result.resultValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newBottom).absoluteValue > adsorbThreshold) {
                    //不够接近推荐值
                    result.resultValue = null
                }
            }
        }

        return result
    }
}