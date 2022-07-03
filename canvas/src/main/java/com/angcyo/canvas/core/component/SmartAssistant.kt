package com.angcyo.canvas.core.component

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.isLineShape
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

    companion object {
        /**X的智能提示*/
        const val SMART_TYPE_X = 0x01

        /**Y的智能提示*/
        const val SMART_TYPE_Y = 0x02

        /**W的智能提示*/
        const val SMART_TYPE_W = 0x04

        /**H的智能提示*/
        const val SMART_TYPE_H = 0x08

        /**R的智能提示*/
        const val SMART_TYPE_R = 0x10
    }

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
    var boundsAdsorbThreshold: Float = 10f

    //---temp

    val rotateMatrix = Matrix()

    /**矩形左上角的点*/
    val rectLTPoint = PointF()

    /**矩形右下角的点*/
    val rectRBPoint = PointF()

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
    var enableWidthSmart: Boolean = true
    var enableHeightSmart: Boolean = true
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

        if (adsorbDx == null && itemRenderer.isSupportControlPoint(SMART_TYPE_X)) {
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

        if (adsorbDy == null && itemRenderer.isSupportControlPoint(SMART_TYPE_Y)) {
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

        if (adsorbAngle == null && itemRenderer.isSupportControlPoint(SMART_TYPE_R)) {
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
    ): BooleanArray {
        if (!enable) {
            canvasDelegate.changeItemBounds(
                itemRenderer,
                width,
                height,
                adjustType
            )
            return booleanArrayOf(true, true)
        }

        val rotateBounds = itemRenderer.getRotateBounds()
        val originWidth = rotateBounds.width()
        val originHeight = rotateBounds.height()

        L.i("智能宽高请求: dx:${dx} dy:${dy}")

        var newWidth = width
        var newHeight = height

        val dw = width - originWidth
        val dh = height - originHeight

        //吸附判断
        var adsorbWidth: Float? = null
        var adsorbHeight: Float? = null

        //震动反馈
        var feedback = false

        //w吸附
        lastWidthAssistant?.let {
            if (dw.absoluteValue <= boundsAdsorbThreshold) {
                //需要吸附
                adsorbWidth = originWidth
                L.d("智能提示吸附W:${it.smartValue.refValue}")
            } else {
                lastWidthAssistant = null
            }
        }

        //h吸附
        lastHeightAssistant?.let {
            if (dh.absoluteValue <= boundsAdsorbThreshold) {
                //需要吸附
                adsorbHeight = originHeight
                L.d("智能提示吸附H:${it.smartValue.refValue}")
            } else {
                lastHeightAssistant = null
            }
        }

        if (adsorbWidth == null && itemRenderer.isSupportControlPoint(SMART_TYPE_W)) {
            //x未吸附
            val wRef = findSmartWidthValue(itemRenderer, width, dx)?.apply {
                newWidth = smartValue.refValue
            }

            if (wRef != null) {
                //找到的推荐点
                L.i("找到推荐宽度:from:${originWidth} ->${newWidth}")
                lastWidthAssistant = wRef
                feedback = true
            }
        }

        if (adsorbHeight == null && itemRenderer.isSupportControlPoint(SMART_TYPE_H)) {
            //y未吸附
            val hRef = findSmartHeightValue(itemRenderer, height, dy)?.apply {
                newHeight = smartValue.refValue
            }

            if (hRef != null) {
                //找到的推荐点
                L.i("找到推荐高度:from:${originHeight} ->${newHeight}")
                lastHeightAssistant = hRef
                feedback = true
            }
        }

        newWidth = adsorbWidth ?: newWidth
        newHeight = adsorbHeight ?: newHeight

        if (newWidth != originWidth || newHeight != originHeight) {

            if (!itemRenderer.isLineShape() && equalRatio) {
                //等比调整

                //原先的缩放比
                val originScale = originWidth / originHeight
                val newScale = newWidth / newHeight

                if ((newScale - originScale).absoluteValue <= 0.00001) {
                    //已经是等比
                } else {
                    if ((newWidth - originWidth).absoluteValue > (newHeight - originHeight).absoluteValue) {
                        //宽度变化比高度大
                        newHeight = originHeight * newWidth / originWidth
                        lastHeightAssistant = null
                    } else {
                        newWidth = originWidth * newHeight / originHeight
                        lastWidthAssistant = null
                    }
                }
            }

            if (feedback) {
                //震动反馈
                canvasDelegate.longFeedback()
                L.i("智能提示: w:${originWidth} h:${originHeight} -> nw:${newWidth} nh:${newHeight}")
            }

            canvasDelegate.changeItemBounds(
                itemRenderer,
                newWidth,
                newHeight,
                adjustType
            )
        }

        return booleanArrayOf(originWidth != newWidth, originHeight != newHeight)
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
        dx: Float,
        adsorbThreshold: Float = translateAdsorbThreshold
    ): SmartAssistantData? {
        var result: SmartAssistantData?

        if (dx > 0) {
            //向右平移, 优先查找right的推荐点
            result = _findSmartRefValue(
                itemRenderer,
                xRefValueList,
                right,
                dx,
                adsorbThreshold
            )
            if (result == null && right != left) {
                //如果没有找到, 则考虑找left的推荐点
                result = _findSmartRefValue(
                    itemRenderer,
                    xRefValueList,
                    left,
                    dx,
                    adsorbThreshold
                )
            }
        } else {
            //向左平移
            result = _findSmartRefValue(
                itemRenderer,
                xRefValueList,
                left,
                dx,
                adsorbThreshold
            )
            if (result == null && right != left) {
                //如果没有找到, 则考虑找left的推荐点
                result = _findSmartRefValue(
                    itemRenderer,
                    xRefValueList,
                    right,
                    dx,
                    adsorbThreshold
                )
            }
        }

        result?.apply {
            //x横向推荐点 提示框

            val canvasViewBox = canvasDelegate.getCanvasViewBox()
            val refRenderer = smartValue.refRenderer

            val top = if (refRenderer == null) {
                canvasViewBox.mapCoordinateSystemPoint(0f, 0f).y
            } else {
                min(refRenderer.getRenderBounds().top, itemRenderer.getRenderBounds().top)
            }

            val bottom = if (refRenderer == null) {
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
        adsorbThreshold: Float = translateAdsorbThreshold
    ): SmartAssistantData? {
        var result: SmartAssistantData?

        if (dy > 0) {
            //向下平移, 优先查找bottom的推荐点
            result = _findSmartRefValue(
                itemRenderer,
                yRefValueList,
                bottom,
                dy,
                adsorbThreshold
            )
            if (result == null) {
                //如果没有找到, 则考虑找top的推荐点
                result = _findSmartRefValue(
                    itemRenderer,
                    yRefValueList,
                    top,
                    dy,
                    adsorbThreshold
                )
            }
        } else {
            //向上平移
            result = _findSmartRefValue(
                itemRenderer,
                yRefValueList,
                top,
                dy,
                adsorbThreshold
            )
            if (result == null) {
                result = _findSmartRefValue(
                    itemRenderer,
                    yRefValueList,
                    bottom,
                    dy,
                    adsorbThreshold
                )
            }
        }

        result?.apply {
            //y纵向推荐点 提示框

            val canvasViewBox = canvasDelegate.getCanvasViewBox()
            val refRenderer = smartValue.refRenderer

            val left = if (refRenderer == null) {
                canvasViewBox.mapCoordinateSystemPoint(0f, 0f).x
            } else {
                min(refRenderer.getRenderBounds().left, itemRenderer.getRenderBounds().left)
            }

            val right = if (refRenderer == null) {
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

    fun findSmartWidthValue(
        itemRenderer: BaseItemRenderer<*>,
        width: Float,
        dx: Float
    ): SmartAssistantData? {
        if (!enableWidthSmart) {
            return null
        }

        var result: SmartAssistantData? = null
        val bounds = itemRenderer.getBounds()
        val rotateBounds = itemRenderer.getRotateBounds()
        val left = rotateBounds.left
        val right = rotateBounds.right

        val xSmartValue = if (bounds.isFlipHorizontal) {
            findSmartXValue(itemRenderer, left, left, dx, boundsAdsorbThreshold)
        } else {
            findSmartXValue(itemRenderer, right, right, dx, boundsAdsorbThreshold)
        }

        xSmartValue?.apply {
            //通过推荐的right, 计算推荐的width

            rotateMatrix.reset()
            rotateMatrix.postRotate(
                itemRenderer.rotate,
                bounds.centerX(),
                bounds.centerY()
            )
            //先算出原始左上角旋转后的坐标
            rectLTPoint.set(bounds.left, bounds.top)
            rotateMatrix.mapPoint(rectLTPoint, rectLTPoint)

            //再算出原始右下角旋转后的坐标
            rectRBPoint.set(bounds.right, bounds.top)//注意top
            rotateMatrix.mapPoint(rectRBPoint, rectRBPoint)

            //修改右下角的x坐标
            val x2 = smartValue.refValue //推荐的x
            val y2 = rectRBPoint.y

            //逆向算出新的宽度
            val smartWidth = ScaleControlPoint.calcRotateBeforeDistance(
                rectLTPoint.x,
                rectLTPoint.y,
                x2,
                y2,
                bounds.centerX(),
                bounds.centerY(),
                itemRenderer.rotate
            )[0]

            result = SmartAssistantData(
                rotateBounds.width(),
                SmartAssistantValueData(smartWidth, smartValue.refRenderer),
                drawRect
            )
        }
        return result
    }

    fun findSmartHeightValue(
        itemRenderer: BaseItemRenderer<*>,
        height: Float,
        dy: Float
    ): SmartAssistantData? {
        if (!enableHeightSmart) {
            return null
        }

        var result: SmartAssistantData? = null
        val bounds = itemRenderer.getBounds()
        val rotateBounds = itemRenderer.getRotateBounds()
        val top = rotateBounds.top
        val bottom = rotateBounds.bottom

        val ySmartValue = if (bounds.isFlipVertical) {
            findSmartYValue(itemRenderer, top, top, dy, boundsAdsorbThreshold)
        } else {
            findSmartYValue(itemRenderer, bottom, bottom, dy, boundsAdsorbThreshold)
        }

        ySmartValue?.apply {
            //通过推荐的bottom, 计算推荐的height

            rotateMatrix.reset()
            rotateMatrix.postRotate(
                itemRenderer.rotate,
                bounds.centerX(),
                bounds.centerY()
            )
            //先算出原始左上角旋转后的坐标
            rectLTPoint.set(bounds.left, bounds.top)
            rotateMatrix.mapPoint(rectLTPoint, rectLTPoint)

            //再算出原始右下角旋转后的坐标
            rectRBPoint.set(bounds.right, bounds.bottom) //注意bottom
            rotateMatrix.mapPoint(rectRBPoint, rectRBPoint)

            //修改右下角的y坐标
            val x2 = rectRBPoint.x
            val y2 = smartValue.refValue //推荐的y

            //逆向算出新的高度
            val smartHeight = ScaleControlPoint.calcRotateBeforeDistance(
                rectLTPoint.x,
                rectLTPoint.y,
                x2,
                y2,
                bounds.centerX(),
                bounds.centerY(),
                itemRenderer.rotate
            )[1]

            result = SmartAssistantData(
                rotateBounds.height(),
                SmartAssistantValueData(smartHeight, smartValue.refRenderer),
                drawRect
            )
        }
        return result
    }

    //endregion ---find---
}