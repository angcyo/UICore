package com.angcyo.canvas.core.renderer

import android.graphics.*
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.BuildConfig
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.Reason
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.component.ControlHandler
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.component.control.RotateControlPoint
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.utils.canvasDecimal
import com.angcyo.canvas.utils.createPaint
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.library.ex.*
import com.angcyo.library.unit.convertPixelToValueUnit

/**
 * 选中[IItemsRenderer]后, 用来绘制控制按钮的渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class ControlRenderer(val controlHandler: ControlHandler, canvasView: ICanvasView) :
    BaseRenderer(canvasView), ICanvasListener {

    /**用来绘制边框*/
    val paint = createPaint(_color(R.color.canvas_select)).apply {
        //init
        strokeWidth = 1 * dp
    }

    /**用来绘制选中元素的宽高*/
    val sizePaint = createTextPaint(Color.GRAY).apply {
        textSize = 9 * dp
    }

    /**用来绘制控制点图标的笔*/
    val controlPointPaint = createPaint(Color.GRAY, Paint.Style.FILL)

    /**绘制按下的控制点*/
    val controlTouchPointPaint = createPaint(Color.DKGRAY, Paint.Style.FILL)

    /**是否需要绘制控制点*/
    var drawControlPoint: Boolean = true

    val isTouchHold: Boolean
        get() {
            val canvasView = canvasViewBox.canvasView
            if (canvasView is CanvasDelegate) {
                return canvasView.isTouchHold
            }
            return false
        }

    init {
        canvasViewBox.canvasView.addCanvasListener(this)
    }

    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldValue: Matrix
    ) {
        super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldValue)
        updateControlPointLocation()
    }

    override fun onItemBoundsChanged(itemRenderer: IRenderer, reason: Reason, oldBounds: RectF) {
        super.onItemBoundsChanged(itemRenderer, reason, oldBounds)
        if (itemRenderer == controlHandler.selectedItemRender) {
            updateControlPointLocation()
        }
    }

    override fun onSelectedItem(
        itemRenderer: IItemRenderer<*>,
        oldItemRenderer: IItemRenderer<*>?
    ) {
        super.onSelectedItem(itemRenderer, oldItemRenderer)
        updateControlPointLocation()
    }

    /**更新控制点位坐标*/
    fun updateControlPointLocation() {
        controlHandler.selectedItemRender?.let {
            controlHandler.calcControlPointLocation(canvasViewBox, it)
        }
    }

    val _textBounds = emptyRectF()
    val _tempPoint: PointF = PointF()

    override fun render(canvas: Canvas) {
        controlHandler.selectedItemRender?.let {

            //目标相对于视图左上角的位置
            val visualBounds = it.getVisualBounds()
            val rotate = it.rotate

            //绘制控制信息, 宽高xy值
            canvas.withRotation(rotate, visualBounds.centerX(), visualBounds.centerY()) {
                //绘制边框
                canvas.withTranslation(-paint.strokeWidth / 2, -paint.strokeWidth / 2) {
                    canvas.drawRect(visualBounds, paint)
                }
                drawFrameText(canvas, visualBounds, it.getRenderRotateBounds(), rotate)
                if (controlHandler.touchControlPoint is RotateControlPoint) {
                    //绘制旋转的角度
                    drawRotateText(canvas, visualBounds, rotate)
                }
            }

            if (drawControlPoint) {
                //绘制控制四个角
                drawControlPoint(canvas, rotate)
            }
        }
    }

    fun Canvas.withTextScale(rotate: Float, textBounds: RectF, block: Canvas.() -> Unit) {
        if (rotate >= -90 && rotate <= 90) {
            block()
        } else {
            withScale(-1f, -1f, textBounds.centerX(), textBounds.centerY(), block)
        }
    }

    fun Canvas.withControlPointRotation(
        rotate: Float,
        controlPoint: ControlPoint,
        block: Canvas.() -> Unit
    ) {
        val controlPointRotate =
            if (controlPoint.type == ControlPoint.POINT_TYPE_SCALE || controlPoint.type == ControlPoint.POINT_TYPE_ROTATE) {
                rotate
            } else {
                0f
            }

        withRotation(
            controlPointRotate,
            controlPoint.bounds.centerX(),
            controlPoint.bounds.centerY(),
            block
        )
    }

    /**绘制旋转的度数*/
    fun drawRotateText(canvas: Canvas, visualBounds: RectF, rotate: Float) {
        val rotateText = "${rotate.canvasDecimal(2)}°"
        //绘制高度
        val textWidth = sizePaint.textWidth(rotateText)
        val left = visualBounds.left - textWidth - controlHandler.sizeOffset
        _textBounds.set(
            left,
            visualBounds.centerY() - sizePaint.textHeight() / 2,
            left + textWidth,
            visualBounds.centerY() + sizePaint.textHeight() / 2
        )
        canvas.withTextScale(rotate, _textBounds) {
            canvas.drawText(
                rotateText,
                _textBounds.left,
                _textBounds.bottom - sizePaint.descent(),
                sizePaint
            )
        }
    }

    /**绘制控制信息, 宽高xy值
     * [visualBounds] 元素未旋转时的坐标, 用来确定绘制坐标
     * [renderRotateBounds] 元素旋转之后, 占用的矩形坐标, 用来计算距离
     * [rotate] 元素旋转的角度*/
    fun drawFrameText(
        canvas: Canvas,
        visualBounds: RectF,
        renderRotateBounds: RectF,
        rotate: Float
    ) {
        //绘制宽高文本
        val widthUnit = canvasViewBox.valueUnit.convertPixelToValueUnit(renderRotateBounds.width())
        val heightUnit =
            canvasViewBox.valueUnit.convertPixelToValueUnit(renderRotateBounds.height())

        //绘制宽度
        var textWidth = sizePaint.textWidth(widthUnit)
        _textBounds.set(
            visualBounds.centerX() - textWidth / 2,
            visualBounds.top - sizePaint.textHeight() - controlHandler.sizeOffset,
            visualBounds.centerX() + textWidth / 2,
            visualBounds.top - controlHandler.sizeOffset
        )
        canvas.withTextScale(rotate, _textBounds) {
            canvas.drawText(
                widthUnit,
                _textBounds.left,
                _textBounds.bottom - sizePaint.descent(),
                sizePaint
            )
        }

        //绘制高度
        textWidth = sizePaint.textWidth(heightUnit)
        _textBounds.set(
            visualBounds.right + controlHandler.sizeOffset,
            visualBounds.centerY() - sizePaint.textHeight() / 2,
            visualBounds.right + controlHandler.sizeOffset + textWidth,
            visualBounds.centerY() + sizePaint.textHeight() / 2
        )
        canvas.withTextScale(rotate, _textBounds) {
            canvas.drawText(
                heightUnit,
                visualBounds.right + controlHandler.sizeOffset,
                visualBounds.centerY() + sizePaint.textHeight() / 2 - sizePaint.descent(),
                sizePaint
            )
        }

        //按下时, 绘制x,y坐标
        if (isTouchHold || BuildConfig.DEBUG) {
            _tempPoint.set(renderRotateBounds.left, renderRotateBounds.top)
            val point = _tempPoint
            val value = canvasViewBox.calcDistanceValueWithOrigin(point)

            val xUnit = canvasViewBox.valueUnit.formattedValueUnit(value.x)
            val yUnit = canvasViewBox.valueUnit.formattedValueUnit(value.y)

            val text = "$xUnit x $yUnit"

            textWidth = sizePaint.textWidth(text)
            _textBounds.set(
                visualBounds.centerX() - textWidth / 2,
                visualBounds.bottom + controlHandler.sizeOffset,
                visualBounds.centerX() + textWidth / 2,
                visualBounds.bottom + sizePaint.textHeight() + controlHandler.sizeOffset
            )

            canvas.withTextScale(rotate, _textBounds) {
                canvas.drawText(
                    text,
                    _textBounds.left,
                    _textBounds.bottom - sizePaint.descent(),
                    sizePaint
                )
            }
        }
    }

    /**绘制控制四个角*/
    fun drawControlPoint(canvas: Canvas, rotate: Float) {
        controlHandler.controlPointList.forEach {
            if (it.enable) {
                canvas.withControlPointRotation(rotate, it) {
                    //控制点的背景绘制
                    canvas.drawCircle(
                        it.bounds.centerX(),
                        it.bounds.centerY(),
                        controlHandler.controlPointSize / 2,
                        if (it == controlHandler.touchControlPoint && isTouchHold) controlTouchPointPaint else controlPointPaint
                    )

                    //控制点的图标绘制
                    it.drawable?.apply {
                        setBounds(
                            it.bounds.left.toInt() + controlHandler.controlPointPadding,
                            it.bounds.top.toInt() + controlHandler.controlPointPadding,
                            it.bounds.right.toInt() - controlHandler.controlPointPadding,
                            it.bounds.bottom.toInt() - controlHandler.controlPointPadding
                        )
                        draw(canvas)
                    }
                }
                //canvas.drawRect(it.bounds, controlPointPaint)
            }
        }
    }
}