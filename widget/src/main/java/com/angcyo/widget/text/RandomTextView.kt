package com.angcyo.widget.text

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.angcyo.library.ex.alphaRatio
import com.angcyo.library.ex.postDelay
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import kotlin.random.Random.Default.nextFloat

/**
 * 随机文本的[AppCompatTextView]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/13
 */
class RandomTextView(context: Context, attrs: AttributeSet? = null) :
    AppCompatTextView(context, attrs) {

    /**随机文本列表*/
    val randomTextList = mutableListOf("0", "1")

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRandomText(canvas)

        //降低绘制频率
        postDelay(100L) {
            invalidate()
        }
    }

    private fun randomText(): String {
        return randomTextList.randomOrNull() ?: ""
    }

    private fun randomAlpha(): Float {
        return nextFloat()
    }

    private fun measureTextWidth(text: String): Float {
        if (minWidth > 0) {
            return minWidth.toFloat()
        }
        return paint.textWidth("0")
        //return paint.textWidth(text)
    }

    private fun measureTextHeight(): Float {
        if (minHeight > 0) {
            return minHeight.toFloat()
        }
        return paint.textHeight()
    }

    /**开始绘制随机文本, 直至撑满控件的宽高*/
    private fun drawRandomText(canvas: Canvas) {
        val fromX = paddingLeft.toFloat()
        val toX = (measuredWidth - paddingRight).toFloat()
        val fromY = paddingTop.toFloat()
        val toY = (measuredHeight - paddingBottom).toFloat()
        var y = fromY
        while (y < toY) {
            y += measureTextHeight()
            drawLineText(canvas, fromX, toX, y - paint.descent())
        }
    }

    /**绘制一行文本, 直至撑满宽度*/
    private fun drawLineText(canvas: Canvas, fromX: Float, toX: Float, y: Float) {
        var x = fromX
        while (x < toX) {
            val text = randomText()
            val width = measureTextWidth(text)
            if (x + width > toX) {
                break
            }
            val textWidth = paint.textWidth(text)
            val color = paint.color
            paint.color = color.alphaRatio(randomAlpha())
            canvas.drawText(text, x + (width - textWidth) / 2, y, paint)
            paint.color = color
            x += width
        }
    }

}