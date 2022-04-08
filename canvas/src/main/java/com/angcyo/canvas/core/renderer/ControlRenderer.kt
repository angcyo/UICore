package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.ControlHandler
import com.angcyo.canvas.utils.createPaint
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.canvas.utils.mapRectF
import com.angcyo.drawable.BuildConfig
import com.angcyo.drawable.textHeight
import com.angcyo.drawable.textWidth
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.toColorInt

/**
 * 选中[IItemsRenderer]后, 用来绘制控制按钮的渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class ControlRenderer(val controlHandler: ControlHandler, canvasViewBox: CanvasViewBox) :
    BaseRenderer(canvasViewBox) {

    /**用来绘制边框*/
    val paint = createPaint("#6e9cce".toColorInt()).apply {
        //init
    }

    /**用来绘制选中元素的宽高*/
    val sizePaint = createTextPaint(Color.GRAY).apply {
        textSize = 9 * dp
    }

    override fun render(canvas: Canvas) {
        controlHandler.selectedItemRender?.let {
            val bounds = it.getRenderBounds()

            //绘制边框
            canvas.withMatrix(canvasViewBox.matrix) {
                canvas.drawRect(bounds, paint)
            }

            //转换之后的矩形
            val _bounds = canvasViewBox.matrix.mapRectF(bounds)

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
            if (canvasViewBox.canvasView.canvasTouchHandler.isTouchHold() || BuildConfig.DEBUG) {
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

        }
    }
}