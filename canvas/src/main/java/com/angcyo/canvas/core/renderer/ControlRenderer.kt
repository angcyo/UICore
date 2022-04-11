package com.angcyo.canvas.core.renderer

import android.graphics.*
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.angcyo.canvas.BuildConfig
import com.angcyo.canvas.R
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.component.ControlHandler
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.utils.createPaint
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.canvas.utils.mapRectF
import com.angcyo.library.ex.*

/**
 * 选中[IItemsRenderer]后, 用来绘制控制按钮的渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class ControlRenderer(val controlHandler: ControlHandler, canvasViewBox: CanvasViewBox) :
    BaseRenderer(canvasViewBox), ICanvasListener {

    /**用来绘制边框*/
    val paint = createPaint(_color(R.color.colorAccent)).apply {
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

    init {
        canvasViewBox.canvasView.canvasListenerList.add(this)
    }

    override fun onCanvasMatrixUpdate(matrix: Matrix, oldValue: Matrix) {
        super.onCanvasMatrixUpdate(matrix, oldValue)
        updateControlPointLocation()
    }

    override fun onItemMatrixChangeAfter(itemRenderer: IItemRenderer<*>) {
        super.onItemMatrixChangeAfter(itemRenderer)
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

    val _textBounds = RectF()
    val _tempPoint: PointF = PointF()
    val _rotateRect: RectF = RectF()

    override fun render(canvas: Canvas) {
        controlHandler.selectedItemRender?.let {
            val bounds = it.getRendererBounds()
            val rotate = it.rendererItem?.rotate ?: 0f

            //绘制边框
            canvas.withMatrix(canvasViewBox.matrix) {
                canvas.withRotation(rotate, bounds.centerX(), bounds.centerY()) {
                    canvas.drawRect(bounds, paint)
                }
            }

            //放大后的矩形, 进行相应的旋转
            val rotateRect = it.mapRotateRect(bounds, _rotateRect)
            /*if (BuildConfig.DEBUG) {
                //绘制旋转之后的矩形
                canvas.withMatrix(canvasViewBox.matrix) {
                    canvas.drawRect(rotateRect, paint)
                }
            }*/

            //绘制宽高文本
            val widthUnit = canvasViewBox.valueUnit.convertPixelToValueUnit(rotateRect.width())
            val heightUnit = canvasViewBox.valueUnit.convertPixelToValueUnit(rotateRect.height())

            //转换之后的矩形
            val _bounds = canvasViewBox.matrix.mapRectF(bounds, controlHandler._tempRect)

            //绘制控制信息, 宽高xy值
            canvas.withRotation(rotate, _bounds.centerX(), _bounds.centerY()) {

                //绘制宽度
                var textWidth = sizePaint.textWidth(widthUnit)
                _textBounds.set(
                    _bounds.centerX() - textWidth / 2,
                    _bounds.top - sizePaint.textHeight() - controlHandler.sizeOffset,
                    _bounds.centerX() + textWidth / 2,
                    _bounds.top - controlHandler.sizeOffset
                )
                withTextScale(rotate, _textBounds) {
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
                    _bounds.right + controlHandler.sizeOffset,
                    _bounds.centerY() - sizePaint.textHeight() / 2,
                    _bounds.right + controlHandler.sizeOffset + textWidth,
                    _bounds.centerY() + sizePaint.textHeight() / 2
                )
                withTextScale(rotate, _textBounds) {
                    canvas.drawText(
                        heightUnit,
                        _bounds.right + controlHandler.sizeOffset,
                        _bounds.centerY() + sizePaint.textHeight() / 2 - sizePaint.descent(),
                        sizePaint
                    )
                }

                //按下时, 绘制x,y坐标
                if (canvasViewBox.canvasView.isTouchHold || BuildConfig.DEBUG) {
                    _tempPoint.set(rotateRect.left, rotateRect.top)
                    val point = _tempPoint
                    val value = canvasViewBox.calcDistanceValueWithOrigin(point)

                    val xUnit = canvasViewBox.valueUnit.formattedValueUnit(value.x)
                    val yUnit = canvasViewBox.valueUnit.formattedValueUnit(value.y)

                    val text = "$xUnit x $yUnit"

                    textWidth = sizePaint.textWidth(text)
                    _textBounds.set(
                        _bounds.centerX() - textWidth / 2,
                        _bounds.bottom + controlHandler.sizeOffset,
                        _bounds.centerX() + textWidth / 2,
                        _bounds.bottom + sizePaint.textHeight() + controlHandler.sizeOffset
                    )

                    withTextScale(rotate, _textBounds) {
                        canvas.drawText(
                            text,
                            _textBounds.left,
                            _textBounds.bottom - sizePaint.descent(),
                            sizePaint
                        )
                    }
                }
            }

            //绘制控制四个角
            controlHandler.controlPointList.forEach {
                canvas.withControlPointRotation(rotate, it) {
                    //控制点的背景绘制
                    canvas.drawCircle(
                        it.bounds.centerX(),
                        it.bounds.centerY(),
                        controlHandler.controlPointSize / 2,
                        if (it == controlHandler.touchControlPoint && canvasViewBox.canvasView.isTouchHold) controlTouchPointPaint else controlPointPaint
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

            /*//左上角的点
            val ltX = _bounds.left
            val ltY = _bounds.top
            canvas.drawCircle(ltX, ltY, 10f, sizePaint)

            //右上角的点
            val rtX = _bounds.right
            val rtY = _bounds.top
            canvas.drawCircle(rtX, rtY, 10f, sizePaint)

            //左下角的点
            val lbX = _bounds.left
            val lbY = _bounds.bottom
            canvas.drawCircle(lbX, lbY, 10f, sizePaint)

            //右下角的点
            val rbX = _bounds.right
            val rbY = _bounds.bottom
            canvas.drawCircle(rbX, rbY, 10f, sizePaint)*/
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
}