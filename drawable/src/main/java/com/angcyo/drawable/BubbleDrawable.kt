package com.angcyo.drawable

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library.ex._color
import com.angcyo.library.ex.dotDegrees
import com.angcyo.library.ex.linearGradientShader
import kotlin.math.min

/**
 * 气泡Drawable
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/18
 */
class BubbleDrawable : AbsDslDrawable() {

    /**绘制的路径*/
    val bubblePath = Path()

    //启动顶部圆的矩形
    val bubbleRect = RectF()

    /**气泡左边圆起始点角度*/
    var leftStartAngle = 150f

    /**气泡右边圆起结束角度*/
    var rightEndAngle = 30f

    /**渐变色*/
    var bubbleColors = intArrayOf(_color(R.color.colorPrimary), _color(R.color.colorPrimaryDark))

    /**初始化气泡路径*/
    fun initBubblePath() {
        val width = drawRect.width()
        val height = drawRect.height()
        val size = min(width, height)
        val bubbleRadius = size / 3f
        val radio = 1.2f
        bubbleRect.set(
            drawRectF.centerX() - bubbleRadius,
            drawRectF.centerY() - bubbleRadius,
            drawRectF.centerX() + bubbleRadius,
            drawRectF.centerY() + bubbleRadius,
        )

        var degrees = leftStartAngle
        val p1 =
            dotDegrees(bubbleRadius, degrees, bubbleRect.centerX(), bubbleRect.centerY(), PointF())

        val startX = drawRectF.centerX()
        val startY = drawRectF.bottom
        bubblePath.rewind()        //移动到气泡底部尖坐标
        bubblePath.moveTo(startX, startY)

        //二阶曲线到左边的圆弧起点
        degrees = (leftStartAngle + 90) / 2
        val c1 = dotDegrees(
            bubbleRadius * radio,
            degrees,
            bubbleRect.centerX(),
            bubbleRect.centerY(),
            PointF()
        )
        bubblePath.quadTo(c1.x, c1.y, p1.x, p1.y)

        //圆弧到右边
        bubblePath.arcTo(bubbleRect, leftStartAngle, 360 - (leftStartAngle - rightEndAngle))

        //二阶曲线到终点
        degrees = (90 + rightEndAngle) / 2
        val c2 = dotDegrees(
            bubbleRadius * radio,
            degrees,
            bubbleRect.centerX(),
            bubbleRect.centerY(),
            PointF()
        )
        bubblePath.quadTo(c2.x, c2.y, startX, startY)
        bubblePath.close()
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        initBubblePath()
        textPaint.shader = linearGradientShader(bubbleRect, bubbleColors)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(bubblePath, textPaint)
    }

}