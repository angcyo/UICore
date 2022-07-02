package com.angcyo.canvas.core.component

import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.library.L
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue

/**
 * 智能提示助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class SmartAssistant(val canvasView: CanvasDelegate) : BaseComponent(), ICanvasListener {

    //region ---field---

    /**智能提示的数据*/
    var lastXAssistant: SmartAssistantData? = null
    var lastYAssistant: SmartAssistantData? = null
    var lastRotateAssistant: SmartAssistantData? = null
    var lastWidthAssistant: SmartAssistantData? = null
    var lastHeightAssistant: SmartAssistantData? = null

    //---阈值

    /**吸附阈值, 当距离推荐线的距离小于等于此值时, 自动吸附
     * 值大小, 不容易吸附. 就会导致频繁计算推荐点
     * */
    var translateAdsorbThreshold: Float = 5f

    /**旋转吸附角度, 当和目标角度小于这个值时, 自动吸附到目标*/
    var rotateAdsorbThreshold: Float = 1f

    /**改变bounds时, 吸附大距离大小*/
    var boundsAdsorbThreshold: Float = 5f

    //---temp

    val rotateMatrix = Matrix()

    //---参考数据

    /**x值所有有效的参考值*/
    val xRefValueList = mutableListOf<SmartAssistantValueData>()

    /**y值所有有效的参考值*/
    val yRefValueList = mutableListOf<SmartAssistantValueData>()

    /**旋转角度所有有效的参考值*/
    val rotateRefValueList = mutableListOf<SmartAssistantValueData>()

    /**每隔15°推荐一次角度*/
    var rotateSmartAngle: Int = 15

    var enableXSmart: Boolean = true
    var enableYSmart: Boolean = true
    var enableRotateSmart: Boolean = true

    //endregion ---field---

    init {
        enable = true
        canvasView.addCanvasListener(this)
    }

    override fun onCanvasTouchEvent(canvasDelegate: CanvasDelegate, event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            //初始化智能提示数据
            if (enable) {
                initSmartAssistantData(canvasDelegate)
            }
        } else if (event.actionMasked == MotionEvent.ACTION_CANCEL || event.actionMasked == MotionEvent.ACTION_UP) {
            lastXAssistant = null
            lastYAssistant = null
            lastRotateAssistant = null
            lastWidthAssistant = null
            lastHeightAssistant = null
            canvasDelegate.refresh()
        }
        return super.onCanvasTouchEvent(canvasDelegate, event)
    }

    override fun onCanvasBoxMatrixChanged(matrix: Matrix, oldValue: Matrix) {
        super.onCanvasBoxMatrixChanged(matrix, oldValue)
    }

    /**初始化智能数据*/
    fun initSmartAssistantData(canvasDelegate: CanvasDelegate) {
        //x的参考点
        xRefValueList.clear()
        if (enableXSmart) {
            canvasDelegate.xAxis.eachAxisPixelList { index, axisPoint ->
                if (axisPoint.haveRule()) {
                    xRefValueList.add(SmartAssistantValueData(axisPoint.pixel))
                }
            }
            canvasDelegate.itemsRendererList.forEach {
                if (it.isVisible()) {
                    val bounds = it.getRotateBounds()
                    xRefValueList.add(SmartAssistantValueData(bounds.left, it))
                    xRefValueList.add(SmartAssistantValueData(bounds.right, it))
                }
            }
            canvasDelegate.limitRenderer.apply {
                if (!_limitPathBounds.isEmpty) {
                    xRefValueList.add(SmartAssistantValueData(_limitPathBounds.left, this))
                    xRefValueList.add(SmartAssistantValueData(_limitPathBounds.right, this))
                }
            }
        }

        //y的参考点
        yRefValueList.clear()
        if (enableYSmart) {
            canvasDelegate.yAxis.eachAxisPixelList { index, axisPoint ->
                if (axisPoint.haveRule()) {
                    yRefValueList.add(SmartAssistantValueData(axisPoint.pixel))
                }
            }
            canvasDelegate.itemsRendererList.forEach {
                if (it.isVisible()) {
                    val bounds = it.getRotateBounds()
                    yRefValueList.add(SmartAssistantValueData(bounds.top, it))
                    yRefValueList.add(SmartAssistantValueData(bounds.bottom, it))
                }
            }
            canvasDelegate.limitRenderer.apply {
                if (!_limitPathBounds.isEmpty) {
                    yRefValueList.add(SmartAssistantValueData(_limitPathBounds.top, this))
                    yRefValueList.add(SmartAssistantValueData(_limitPathBounds.bottom, this))
                }
            }
        }

        //旋转的参考点
        rotateRefValueList.clear()
        if (enableRotateSmart) {
            for (i in 0 until 360 step rotateSmartAngle) {
                rotateRefValueList.add(SmartAssistantValueData(i.toFloat(), null))
            }
            canvasDelegate.itemsRendererList.forEach {
                if (it.isVisible()) {
                    val rotate = it.rotate
                    rotateRefValueList.add(SmartAssistantValueData(rotate, it))
                }
            }
        }
    }

    //region ---smart---

    /**智能推荐算法平移
     * @return true 表示拦截此次手势操作*/
    fun smartTranslateItemBy(
        itemRenderer: BaseItemRenderer<*>,
        distanceX: Float = 0f,
        distanceY: Float = 0f
    ) {
        if (!enable || (distanceX == 0f && distanceX == 0f)) {
            canvasView.translateItemBy(itemRenderer, distanceX, distanceY)
            return
        }

        L.i("智能提示请求: dx:${distanceX} dy:${distanceY}")

        val renderRotateBounds = itemRenderer.getRenderRotateBounds()

        var dx = distanceX
        var dy = distanceY

        val left = renderRotateBounds.left
        val top = renderRotateBounds.top
        val right = renderRotateBounds.right
        val bottom = renderRotateBounds.bottom

        //吸附判断
        var adsorbDx: Float? = null
        var adsorbDy: Float? = null

        //震动反馈
        var feedback = false

        //x吸附
        lastXAssistant?.let {
            if (dx.absoluteValue <= translateAdsorbThreshold) {
                //需要吸附
                adsorbDx = 0f
                L.i("智能提示吸附X:${it.smartValue.refValue}")
            }

            /*if (it.forward) {
                val value = it.smartValue.refValue - (right + dx)
                if (value.absoluteValue <= translateAdsorbThreshold) {
                    //需要吸附
                    adsorbDx = 0f
                }
            } else {
                val value = it.smartValue.refValue - (left + dx)
                if (value.absoluteValue <= translateAdsorbThreshold) {
                    //需要吸附
                    adsorbDx = 0f
                }
            }*/
        }

        //y吸附
        lastYAssistant?.let {
            if (dy.absoluteValue <= translateAdsorbThreshold) {
                //需要吸附
                adsorbDy = 0f
                L.i("智能提示吸附Y:${it.smartValue.refValue}")
            }

            /*if (it.forward) {
                val value = it.smartValue.refValue - (bottom + dy)
                if (value.absoluteValue <= translateAdsorbThreshold) {
                    //需要吸附
                    adsorbDy = 0f
                }
            } else {
                val value = it.smartValue.refValue - (top + dy)
                if (value.absoluteValue <= translateAdsorbThreshold) {
                    //需要吸附
                    adsorbDy = 0f
                }
            }*/
        }

        val viewRect = canvasView.getCanvasViewBox()
            .mapCoordinateSystemRect(canvasView.viewBounds, _tempRect)

        if (adsorbDx == null) {
            //x未吸附
            val xRef = if (dx > 0f) {
                findSmartXValue(itemRenderer, right + dx, true)?.apply {
                    dx = smartValue.refValue - right
                }
            } else if (dx < 0f) {
                findSmartXValue(itemRenderer, left + dx, false)?.apply {
                    dx = smartValue.refValue - left
                }
            } else {
                //未移动
                null
            }

            if (xRef != null) {
                //找到的推荐点
                lastXAssistant = xRef
                feedback = true
                xRef.drawRect = RectF(
                    xRef.smartValue.refValue,
                    viewRect.top,
                    xRef.smartValue.refValue,
                    viewRect.bottom
                )
            }
        }

        if (adsorbDy == null) {
            //y未吸附
            val yRef = if (dy > 0f) {
                findSmartYValue(itemRenderer, bottom + dy, true)?.apply {
                    dy = smartValue.refValue - bottom
                }
            } else if (dy < 0f) {
                findSmartYValue(itemRenderer, top + dy, false)?.apply {
                    dy = smartValue.refValue - top
                }
            } else {
                //未移动
                null
            }

            if (yRef != null) {
                //找到的推荐点
                lastYAssistant = yRef
                feedback = true
                yRef.drawRect = RectF(
                    viewRect.left,
                    yRef.smartValue.refValue,
                    viewRect.right,
                    yRef.smartValue.refValue
                )
            }
        }

        dx = adsorbDx ?: dx
        dy = adsorbDy ?: dy

        if (feedback) {
            //震动反馈
            canvasView.longFeedback()
            L.i("智能提示: dx:${distanceX} dy:${distanceY} -> ndx:${dx} ndy:${dy}")
        }

        canvasView.translateItemBy(itemRenderer, dx, dy)
    }

    /**智能旋转算法
     * @return true 表示拦截此次手势操作*/
    fun smartRotateBy(
        itemRenderer: BaseItemRenderer<*>,
        angle: Float,
        rotateFlag: Int
    ) {
        if (!enable) {
            canvasView.rotateItemBy(itemRenderer, angle, rotateFlag)
            return
        }

        canvasView.rotateItemBy(itemRenderer, angle, rotateFlag)

        /*val oldRotate = itemRenderer.rotate
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
            canvasView.rotateItemBy(itemRenderer, result, rotateFlag)
        }
        return lastRotateAssistant*/
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
    ) {
        if (!enable) {
            canvasView.changeItemBounds(
                itemRenderer,
                width,
                height,
                adjustType
            )
            return
        }

        canvasView.changeItemBounds(
            itemRenderer,
            width,
            height,
            adjustType
        )

        /* var newWidth = width
         var newHeight = height

         var feedbackWidth = false
         var feedbackHeight = false
         var result: SmartAssistantData? = null

         val viewRect = canvasView.getCanvasViewBox()
             .mapCoordinateSystemRect(canvasView.viewBounds, _tempRect)

         val widthAssistant = findOptimalWidth(itemRenderer, dx)
         if (widthAssistant.isChanged()) {
             newWidth = widthAssistant.resultValue!!

             widthAssistantRect = RectF(
                 widthAssistant.smartValue!!,
                 viewRect.top,
                 widthAssistant.smartValue!!,
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
                 heightAssistant.smartValue!!,
                 viewRect.right,
                 heightAssistant.smartValue!!
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

         if (!itemRenderer.isLineShape() && equalRatio) {
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

         *//*if (lastWidthAssistant?.resultValue != newWidth) {
            widthAssistantRect = null
            feedbackWidth = false
        }

        if (lastHeightAssistant?.resultValue != newHeight) {
            heightAssistantRect = null
            feedbackHeight = false
        }*//*

        if (feedbackWidth || feedbackHeight) {
            //找到了 震动反馈
            canvasView.longFeedback()

            L.w("智能提示: width:${width}->$newWidth height:${height}->$newHeight")
        }

        resetSmartLine()
        canvasView.changeItemBounds(
            itemRenderer,
            newWidth,
            newHeight,
            adjustType
        )
        return result*/
    }

    //endregion ---smart---

    //region ---find---

    /**
     * 查找[x]附近最优的推荐点, [forward]正向查找or负向查找
     * */
    fun findSmartXValue(
        itemRenderer: IRenderer?,
        x: Float,
        forward: Boolean
    ): SmartAssistantData? {
        var smartValue: SmartAssistantValueData? = null

        //差值越小越好
        var diffValue = Float.MAX_VALUE

        xRefValueList.forEach {
            if (it.refRenderer != null && it.refRenderer == itemRenderer) {
                //自身
            } else {
                val v = it.refValue - x
                if ((forward && v >= 0) || (!forward && v <= 0)) {
                    val vAbs = v.absoluteValue
                    if (vAbs <= diffValue) {
                        diffValue = vAbs
                        smartValue = it
                    }
                }
            }
        }

        if (smartValue != null) {
            return SmartAssistantData(x, smartValue!!, forward)
        }
        return null
    }

    /**
     * 查找[y]附近最优的推荐点, [forward]正向查找or负向查找
     * */
    fun findSmartYValue(
        itemRenderer: IRenderer?,
        y: Float,
        forward: Boolean
    ): SmartAssistantData? {
        var smartValue: SmartAssistantValueData? = null

        //差值越小越好
        var diffValue = Float.MAX_VALUE

        yRefValueList.forEach {
            if (it.refRenderer != null && it.refRenderer == itemRenderer) {
                //自身
            } else {
                val v = it.refValue - y
                if ((forward && v >= 0) || (!forward && v <= 0)) {
                    val vAbs = v.absoluteValue
                    if (vAbs <= diffValue) {
                        diffValue = vAbs
                        smartValue = it
                    }
                }
            }
        }

        if (smartValue != null) {
            return SmartAssistantData(y, smartValue!!, forward)
        }
        return null
    }

    /**
     * 查找[rotate]附近最优的推荐点, [forward]正向查找or负向查找
     * */
    fun findSmartRotateValue(
        itemRenderer: IRenderer?,
        rotate: Float,
        forward: Boolean
    ): SmartAssistantData? {
        var smartValue: SmartAssistantValueData? = null

        //差值越小越好
        var diffValue = Float.MAX_VALUE

        rotateRefValueList.forEach {
            if (it.refRenderer != null && it.refRenderer == itemRenderer) {
                //自身
            } else {
                val v = it.refValue - rotate
                if ((forward && v >= 0) || (!forward && v <= 0)) {
                    val vAbs = v.absoluteValue
                    if (vAbs <= diffValue) {
                        diffValue = vAbs
                        smartValue = it
                    }
                }
            }
        }

        if (smartValue != null) {
            return SmartAssistantData(rotate, smartValue!!, forward)
        }
        return null
    }
/*

    */
    /**查找最优的旋转角度
     * [angle] 当前需要旋转的角度
     * @return 返回最优需要旋转的角度*//*

    fun findOptimalAngle(itemRenderer: BaseItemRenderer<*>, angle: Float): SmartAssistantData {
        val oldRotate = itemRenderer.rotate
        val newValue = itemRenderer.rotate + angle
        val result = SmartAssistantData(oldRotate, newValue)

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

    */
    /**查找最贴近的横坐标值
     * [origin] 原始的横坐标
     * [newValue] 偏移后的横坐标
     * [forward] true, 表示向正方向查找, 否则向负方向查找
     * *//*

    fun findOptimalX(
        originItem: IItemRenderer<*>,
        origin: Float,
        newValue: Float,
        forward: Boolean
    ): SmartAssistantData {
        val result = SmartAssistantData(origin, newValue)
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.getCanvasViewBox().getScaleX()

            canvasView.xAxis.eachAxisPixelList { index, axisPoint ->
                if ((forward && axisPoint.pixel >= origin) || (!forward && axisPoint.pixel <= origin)) {
                    val axisLineType = canvasView.xAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = (newValue - axisPoint.pixel).absoluteValue
                        if (d < min) {
                            result.resultValue = axisPoint.pixel
                            result.smartValue = axisPoint.pixel
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
                        result.smartValue = value
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

    */
    /**查找最贴近的纵坐标值
     * [origin] 原始的纵坐标
     * [newValue] 偏移后的纵坐标
     * [forward] true, 表示向正方向查找, 否则向负方向查找
     * *//*

    fun findOptimalY(
        originItem: IItemRenderer<*>,
        origin: Float,
        newValue: Float,
        forward: Boolean
    ): SmartAssistantData {
        val result = SmartAssistantData(origin, newValue)
        if (canvasView.yAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.getCanvasViewBox().getScaleY()

            canvasView.yAxis.eachAxisPixelList { index, axisPoint ->
                if ((forward && axisPoint.pixel >= origin) || (!forward && axisPoint.pixel <= origin)) {
                    val axisLineType = canvasView.yAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = (newValue - axisPoint.pixel).absoluteValue
                        if (d < min) {
                            result.resultValue = axisPoint.pixel
                            result.smartValue = axisPoint.pixel
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
                        result.smartValue = value
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

    */
    /**查找最优的宽度*//*

    fun findOptimalWidth(
        itemRenderer: BaseItemRenderer<*>,
        distanceX: Float = 0f
    ): SmartAssistantData {
        val renderBounds = itemRenderer.getRenderBounds()
        val renderRotateBounds = itemRenderer.getRenderRotateBounds()
        val result = SmartAssistantData(renderBounds.width(), renderBounds.width() + distanceX)

        val assistant: SmartAssistantData = if (renderBounds.isFlipHorizontal) {
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
                result.smartValue = resultRight
            }
        }

        return result
    }

    */
    /**查找最优的高度*//*

    fun findOptimalHeight(
        itemRenderer: BaseItemRenderer<*>,
        distanceY: Float = 0f
    ): SmartAssistantData {
        val renderBounds = itemRenderer.getRenderBounds()
        val renderRotateBounds = itemRenderer.getRenderRotateBounds()
        val result = SmartAssistantData(renderBounds.height(), renderBounds.height() + distanceY)

        val assistant: SmartAssistantData = if (renderBounds.isFlipVertical) {
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
                result.smartValue = resultBottom
            }
        }

        return result
    }

    */
    /**查找当前坐标, 最贴近的提示目标*//*

    fun findOptimalLeft(origin: Float, newLeft: Float): SmartAssistantData {
        val result = SmartAssistantData(origin, newLeft)
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.getCanvasViewBox().getScaleX()

            canvasView.xAxis.eachAxisPixelList { index, axisPoint ->
                if (axisPoint.pixel <= origin) {
                    val axisLineType = canvasView.xAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = newLeft - axisPoint.pixel
                        if (d < min) {
                            result.resultValue = axisPoint.pixel
                            result.smartValue = axisPoint.pixel
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

    fun findOptimalRight(origin: Float, newRight: Float): SmartAssistantData {
        val result = SmartAssistantData(origin, newRight)
        if (canvasView.xAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.getCanvasViewBox().getScaleX()

            canvasView.xAxis.eachAxisPixelList { index, axisPoint ->
                if (axisPoint.pixel >= newRight) {
                    val axisLineType = canvasView.xAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = axisPoint.pixel - newRight
                        if (d < min) {
                            result.resultValue = axisPoint.pixel
                            result.smartValue = axisPoint.pixel
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

    fun findOptimalTop(origin: Float, newTop: Float): SmartAssistantData {
        val result = SmartAssistantData(origin, newTop)
        if (canvasView.yAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.getCanvasViewBox().getScaleY()

            canvasView.yAxis.eachAxisPixelList { index, axisPoint ->
                if (axisPoint.pixel <= newTop) {
                    val axisLineType = canvasView.yAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = newTop - axisPoint.pixel
                        if (d < min) {
                            result.resultValue = axisPoint.pixel
                            result.smartValue = axisPoint.pixel
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

    fun findOptimalBottom(origin: Float, newBottom: Float): SmartAssistantData {
        val result = SmartAssistantData(origin, newBottom)
        if (canvasView.yAxis.enable) {
            var min = Float.MAX_VALUE
            val scale = canvasView.getCanvasViewBox().getScaleY()

            canvasView.yAxis.eachAxisPixelList { index, axisPoint ->
                if (axisPoint.pixel >= newBottom) {
                    val axisLineType = canvasView.yAxis.getAxisLineType(
                        canvasView.getCanvasViewBox(),
                        index,
                        scale
                    )
                    if (axisLineType > 0) {
                        val d = axisPoint.pixel - newBottom
                        if (d < min) {
                            result.resultValue = axisPoint.pixel
                            result.smartValue = axisPoint.pixel
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
*/

    //endregion ---find---
}