package com.angcyo.canvas.core.component

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.library.L
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue

/**
 * 智能提示助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class SmartAssistant(val canvasView: CanvasDelegate) : BaseComponent(), ICanvasListener {

    /**提示线的坐标*/
    val smartLineList = mutableListOf<RectF>()

    var lastXAssistant: AssistantData? = null
    var lastYAssistant: AssistantData? = null
    var lastRotateAssistant: AssistantData? = null
    var lastWidthAssistant: AssistantData? = null
    var lastHeightAssistant: AssistantData? = null

    /**吸附阈值, 当距离推荐线的距离小于等于此值时, 自动吸附*/
    var translateAdsorbThreshold: Float = 2f

    /**旋转吸附角度, 当和目标角度小于这个值时, 自动吸附到目标*/
    var rotateAdsorbThreshold: Float = 1f

    /**改变bounds时, 吸附大距离大小*/
    var boundsAdsorbThreshold: Float = 5f

    /**每隔15°推荐一次角度*/
    var rotateSmartAngle: Int = 15

    var xAssistantRect: RectF? = null
    var yAssistantRect: RectF? = null
    var rotateAssistantRect: RectF? = null
    var widthAssistantRect: RectF? = null
    var heightAssistantRect: RectF? = null

    val rotateMatrix = Matrix()

    init {
        enable = true
        canvasView.addCanvasListener(this)
    }

    override fun onCanvasTouchEvent(canvasView: CanvasDelegate, event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_CANCEL || event.actionMasked == MotionEvent.ACTION_UP) {
            smartLineList.clear()
            canvasView.refresh()
            xAssistantRect = null
            yAssistantRect = null
            rotateAssistantRect = null
            widthAssistantRect = null
            heightAssistantRect = null
        }
        return super.onCanvasTouchEvent(canvasView, event)
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
        widthAssistantRect?.let {
            smartLineList.add(it)
        }
        heightAssistantRect?.let {
            smartLineList.add(it)
        }
    }

    /**智能推荐算法平移
     * @return true 表示拦截此次手势操作*/
    fun smartTranslateItemBy(
        itemRenderer: BaseItemRenderer<*>,
        distanceX: Float = 0f,
        distanceY: Float = 0f
    ): AssistantData? {
        if (!enable) {
            canvasView.translateItemBy(itemRenderer, distanceX, distanceY)
            return null
        }

        val renderRotateBounds = itemRenderer.getRenderRotateBounds()

        val left = renderRotateBounds.left
        val top = renderRotateBounds.top

        var dx = distanceX
        var dy = distanceY

        var newLeft = left + distanceX
        var newTop = top + distanceY

        val viewRect = canvasView.getCanvasViewBox()
            .mapCoordinateSystemRect(canvasView.viewBounds, _tempRect)

        var feedback = false
        var result: AssistantData? = null

        if (distanceX != 0f) {
            val assistant = findOptimalX(itemRenderer, left, newLeft, distanceX > 0)
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
                result = lastXAssistant
            } else {
                xAssistantRect = null
            }
        }

        if (distanceY != 0f) {
            val topAssistant = findOptimalY(itemRenderer, top, newTop, distanceY > 0)
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
                result = lastYAssistant
            } else {
                yAssistantRect = null
            }
        }

        if (feedback) {
            //震动反馈
            canvasView.longFeedback()

            L.w("智能提示: dx:${distanceX} dy:${distanceY} left:${left} top:${top} newLeft:${newLeft} newTop:${newTop}")
        }

        resetSmartLine()
        canvasView.translateItemBy(itemRenderer, dx, dy)
        return result
    }

    /**智能旋转算法
     * @return true 表示拦截此次手势操作*/
    fun smartRotateBy(itemRenderer: BaseItemRenderer<*>, angle: Float): AssistantData? {
        if (!enable) {
            canvasView.rotateItemBy(itemRenderer, angle)
            return null
        }

        val oldRotate = itemRenderer.rotate
        var newRotate = itemRenderer.rotate + angle
        var feedback = false

        val assistant = findOptimalAngle(itemRenderer, angle)
        if (assistant.isChanged()) {
            newRotate = assistant.resultValue!!

            val viewRect = canvasView.getCanvasViewBox()
                .mapCoordinateSystemRect(canvasView.viewBounds, _tempRect)
            val renderBounds = itemRenderer.getRenderBounds()
            var left = viewRect.left
            var right = viewRect.right
            var top = renderBounds.centerY()
            var bottom = top

            rotateMatrix.reset()
            rotateMatrix.postRotate(newRotate, renderBounds.centerX(), renderBounds.centerY())

            rotateMatrix.mapPoint(left, top).apply {
                left = x
                top = y
            }

            rotateMatrix.mapPoint(right, bottom).apply {
                right = x
                bottom = y
            }
            rotateAssistantRect = RectF(left, top, right, bottom)

            feedback = feedback || assistant.isChanged(lastRotateAssistant)
            lastRotateAssistant = assistant
        }

        if (feedback) {
            //找到了 震动反馈
            canvasView.longFeedback()

            L.w("智能提示: angle:${angle} from:${oldRotate} to:${newRotate}")
        }
        val result = newRotate - oldRotate
        resetSmartLine()
        if (result != 0f) {
            canvasView.rotateItemBy(itemRenderer, result)
        }
        return lastRotateAssistant
    }

    /**智能算法改变矩形的宽高*/
    fun smartChangeBounds(
        itemRenderer: BaseItemRenderer<*>,
        equalRatio: Boolean, //等比缩放
        width: Float,
        height: Float,
        dx: Float,
        dy: Float,
        adjustType: Int = ADJUST_TYPE_LT
    ): AssistantData? {
        if (!enable) {
            canvasView.changeItemBounds(
                itemRenderer,
                width,
                height,
                adjustType
            )
            return null
        }

        var newWidth = width
        var newHeight = height

        var feedbackWidth = false
        var feedbackHeight = false
        var result: AssistantData? = null

        val viewRect = canvasView.getCanvasViewBox()
            .mapCoordinateSystemRect(canvasView.viewBounds, _tempRect)

        val widthAssistant = findOptimalWidth(itemRenderer, dx)
        if (widthAssistant.isChanged()) {
            newWidth = widthAssistant.resultValue!!

            widthAssistantRect = RectF(
                widthAssistant.refValue!!,
                viewRect.top,
                widthAssistant.refValue!!,
                viewRect.bottom
            )

            feedbackWidth = feedbackWidth || widthAssistant.isChanged(lastWidthAssistant)
            lastWidthAssistant = widthAssistant
            result = widthAssistant
        } else {
            lastWidthAssistant = null
        }

        val heightAssistant = findOptimalHeight(itemRenderer, dy)
        if (heightAssistant.isChanged()) {
            newHeight = heightAssistant.resultValue!!

            heightAssistantRect = RectF(
                viewRect.left,
                heightAssistant.refValue!!,
                viewRect.right,
                heightAssistant.refValue!!
            )

            feedbackHeight = feedbackHeight || heightAssistant.isChanged(lastHeightAssistant)
            lastHeightAssistant = heightAssistant
            result = heightAssistant
        } else {
            lastHeightAssistant = null
        }

        if ((newWidth - width).absoluteValue <= boundsAdsorbThreshold) {
            newWidth = width
        }
        if ((newHeight - height).absoluteValue <= boundsAdsorbThreshold) {
            newHeight = height
        }

        if (equalRatio) {
            //等比调整
            val bounds = itemRenderer.getBounds()
            val rectWidth = bounds.width()
            val rectHeight = bounds.height()
            if (rectWidth > rectHeight) {
                newHeight = rectHeight * newWidth / rectWidth
                feedbackHeight = false
                lastHeightAssistant = null
                heightAssistantRect = null
            } else {
                newWidth = rectWidth * newHeight / rectHeight
                feedbackWidth = false
                lastWidthAssistant = null
                widthAssistantRect = null
            }
        }

        /*if (lastWidthAssistant?.resultValue != newWidth) {
            widthAssistantRect = null
            feedbackWidth = false
        }

        if (lastHeightAssistant?.resultValue != newHeight) {
            heightAssistantRect = null
            feedbackHeight = false
        }*/

        if (feedbackWidth || feedbackHeight) {
            //找到了 震动反馈
            canvasView.longFeedback()

            L.w("智能提示: width:${width} $newWidth height:${height} $newHeight")
        }

        resetSmartLine()
        canvasView.changeItemBounds(
            itemRenderer,
            newWidth,
            newHeight,
            adjustType
        )
        return result
    }

    /**查找最优的旋转角度
     * [angle] 当前需要旋转的角度
     * @return 返回最优需要旋转的角度*/
    fun findOptimalAngle(itemRenderer: BaseItemRenderer<*>, angle: Float): AssistantData {
        val oldRotate = itemRenderer.rotate
        val newValue = itemRenderer.rotate + angle
        val result = AssistantData(oldRotate, newValue)

        var min = Float.MAX_VALUE
        if (angle > 0) {
            //顺时针旋转
            for (value in 0 until 360 step rotateSmartAngle) {
                if (value >= oldRotate) {
                    val d = (newValue - value).absoluteValue
                    if (d < min) {
                        result.resultValue = value.toFloat()
                        min = d
                    }
                }
            }
        } else if (angle < 0) {
            //逆时针旋转
            for (value in 0 downTo -360 step rotateSmartAngle) {
                if (value <= oldRotate) {
                    val d = (newValue - value).absoluteValue
                    if (d < min) {
                        result.resultValue = value.toFloat()
                        min = d
                    }
                }
            }
        }

        result.resultValue?.let {
            if ((it - newValue).absoluteValue > rotateAdsorbThreshold) {
                //不够接近推荐值
                result.resultValue = null
            }
        }

        return result
    }

    /**查找最贴近的横坐标值
     * [origin] 原始的横坐标
     * [newValue] 偏移后的横坐标
     * [forward] true, 表示向正方向查找, 否则向负方向查找
     * */
    fun findOptimalX(
        originItem: IItemRenderer<*>,
        origin: Float,
        newValue: Float,
        forward: Boolean
    ): AssistantData {
        val result = AssistantData(origin, newValue)
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.getCanvasViewBox().getScaleX()

            canvasView.xAxis.eachAxisPixelList { index, value ->
                if ((forward && value >= origin) || (!forward && value <= origin)) {
                    val axisLineType = canvasView.xAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = (newValue - value).absoluteValue
                        if (d < min) {
                            result.resultValue = value
                            result.refValue = value
                            min = d
                        }
                    }
                }
            }

            canvasView.itemsRendererList.forEach {
                if (it != originItem) {
                    val bounds = it.getRenderRotateBounds()
                    val value = bounds.flipLeft
                    val d = (newValue - value).absoluteValue
                    if (d < min) {
                        result.resultValue = value
                        result.refValue = value
                        result.refRenderer = it
                        min = d
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newValue).absoluteValue > translateAdsorbThreshold) {
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
    fun findOptimalY(
        originItem: IItemRenderer<*>,
        origin: Float,
        newValue: Float,
        forward: Boolean
    ): AssistantData {
        val result = AssistantData(origin, newValue)
        if (canvasView.yAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.getCanvasViewBox().getScaleY()

            canvasView.yAxis.eachAxisPixelList { index, value ->
                if ((forward && value >= origin) || (!forward && value <= origin)) {
                    val axisLineType = canvasView.yAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = (newValue - value).absoluteValue
                        if (d < min) {
                            result.resultValue = value
                            result.refValue = value
                            min = d
                        }
                    }
                }
            }

            canvasView.itemsRendererList.forEach {
                if (it != originItem) {
                    val bounds = it.getRenderRotateBounds()
                    val value = bounds.flipTop
                    val d = (newValue - value).absoluteValue
                    if (d < min) {
                        result.resultValue = value
                        result.refValue = value
                        result.refRenderer = it
                        min = d
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newValue).absoluteValue > translateAdsorbThreshold) {
                    //不够接近推荐值
                    result.resultValue = null
                }
            }
        }

        return result
    }

    val rectScaleAnchorPoint = PointF()

    /**查找最优的宽度*/
    fun findOptimalWidth(itemRenderer: BaseItemRenderer<*>, distanceX: Float = 0f): AssistantData {
        val renderBounds = itemRenderer.getRenderBounds()
        val renderRotateBounds = itemRenderer.getRenderRotateBounds()
        val result = AssistantData(renderBounds.width(), renderBounds.width() + distanceX)

        val assistant: AssistantData = if (renderBounds.isFlipHorizontal) {
            val left = renderRotateBounds.left
            findOptimalLeft(left, left + distanceX)
        } else {
            val right = renderRotateBounds.right
            findOptimalRight(right, right + distanceX)
        }

        assistant.resultValue?.let { resultRight ->
            rectScaleAnchorPoint.set(renderBounds.left, renderBounds.top)
            itemRenderer.mapRotatePoint(rectScaleAnchorPoint, rectScaleAnchorPoint)

            _tempPoint.set(renderBounds.right, renderBounds.top)
            itemRenderer.mapRotatePoint(_tempPoint, _tempPoint)
            val rotateX = _tempPoint.x
            val rotateY = _tempPoint.y

            val x2 = resultRight
            val y2 = rotateY

            ScaleControlPoint.calcRotateBeforeDistance(
                rectScaleAnchorPoint.x,
                rectScaleAnchorPoint.y,
                x2,
                y2,
                renderRotateBounds.centerX(),
                renderRotateBounds.centerY(),
                itemRenderer.rotate
            ).apply {
                result.resultValue = this[0]
                result.refValue = resultRight
            }
        }

        return result
    }

    /**查找最优的高度*/
    fun findOptimalHeight(itemRenderer: BaseItemRenderer<*>, distanceY: Float = 0f): AssistantData {
        val renderBounds = itemRenderer.getRenderBounds()
        val renderRotateBounds = itemRenderer.getRenderRotateBounds()
        val result = AssistantData(renderBounds.height(), renderBounds.height() + distanceY)

        val assistant: AssistantData = if (renderBounds.isFlipVertical) {
            val top = renderRotateBounds.top
            findOptimalTop(top, top + distanceY)
        } else {
            val bottom = renderRotateBounds.bottom
            findOptimalBottom(bottom, bottom + distanceY)
        }

        assistant.resultValue?.let { resultBottom ->
            rectScaleAnchorPoint.set(renderBounds.left, renderBounds.top)
            itemRenderer.mapRotatePoint(rectScaleAnchorPoint, rectScaleAnchorPoint)

            _tempPoint.set(renderBounds.right, renderBounds.bottom)
            itemRenderer.mapRotatePoint(_tempPoint, _tempPoint)
            val rotateX = _tempPoint.x
            val rotateY = _tempPoint.y

            val x2 = rotateX
            val y2 = resultBottom

            ScaleControlPoint.calcRotateBeforeDistance(
                rectScaleAnchorPoint.x,
                rectScaleAnchorPoint.y,
                x2,
                y2,
                renderRotateBounds.centerX(),
                renderRotateBounds.centerY(),
                itemRenderer.rotate
            ).apply {
                result.resultValue = this[1]
                result.refValue = resultBottom
            }
        }

        return result
    }

    /**查找当前坐标, 最贴近的提示目标*/
    fun findOptimalLeft(origin: Float, newLeft: Float): AssistantData {
        val result = AssistantData(origin, newLeft)
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.getCanvasViewBox().getScaleX()

            canvasView.xAxis.eachAxisPixelList { index, value ->
                if (value <= origin) {
                    val axisLineType = canvasView.xAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = newLeft - value
                        if (d < min) {
                            result.resultValue = value
                            result.refValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newLeft).absoluteValue > translateAdsorbThreshold) {
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
            val scale = canvasView.getCanvasViewBox().getScaleX()

            canvasView.xAxis.eachAxisPixelList { index, value ->
                if (value >= newRight) {
                    val axisLineType = canvasView.xAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = value - newRight
                        if (d < min) {
                            result.resultValue = value
                            result.refValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newRight).absoluteValue > translateAdsorbThreshold) {
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
            val scale = canvasView.getCanvasViewBox().getScaleY()

            canvasView.yAxis.eachAxisPixelList { index, value ->
                if (value <= newTop) {
                    val axisLineType = canvasView.yAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = newTop - value
                        if (d < min) {
                            result.resultValue = value
                            result.refValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newTop).absoluteValue > translateAdsorbThreshold) {
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
            val scale = canvasView.getCanvasViewBox().getScaleY()

            canvasView.yAxis.eachAxisPixelList { index, value ->
                if (value >= newBottom) {
                    val axisLineType = canvasView.yAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = value - newBottom
                        if (d < min) {
                            result.resultValue = value
                            result.refValue = value
                            min = d
                        }
                    }
                }
            }

            result.resultValue?.let {
                if ((it - newBottom).absoluteValue > translateAdsorbThreshold) {
                    //不够接近推荐值
                    result.resultValue = null
                }
            }
        }

        return result
    }
}