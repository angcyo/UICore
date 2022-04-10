package com.angcyo.canvas.core.renderer

import android.graphics.*
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.BuildConfig
import com.angcyo.canvas.R
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.component.ControlHandler
import com.angcyo.canvas.core.renderer.items.IItemRenderer
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

    override fun onSelectedItem(itemRenderer: IItemRenderer<*>, oldItemRenderer: IItemRenderer<*>?) {
        super.onSelectedItem(itemRenderer, oldItemRenderer)
        updateControlPointLocation()
    }

    /**更新控制点位坐标*/
    fun updateControlPointLocation() {
        controlHandler.selectedItemRender?.let {
            controlHandler.calcControlPointLocation(canvasViewBox, it)
        }
    }

    override fun render(canvas: Canvas) {
        controlHandler.selectedItemRender?.let {
            val bounds = it.getRendererBounds()

            //绘制边框
            canvas.withMatrix(canvasViewBox.matrix) {
                canvas.drawRect(bounds, paint)
            }

            //转换之后的矩形
            val _bounds = canvasViewBox.matrix.mapRectF(bounds, controlHandler._tempRect)

            //绘制宽高文本
            val widthUnit = canvasViewBox.valueUnit.convertPixelToValueUnit(bounds.width())
            val heightUnit = canvasViewBox.valueUnit.convertPixelToValueUnit(bounds.height())

            canvas.drawText(
                widthUnit,
                _bounds.centerX() - sizePaint.textWidth(widthUnit) / 2,
                _bounds.top - sizePaint.descent() - controlHandler.sizeOffset,
                sizePaint
            )

            canvas.drawText(
                heightUnit,
                _bounds.right + controlHandler.sizeOffset,
                _bounds.centerY() + sizePaint.textHeight() / 2 - sizePaint.descent(),
                sizePaint
            )

            //按下时, 绘制x,y坐标
            if (canvasViewBox.canvasView.isTouchHold || BuildConfig.DEBUG) {
                val point = PointF(bounds.left, bounds.top)
                val value = canvasViewBox.calcDistanceValueWithOrigin(point)

                val xUnit = canvasViewBox.valueUnit.formattedValueUnit(value.x)
                val yUnit = canvasViewBox.valueUnit.formattedValueUnit(value.x)

                val text = "$xUnit x $yUnit"

                canvas.drawText(
                    text,
                    _bounds.centerX() - sizePaint.textWidth(text) / 2,
                    _bounds.bottom + sizePaint.textHeight() - sizePaint.descent() + controlHandler.sizeOffset,
                    sizePaint
                )
            }

            //绘制控制四个角
            controlHandler.controlPointList.forEach {

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
}