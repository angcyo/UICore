package com.angcyo.canvas.core.component

import android.graphics.RectF
import android.view.HapticFeedbackConstants
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import kotlin.math.tan

/**
 * 智能提示助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class SmartAssistant(val canvasView: CanvasView) : BaseComponent() {

    /**提示线的坐标*/
    val smartLineList = mutableListOf<RectF>()

    init {
        enable = false
    }

    fun smartTranslateItemBy(
        itemRenderer: BaseItemRenderer<*>,
        distanceX: Float = 0f,
        distanceY: Float = 0f
    ) {
        if (enable) {
            /*val renderBounds = itemRenderer.getRenderBounds()
            val left = renderBounds.left
            val top = renderBounds.top
            val newLeft = findOptimalLeft(left + distanceX)
            val newTop = findOptimalTop(top + distanceY)
            smartLineList.clear()

            val leftChanged = newLeft.toInt() != left.toInt()
            val topChanged = newTop.toInt() != top.toInt()
            if (leftChanged || topChanged) {
                if (leftChanged) {
                    smartLineList.add(
                        RectF(
                            newLeft,
                            0f,
                            newLeft,
                            canvasView.canvasViewBox.getContentBottom()
                        )
                    )
                }
                if (topChanged) {
                    smartLineList.add(
                        RectF(
                            0f,
                            newTop,
                            canvasView.canvasViewBox.getContentRight(),
                            newTop
                        )
                    )
                }
                //震动反馈
                canvasView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            canvasView.translateItemBy(itemRenderer, newLeft - left, newTop - top)*/
            canvasView.translateItemBy(itemRenderer, distanceX, distanceY)
        } else {
            canvasView.translateItemBy(itemRenderer, distanceX, distanceY)
        }
    }

    /**查找当前坐标, 最贴近的提示目标*/
    fun findOptimalLeft(left: Float): Float {
        var result = left
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scaleX = canvasView.canvasViewBox.getScaleX()
            canvasView.xAxis.plusList.forEachIndexed { index, value ->
                if (left > value) {
                    val axisLineType = canvasView.xAxis.getAxisLineType(index, scaleX)
                    if (axisLineType > 0) {
                        val d = left - value
                        if (d < min) {
                            result = value
                            min = d
                        }
                    }
                }
            }

            canvasView.xAxis.minusList.forEachIndexed { index, value ->
                if (left > value) {
                    val axisLineType = canvasView.xAxis.getAxisLineType(index, scaleX)
                    if (axisLineType > 0) {
                        val d = left - value
                        if (d < min) {
                            result = value
                            min = d
                        }
                    }
                }
            }
        }

        return result
    }

    fun findOptimalTop(top: Float): Float {
        var result = top
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scaleX = canvasView.canvasViewBox.getScaleX()
            canvasView.yAxis.plusList.forEachIndexed { index, value ->
                if (top > value) {
                    val axisLineType = canvasView.yAxis.getAxisLineType(index, scaleX)
                    if (axisLineType > 0) {
                        val d = top - value
                        if (d < min) {
                            result = value
                            min = d
                        }
                    }
                }
            }

            canvasView.yAxis.minusList.forEachIndexed { index, value ->
                if (top > value) {
                    val axisLineType = canvasView.yAxis.getAxisLineType(index, scaleX)
                    if (axisLineType > 0) {
                        val d = top - value
                        if (d < min) {
                            result = value
                            min = d
                        }
                    }
                }
            }
        }

        return result
    }

    /**查找最优的旋转角度
     * [angle] 当前需要旋转的角度
     * @return 返回最优需要旋转的角度*/
    fun findOptimalAngle(itemRenderer: BaseItemRenderer<*>, angle: Float): Float {
        if (!enable) {
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
}