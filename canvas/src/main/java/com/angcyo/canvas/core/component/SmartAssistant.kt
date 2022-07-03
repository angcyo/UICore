package com.angcyo.canvas.core.component

import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.library.L
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * 智能提示助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class SmartAssistant(val canvasDelegate: CanvasDelegate) : BaseComponent(), ICanvasListener {

    //region ---field---

    /**智能提示的数据*/
    var lastXAssistant: SmartAssistantData? = null
    var lastYAssistant: SmartAssistantData? = null
    var lastRotateAssistant: SmartAssistantData? = null
    var lastWidthAssistant: SmartAssistantData? = null
    var lastHeightAssistant: SmartAssistantData? = null

    //---阈值

    /**吸附阈值, 当距离推荐线的距离小于等于此值时, 自动吸附
     * 值越小, 不容易吸附. 就会导致频繁计算推荐点.
     *
     * 当距离推荐值, 小于等于这个值, 就选取这个推荐值
     * */
    var translateAdsorbThreshold: Float = 10f

    /**旋转吸附角度, 当和目标角度小于这个值时, 自动吸附到目标*/
    var rotateAdsorbThreshold: Float = 5f

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
        canvasDelegate.addCanvasListener(this)
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
                if (axisPoint.isMasterRule()) {
                    xRefValueList.add(
                        SmartAssistantValueData(
                            axisPoint.pixel - canvasDelegate.getCanvasViewBox()
                                .getCoordinateSystemX()
                        )
                    )
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
                if (axisPoint.isMasterRule()) {
                    yRefValueList.add(
                        SmartAssistantValueData(
                            axisPoint.pixel - canvasDelegate.getCanvasViewBox()
                                .getCoordinateSystemY()
                        )
                    )
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
     * @return true 表示消耗了此次手势操作, x, y*/
    fun smartTranslateItemBy(
        itemRenderer: BaseItemRenderer<*>,
        distanceX: Float = 0f,
        distanceY: Float = 0f
    ): BooleanArray {
        if (!enable || (distanceX == 0f && distanceY == 0f)) {
            canvasDelegate.translateItemBy(itemRenderer, distanceX, distanceY)
            return booleanArrayOf(true, true)
        }

        L.i("智能平移请求: dx:${distanceX} dy:${distanceY}")

        val rotateBounds = itemRenderer.getRotateBounds()

        var dx = distanceX
        var dy = distanceY

        val left = rotateBounds.left
        val top = rotateBounds.top
        val right = rotateBounds.right
        val bottom = rotateBounds.bottom

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
                L.d("智能提示吸附X:${it.smartValue.refValue}")
            } else {
                lastXAssistant = null
            }
        }

        //y吸附
        lastYAssistant?.let {
            if (dy.absoluteValue <= translateAdsorbThreshold) {
                //需要吸附
                adsorbDy = 0f
                L.d("智能提示吸附Y:${it.smartValue.refValue}")
            } else {
                lastYAssistant = null
            }
        }

        if (adsorbDx == null) {
            //x未吸附
            val xRef = findSmartXValue(itemRenderer, left, right, dx)?.apply {
                dx = smartValue.refValue - fromValue
            }

            if (xRef != null) {
                //找到的推荐点
                L.i("找到推荐点:fromX:->${xRef.fromValue} ->${xRef.smartValue.refValue}")
                lastXAssistant = xRef
                feedback = true
            }
        }

        if (adsorbDy == null) {
            //y未吸附
            val yRef = findSmartYValue(itemRenderer, top, bottom, dy)?.apply {
                dy = smartValue.refValue - fromValue
            }

            if (yRef != null) {
                //找到的推荐点
                L.i("找到推荐点:fromY:->${yRef.fromValue} ->${yRef.smartValue.refValue}")
                lastYAssistant = yRef
                feedback = true
            }
        }

        dx = adsorbDx ?: dx
        dy = adsorbDy ?: dy

        if (feedback) {
            //震动反馈
            canvasDelegate.longFeedback()
            L.i("智能提示: dx:${distanceX} dy:${distanceY} -> ndx:${dx} ndy:${dy}")
        }

        canvasDelegate.translateItemBy(itemRenderer, dx, dy)
        return booleanArrayOf(dx != 0f, dy != 0f)
    }

    /**智能旋转算法
     * @return true 表示消耗了此次手势操作*/
    fun smartRotateBy(
        itemRenderer: BaseItemRenderer<*>,
        angle: Float,
        rotateFlag: Int
    ): Boolean {
        if (!enable) {
            canvasDelegate.rotateItemBy(itemRenderer, angle, rotateFlag)
            return true
        }

        val rotate = itemRenderer.rotate
        L.i("智能旋转请求: from:$rotate dr:${angle}")

        var result = angle

        //吸附判断
        var adsorbAngle: Float? = null

        //震动反馈
        var feedback = false

        lastRotateAssistant?.let {
            if (angle.absoluteValue <= rotateAdsorbThreshold) {
                //需要吸附
                adsorbAngle = 0f
                L.d("智能提示吸附Rotate:${it.smartValue.refValue}")
            } else {
                lastRotateAssistant = null
            }
        }

        if (adsorbAngle == null) {
            //未吸附, 查找推荐点

            val rotateRef = findSmartRotateValue(itemRenderer, rotate, angle)?.apply {
                result = smartValue.refValue - fromValue
            }

            if (rotateRef != null) {
                //找到的推荐点
                L.i("找到推荐点:fromRotate:->${rotate} ->${rotateRef.smartValue.refValue}")
                lastRotateAssistant = rotateRef
                feedback = true
            }
        }

        result = adsorbAngle ?: result

        if (feedback) {
            //找到了 震动反馈
            canvasDelegate.longFeedback()
            L.w("智能提示: angle:${angle} -> $result")
        }

        canvasDelegate.rotateItemBy(itemRenderer, result, rotateFlag)
        return result != 0f
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
            canvasDelegate.changeItemBounds(
                itemRenderer,
                width,
                height,
                adjustType
            )
            return
        }

        canvasDelegate.changeItemBounds(
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
     * 查找[left] [right] 附近最优的推荐点
     * [dx] 想要偏移的量
     * */
    fun findSmartXValue(
        itemRenderer: BaseItemRenderer<*>,
        left: Float,
        right: Float,
        dx: Float
    ): SmartAssistantData? {
        var result: SmartAssistantData? = null

        if (dx > 0) {
            //向右平移, 优先查找right的推荐点
            result = _findSmartRefValue(
                itemRenderer,
                xRefValueList,
                right,
                dx,
                translateAdsorbThreshold
            )
            if (result == null) {
                //如果没有找到, 则考虑找left的推荐点
                result = _findSmartRefValue(
                    itemRenderer,
                    xRefValueList,
                    left,
                    dx,
                    translateAdsorbThreshold
                )
            }
        } else {
            //向左平移
            result = _findSmartRefValue(
                itemRenderer,
                xRefValueList,
                left,
                dx,
                translateAdsorbThreshold
            )
            if (result == null) {
                //如果没有找到, 则考虑找left的推荐点
                result = _findSmartRefValue(
                    itemRenderer,
                    xRefValueList,
                    right,
                    dx,
                    translateAdsorbThreshold
                )
            }
        }

        result?.apply {
            //x横向推荐点 提示框

            val canvasViewBox = canvasDelegate.getCanvasViewBox()
            val refRenderer = smartValue.refRenderer

            val top = if (refRenderer == null || itemRenderer == null) {
                canvasViewBox.mapCoordinateSystemPoint(0f, 0f).y
            } else {
                min(refRenderer.getRenderBounds().top, itemRenderer.getRenderBounds().top)
            }

            val bottom = if (refRenderer == null || itemRenderer == null) {
                canvasViewBox.mapCoordinateSystemPoint(0f, canvasViewBox.getContentBottom()).y
            } else {
                max(refRenderer.getRenderBounds().bottom, itemRenderer.getRenderBounds().bottom)
            }

            drawRect = RectF(
                smartValue.refValue,
                top - canvasViewBox.getCoordinateSystemY(),
                smartValue.refValue,
                bottom - canvasViewBox.getCoordinateSystemY()
            )
        }

        return result
    }

    /**查找推荐点
     * [refValueList] 推荐点池子
     * [originValue] 原始的点位值
     * [dValue] 本次更新的差值
     * [adsorbThreshold] 吸附的阈值, 值越大, 越容易吸附到推荐值
     * */
    fun _findSmartRefValue(
        itemRenderer: BaseItemRenderer<*>,
        refValueList: List<SmartAssistantValueData>,
        originValue: Float,
        dValue: Float,
        adsorbThreshold: Float
    ): SmartAssistantData? {
        var smartValue: SmartAssistantValueData? = null
        //差值越小越好
        var diffValue = adsorbThreshold
        refValueList.forEach {
            if (it.refRenderer != null && it.refRenderer == itemRenderer) {
                //自身
            } else {
                if (it.refValue == originValue && dValue.absoluteValue <= diffValue) {
                    //吸附值
                    smartValue = it
                    return@forEach
                } else {
                    val vAbs = (it.refValue - (originValue + dValue)).absoluteValue
                    if (vAbs <= diffValue) {
                        diffValue = vAbs
                        smartValue = it
                    }
                }
            }
        }

        if (smartValue != null) {
            return SmartAssistantData(originValue, smartValue!!)
        }
        return null
    }

    /**
     * 查找[top] [bottom] 附近最优的推荐点
     * */
    fun findSmartYValue(
        itemRenderer: BaseItemRenderer<*>,
        top: Float,
        bottom: Float,
        dy: Float,
    ): SmartAssistantData? {
        var result: SmartAssistantData?

        if (dy > 0) {
            //向下平移, 优先查找bottom的推荐点
            result = _findSmartRefValue(
                itemRenderer,
                yRefValueList,
                bottom,
                dy,
                translateAdsorbThreshold
            )
            if (result == null) {
                //如果没有找到, 则考虑找top的推荐点
                result = _findSmartRefValue(
                    itemRenderer,
                    yRefValueList,
                    top,
                    dy,
                    translateAdsorbThreshold
                )
            }
        } else {
            //向上平移
            result = _findSmartRefValue(
                itemRenderer,
                yRefValueList,
                top,
                dy,
                translateAdsorbThreshold
            )
            if (result == null) {
                result = _findSmartRefValue(
                    itemRenderer,
                    yRefValueList,
                    bottom,
                    dy,
                    translateAdsorbThreshold
                )
            }
        }

        result?.apply {
            //y纵向推荐点 提示框

            val canvasViewBox = canvasDelegate.getCanvasViewBox()
            val refRenderer = smartValue.refRenderer

            val left = if (refRenderer == null || itemRenderer == null) {
                canvasViewBox.mapCoordinateSystemPoint(0f, 0f).x
            } else {
                min(refRenderer.getRenderBounds().left, itemRenderer.getRenderBounds().left)
            }

            val right = if (refRenderer == null || itemRenderer == null) {
                canvasViewBox.mapCoordinateSystemPoint(canvasViewBox.getContentRight(), 0f).x
            } else {
                max(refRenderer.getRenderBounds().right, itemRenderer.getRenderBounds().right)
            }

            drawRect = RectF(
                left - canvasViewBox.getCoordinateSystemX(),
                smartValue.refValue,
                right - canvasViewBox.getCoordinateSystemX(),
                smartValue.refValue
            )
        }

        return result
    }

    /**
     * 查找[rotate]附近最优的推荐点, [forward]正向查找or负向查找
     * */
    fun findSmartRotateValue(
        itemRenderer: BaseItemRenderer<*>,
        rotate: Float,
        angle: Float
    ): SmartAssistantData? {
        val result: SmartAssistantData? = _findSmartRefValue(
            itemRenderer,
            rotateRefValueList,
            if (rotate < 0) rotate + 360 else rotate,
            angle,
            rotateAdsorbThreshold
        )

        result?.apply {
            //旋转推荐角度 提示框

            val canvasViewBox = canvasDelegate.getCanvasViewBox()
            val refRenderer = smartValue.refRenderer

            val viewRect = canvasViewBox
                .mapCoordinateSystemRect(canvasDelegate.viewBounds, _tempRect)
            val renderBounds = itemRenderer.getRenderBounds()
            var left = viewRect.left
            var right = viewRect.right
            var top = renderBounds.centerY()
            var bottom = top

            rotateMatrix.reset()
            rotateMatrix.postRotate(
                smartValue.refValue,
                renderBounds.centerX(),
                renderBounds.centerY()
            )

            rotateMatrix.mapPoint(left, top).apply {
                left = x
                top = y
            }

            rotateMatrix.mapPoint(right, bottom).apply {
                right = x
                bottom = y
            }

            drawRect = RectF(
                left - canvasViewBox.getCoordinateSystemX(),
                top - canvasViewBox.getCoordinateSystemY(),
                right - canvasViewBox.getCoordinateSystemX(),
                bottom - canvasViewBox.getCoordinateSystemY()
            )
        }

        return result
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