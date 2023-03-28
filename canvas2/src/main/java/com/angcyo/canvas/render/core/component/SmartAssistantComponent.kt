package com.angcyo.canvas.render.core.component

import android.graphics.Canvas
import android.graphics.RectF
import android.widget.LinearLayout
import androidx.core.graphics.withRotation
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.canvas.render.core.IComponent
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.data.SmartAssistantDistanceTextData
import com.angcyo.canvas.render.data.SmartAssistantReferenceValue
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.canvas.render.util.createRenderTextPaint
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * 智能提示助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/27
 */
class SmartAssistantComponent(val controlManager: CanvasControlManager) : IRenderer, IComponent {

    override var isEnableComponent: Boolean = true

    override var renderFlags: Int = 0xf

    /**智能提示的颜色*/
    var smartAssistantColor = _color(
        R.color.canvas_render_assistant,
        controlManager.delegate.view.context
    )

    /**距离提示的颜色*/
    var smartAssistantDistanceColor = _color(
        R.color.error,
        controlManager.delegate.view.context
    )

    /**画笔, 绘制提示线*/
    val paint = createRenderPaint(smartAssistantColor)

    /**绘制距离信息*/
    val textPaint = createRenderTextPaint(smartAssistantDistanceColor)

    /**当前的边界矩形*/
    @CanvasInsideCoordinate
    private val visibleBoundsInside: RectF
        get() = controlManager.delegate.renderViewBox.visibleBoundsInside

    /**选中的元素Bounds*/
    private val selectorElementBounds: RectF?
        get() = if (controlManager.delegate.selectorManager.isSelectorElement) {
            controlManager.delegate.selectorManager.selectorComponent.renderProperty?.getRenderBounds()
        } else {
            null
        }

    /**画布当前的缩放系数*/
    private val renderScale: Float
        get() = controlManager.delegate.renderViewBox.getScaleX()

    /**数据提供*/
    var valueProvider = SmartAssistantValueProvider(controlManager.delegate)

    /**吸附阈值, 当距离推荐线的距离小于等于此值时, 自动吸附
     * 值越小, 不容易吸附. 就会导致频繁计算推荐点.
     *
     * 当距离推荐值, 小于等于这个值, 就选取这个推荐值
     * */
    var translateAdsorbThreshold: Float = 3f * dp

    /**旋转吸附角度, 当和目标角度小于这个值时, 自动吸附到目标*/
    var rotateAdsorbThreshold: Float = 5f

    private val _translateAdsorbThreshold: Float
        get() = translateAdsorbThreshold / renderScale

    //region ---入口点---

    /**开始控制
     * [com.angcyo.canvas.render.core.component.BaseControl.startControl]*/
    @CallPoint
    fun initSmartAssistant() {
        //初始化对应的数据
        valueProvider.getTranslateXRefValue()
        valueProvider.getTranslateYRefValue()
        valueProvider.getRotateRefValue()
    }

    /**结束控制
     * [com.angcyo.canvas.render.core.component.BaseControl.endControl]*/
    @CallPoint
    fun clearSmartAssistant() {
        lastSmartXValue = null
        lastSmartYValue = null
        lastSmartRotateValue = null
    }

    //endregion ---入口点---

    //region ---操作---

    /**查找智能推荐的dx值
     * [elementBounds] 元素按下时, 最后原始的边界
     * [tx] 当前需要执行的tx
     * [dx] 当前手指移动的距离, 用来判断手势的方向
     * @return 返回新的tx */
    @Pixel
    @CanvasInsideCoordinate
    fun findSmartDx(elementBounds: RectF?, tx: Float, dx: Float): Float? {
        elementBounds ?: return null
        var requestValue = if (tx > 0) {
            //向右移动, 优先使用 right
            elementBounds.right
        } else {
            //向左移动, 优先使用 left
            elementBounds.left
        }

        findSmartX(requestValue, tx, dx)?.let { ref ->
            return ref.value - requestValue
        }

        //如果还未找到, 则使用中点再次查询
        requestValue = elementBounds.centerX()
        findSmartX(requestValue, tx, dx)?.let { ref ->
            return ref.value - requestValue
        }

        return null
    }

    /**查找智能推荐的宽度, 返回推荐的宽度
     * [findSmartDx]*/
    fun findSmartWidth(elementBounds: RectF?, tx: Float, dx: Float): Float? {
        elementBounds ?: return null
        val requestValue = elementBounds.right
        findSmartX(requestValue, tx, dx)?.let { ref ->
            return ref.value - elementBounds.left
        }
        return null
    }

    /**[findSmartDx]*/
    @Pixel
    @CanvasInsideCoordinate
    fun findSmartDy(elementBounds: RectF?, ty: Float, dy: Float): Float? {
        elementBounds ?: return null
        val ref = findSmartY(elementBounds.top, ty, dy) ?: return null
        return ref.value - elementBounds.top
    }

    /**查找智能推荐的高度, 返回推荐的高度
     * [findSmartDy]*/
    fun findSmartHeight(elementBounds: RectF?, ty: Float, dy: Float): Float? {
        elementBounds ?: return null
        val requestValue = elementBounds.bottom
        findSmartY(requestValue, ty, dy)?.let { ref ->
            return ref.value - elementBounds.top
        }
        return null
    }

    /**找到智能推荐的角度
     * [angle] 当前旋转的角度, to的角度, 非by的角度
     * @return 返回要旋转to的角度
     * */
    fun findSmartRotate(angle: Float): Float? {
        var result: SmartAssistantReferenceValue? = null
        val referenceValue = lastSmartRotateValue

        result = if (referenceValue == null) {
            //之前没有推荐值, 则查找推荐值
            findSmartRotateValue(angle)?.apply {
                longFeedback()
                logSmartValue("智能推荐角度", angle, this)
            }
        } else {
            //已有推荐值, 则判断是否要吸附
            if ((referenceValue.value - angle).absoluteValue <= rotateAdsorbThreshold) {
                referenceValue.apply {
                    logSmartValue("智能吸附角度", angle, this)
                }
            } else {
                //查找最新的推荐值
                findSmartRotateValue(angle)?.apply {
                    if (value != referenceValue.value) {
                        longFeedback()
                        logSmartValue("智能推荐角度", angle, this)
                    } else {
                        logSmartValue("相同推荐角度", angle, this)
                    }
                }
            }
        }
        lastSmartRotateValue = result
        return result?.value
    }

    /**查找智能推荐的x值
     * [x] 当前元素的x坐标
     * [tx] 当前元素原本要移动的距离
     * [dx] 当前手势移动了多少距离
     * @return 推荐值
     * */
    @Pixel
    @CanvasInsideCoordinate
    private fun findSmartX(x: Float, tx: Float, dx: Float): SmartAssistantReferenceValue? {
        var result: SmartAssistantReferenceValue? = null
        val referenceValue = lastSmartXValue
        val newX = x + tx
        result = if (referenceValue == null) {
            //之前没有推荐值, 则查找推荐值
            findSmartXValue(newX, dx)?.apply {
                longFeedback()
                logSmartValue("智能推荐x[${dx}]", x, this)
            }
        } else {
            //已有推荐值, 则判断是否要吸附
            if (dx == 0f /*未移动时*/ && (referenceValue.value - newX).absoluteValue <= _translateAdsorbThreshold) {
                referenceValue.apply {
                    logSmartValue("吸附x[${dx}]", x, this)
                }
            } else {
                //查找最新的推荐值
                findSmartXValue(newX, dx)?.apply {
                    if (value != referenceValue.value) {
                        longFeedback()
                        logSmartValue("智能推荐x[$dx]", x, this)
                    } else {
                        logSmartValue("相同推荐x[$dx]", x, this)
                    }
                }
            }
        }
        lastSmartXValue = result
        return result
    }

    /**[findSmartX] */
    @Pixel
    @CanvasInsideCoordinate
    private fun findSmartY(
        y: Float,
        @Pixel ty: Float,
        dy: Float
    ): SmartAssistantReferenceValue? {
        var result: SmartAssistantReferenceValue? = null
        val referenceValue = lastSmartYValue
        val newY = y + ty
        result = if (referenceValue == null) {
            //之前没有推荐值, 则查找推荐值
            findSmartYValue(newY, dy)?.apply {
                longFeedback()
                logSmartValue("智能推荐y[${dy}]", y, this)
            }
        } else {
            //已有推荐值, 则判断是否要吸附
            if (dy == 0f /*未移动时*/ || (referenceValue.value - newY).absoluteValue <= _translateAdsorbThreshold) {
                referenceValue.apply {
                    logSmartValue("吸附y[${dy}]", y, this)
                }
            } else {
                //查找最新的推荐值
                findSmartYValue(newY, dy)?.apply {
                    if (value != referenceValue.value) {
                        longFeedback()
                        logSmartValue("智能推荐y[$dy]", y, this)
                    } else {
                        logSmartValue("相同推荐y[$dy]", y, this)
                    }
                }
            }
        }
        lastSmartYValue = result
        return result
    }

    //endregion ---操作---

    //region ---render---

    /**查找想要的目标[x]值, 对应的推荐值 [dx] 用来判断手势方式 */
    @Pixel
    @CanvasInsideCoordinate
    private fun findSmartXValue(x: Float, dx: Float): SmartAssistantReferenceValue? {
        var result: SmartAssistantReferenceValue? = null
        for (ref in valueProvider.translateXRefList) {
            if (dx > 0) {
                //向右查询
                if (ref.value < x) {
                    continue
                }
                if (result == null || ref.value - x < result.value - x) {
                    result = ref
                }
            } else {
                //向左查询
                if (ref.value > x) {
                    continue
                }
                if (result == null || x - ref.value < x - result.value) {
                    result = ref
                }
            }
        }
        if (result != null && (result.value - x).absoluteValue <= _translateAdsorbThreshold) {
            return result
        }
        return null
    }

    /**[findSmartXValue]*/
    @Pixel
    @CanvasInsideCoordinate
    private fun findSmartYValue(y: Float, dy: Float): SmartAssistantReferenceValue? {
        var result: SmartAssistantReferenceValue? = null
        for (ref in valueProvider.translateYRefList) {
            if (dy > 0) {
                //向下查询
                if (ref.value < y) {
                    continue
                }
                if (result == null || ref.value - y < result.value - y) {
                    result = ref
                }
            } else {
                //向上查询
                if (ref.value > y) {
                    continue
                }
                if (result == null || y - ref.value < y - result.value) {
                    result = ref
                }
            }
        }
        if (result != null && (result.value - y).absoluteValue <= _translateAdsorbThreshold) {
            return result
        }
        return null
    }

    /**[findSmartRotate]*/
    private fun findSmartRotateValue(angle: Float): SmartAssistantReferenceValue? {
        var result: SmartAssistantReferenceValue? = null
        val wrapAngle = wrapAngle(angle)
        for (ref in valueProvider.rotateRefList) {
            val refValue = (wrapAngle(ref.value))
            if (result == null || (refValue - wrapAngle).absoluteValue < (result.value - wrapAngle).absoluteValue) {
                result = ref
            }
        }
        if (result != null && (result.value - wrapAngle).absoluteValue <= rotateAdsorbThreshold) {
            return result
        }
        return null
    }

    /**扶正角度*/
    private fun wrapAngle(angle: Float) = (angle + 360) % 360

    /**上一次推荐的值*/
    private var lastSmartXValue: SmartAssistantReferenceValue? = null
    private var lastSmartYValue: SmartAssistantReferenceValue? = null
    private var lastSmartRotateValue: SmartAssistantReferenceValue? = null

    private val distanceTextOffset = 4 * dp

    /**距离提示文本*/
    private val smartAssistantDistanceTextList = mutableListOf<SmartAssistantDistanceTextData>()

    /**先绘制提示线*/
    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        smartAssistantDistanceTextList.clear()
        //选中元素的边界
        val elementBounds = selectorElementBounds ?: return
        paint.strokeWidth = 1f * dp / renderScale

        //绘制提示竖线
        lastSmartXValue?.let { ref ->
            //边界
            val refElementBounds = ref.refElementBounds
            val bounds = refElementBounds ?: visibleBoundsInside
            val startY = min(elementBounds.top, bounds.top)
            val stopY = max(elementBounds.bottom, bounds.bottom)

            paint.color = smartAssistantColor
            canvas.drawLine(ref.value, startY, ref.value, stopY, paint)

            //上边距
            if (bounds.bottom < elementBounds.top) {
                drawXDistanceLine(canvas, ref.value, bounds.bottom, elementBounds.top)
            } else if (refElementBounds != null && bounds.centerY() < elementBounds.top) {
                drawXDistanceLine(canvas, ref.value, bounds.centerY(), elementBounds.top)
            } else if (bounds.top < elementBounds.top) {
                drawXDistanceLine(canvas, ref.value, bounds.top, elementBounds.top)
            }

            //下边距
            if (bounds.top > elementBounds.bottom) {
                drawXDistanceLine(canvas, ref.value, bounds.top, elementBounds.bottom)
            } else if (refElementBounds != null && bounds.centerY() > elementBounds.bottom) {
                drawXDistanceLine(canvas, ref.value, bounds.centerY(), elementBounds.bottom)
            } else if (bounds.bottom > elementBounds.bottom) {
                drawXDistanceLine(canvas, ref.value, bounds.bottom, elementBounds.bottom)
            }
        }

        //绘制提示横线
        lastSmartYValue?.let { ref ->
            //边界
            val refElementBounds = ref.refElementBounds
            val bounds = refElementBounds ?: visibleBoundsInside
            val startX = min(elementBounds.left, bounds.left)
            val stopX = max(elementBounds.right, bounds.right)

            paint.color = smartAssistantColor
            canvas.drawLine(startX, ref.value, stopX, ref.value, paint)

            //左边距
            if (bounds.right < elementBounds.left) {
                drawYDistanceLine(canvas, ref.value, bounds.right, elementBounds.left)
            } else if (refElementBounds != null && bounds.centerX() < elementBounds.left) {
                drawYDistanceLine(canvas, ref.value, bounds.centerX(), elementBounds.left)
            } else if (bounds.left < elementBounds.left) {
                drawYDistanceLine(canvas, ref.value, bounds.left, elementBounds.left)
            }

            //右边距
            if (bounds.left > elementBounds.right) {
                drawYDistanceLine(canvas, ref.value, bounds.left, elementBounds.right)
            } else if (refElementBounds != null && bounds.centerX() > elementBounds.right) {
                drawYDistanceLine(canvas, ref.value, bounds.centerX(), elementBounds.right)
            } else if (bounds.right > elementBounds.right) {
                drawYDistanceLine(canvas, ref.value, bounds.right, elementBounds.right)
            }
        }

        lastSmartRotateValue?.let { ref ->
            paint.color = smartAssistantColor

            ref.refElementBounds?.let {
                val centerX = it.centerX()
                val centerY = it.centerY()
                canvas.withRotation(ref.value, centerX, centerY) {
                    val diagonalLine = it.diagonalLine()
                    val startX = centerX - diagonalLine / 2
                    val endX = centerX + diagonalLine / 2
                    canvas.drawLine(startX, centerY, endX, centerY, paint)
                }
            }

            val centerX = elementBounds.centerX()
            val centerY = elementBounds.centerY()
            canvas.withRotation(ref.value, centerX, centerY) {
                val diagonalLine = visibleBoundsInside.diagonalLine()
                val startX = centerX - diagonalLine / 2
                val endX = centerX + diagonalLine / 2
                canvas.drawLine(startX, centerY, endX, centerY, paint)
            }
        }
    }

    /**再绘制距离文本*/
    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        val unit = controlManager.delegate.axisManager.renderUnit
        val renderViewBox = controlManager.delegate.renderViewBox
        val point = acquireTempPointF()
        for (distanceTextData in smartAssistantDistanceTextList) {
            val text = unit.formatValue(
                unit.convertPixelToValue(distanceTextData.distance),
                true,
                true
            )
            point.set(distanceTextData.drawX, distanceTextData.drawY)
            renderViewBox.transformToOutside(point)

            if (distanceTextData.orientation == LinearLayout.HORIZONTAL) {
                //绘制横线距离
                var textDrawY = point.y - distanceTextOffset
                val textTop = textDrawY - textPaint.textHeight()

                if (textTop <= visibleBoundsInside.height() / 3) {
                    //如果文本的顶部, 在头部1/3的位置, 则文本向下偏移绘制
                    textDrawY = point.y + textPaint.textDrawHeight() + distanceTextOffset
                }

                canvas.drawText(
                    text,
                    point.x - textPaint.textWidth(text) / 2,
                    textDrawY,
                    textPaint
                )
            } else {
                //绘制纵向距离
                var textDrawX = point.x + distanceTextOffset
                val textRight = textDrawX + textPaint.textWidth(text)

                if (textRight >= visibleBoundsInside.width() * 2 / 3) {
                    //如果文本的右边, 在右侧的2/3的位置, 则文本向左偏移绘制
                    textDrawX = point.x - textPaint.textWidth(text) - distanceTextOffset
                }

                canvas.drawText(
                    text,
                    textDrawX,
                    point.y + textPaint.descent(),
                    textPaint
                )
            }
        }
        point.release()
    }

    /**绘制X方向的距离线, 竖线*/
    private fun drawXDistanceLine(canvas: Canvas, x: Float, fromY: Float, toY: Float) {
        paint.color = smartAssistantDistanceColor
        canvas.drawLine(x, fromY, x, toY, paint)
        smartAssistantDistanceTextList.add(
            SmartAssistantDistanceTextData(
                (fromY - toY).absoluteValue,
                x,
                (fromY + toY) / 2,
                LinearLayout.VERTICAL
            )
        )
    }

    /**绘制Y方向的距离线, 横线*/
    private fun drawYDistanceLine(canvas: Canvas, y: Float, fromX: Float, toX: Float) {
        paint.color = smartAssistantDistanceColor
        canvas.drawLine(fromX, y, toX, y, paint)
        smartAssistantDistanceTextList.add(
            SmartAssistantDistanceTextData(
                (fromX - toX).absoluteValue,
                (fromX + toX) / 2,
                y,
                LinearLayout.HORIZONTAL
            )
        )
    }

    private fun longFeedback() {
        //震动反馈
        controlManager.delegate.longFeedback()
    }

    private fun logSmartValue(tag: String, fromValue: Float, ref: SmartAssistantReferenceValue) {
        L.i("智能提示:$tag ${fromValue}->${ref.value}")
    }

    //endregion ---render---
}