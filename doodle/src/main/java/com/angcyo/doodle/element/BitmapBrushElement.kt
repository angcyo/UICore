package com.angcyo.doodle.element

import android.graphics.*
import com.angcyo.doodle.brush.BaseBrush
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BitmapBrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.layer.BaseLayer

/**
 * 图片画刷元素
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
class BitmapBrushElement(val data: BitmapBrushElementData) : BaseBrushElement(data) {

    /**画刷的图片*/
    var brushBitmap: Bitmap? = null

    /**图片的绘制范围*/
    var brushBitmapRect: Rect = Rect()

    override fun onCreateElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {
        super.onCreateElement(manager, pointList)

        data.brushBitmap?.apply {
            //给原始图片上色
            val canvas = Canvas()
            brushBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            //用指定的方式填充位图的像素。
            val color = data.paintColor
            brushBitmap?.eraseColor(
                Color.rgb(Color.red(color), Color.green(color), Color.blue(color))
            )
            //用画布制定位图绘制
            canvas.setBitmap(brushBitmap)
            val paint = Paint()
            // 设置混合模式   （只在源图像和目标图像相交的地方绘制目标图像）
            //最常见的应用就是蒙板绘制，利用源图作为蒙板“抠出”目标图上的图像。
            //如果把这行代码注释掉这里生成的东西更加有意思
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            canvas.drawBitmap(this, 0f, 0f, paint)

            //src 代表需要绘制的区域
            //brushBitmapRect.set(0, 0, width / 4, height / 4)
            brushBitmapRect.set(0, 0, width, height)
        }
    }

    val _tempRectF = RectF()

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        data.pathSampleInfoList.forEach { info ->
            val afterAlpha = BaseBrush.selectPaintAlpha(info.width, data.paintWidth)

            _tempRectF.set(
                info.x - info.width,
                info.y - info.width,
                info.x + info.width,
                info.y + info.width
            )

            //paint.alpha = (a / 3.0f).toInt()
            //第一个Rect 代表要绘制的bitmap 区域，第二个 Rect 代表的是要将bitmap 绘制在屏幕的什么地方
            brushBitmap?.let { bitmap ->
                paint.alpha = afterAlpha//(afterAlpha / 3.0f).toInt()
                canvas.drawBitmap(bitmap, brushBitmapRect, _tempRectF, paint)
            }
        }
    }

    /*

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        var before: TouchPoint? = null
        data.brushPointList?.forEach {
            if (before != null) {
                draw(canvas, before!!, it)
            }
            before = it
        }
    }

    fun draw(canvas: Canvas, before: TouchPoint, after: TouchPoint) {
        //2点之间的距离
        val distance = after.distance
        val afterWidth = BaseBrush.selectPaintWidth(after.speed, data.paintWidth)
        val beforeWidth = BaseBrush.selectPaintWidth(before.speed, data.paintWidth)

        val afterAlpha = BaseBrush.selectPaintAlpha(afterWidth, data.paintWidth)
        val beforeAlpha = BaseBrush.selectPaintAlpha(beforeWidth, data.paintWidth)

        var factor = 2
        if (data.paintWidth < 6) {
            factor = 1
        } else if (data.paintWidth > 60) {
            factor = 3
        }
        val steps: Int = 1 + (distance / factor).toInt()
        val deltaX: Float = (after.eventX - before.eventX) / steps
        val deltaY: Float = (after.eventY - before.eventY) / steps
        val deltaW: Float = (afterWidth - beforeWidth) / steps
        val deltaA: Float = (beforeAlpha - afterAlpha) * 1f / steps

        var x: Float = before.eventX
        var y: Float = before.eventY
        var w: Float = beforeWidth
        var a: Float = beforeAlpha.toFloat()

        for (i in 0 until steps) {
            if (w < 1.5) w = 1.5f
            //根据点的信息计算出需要把bitmap绘制在什么地方
            _tempRectF.set(x - w / 2.0f, y - w / 2.0f, x + w / 2.0f, y + w / 2.0f)
            //每次到这里来的话，这个笔的透明度就会发生改变，但是呢，这个笔不用同一个的话，有点麻烦
            //我在这里做了个不是办法的办法，每次呢？我都从新new了一个新的笔，每次循环就new一个，内存就有很多的笔了
            //这里new 新的笔  我放到外面去做了
            //Paint newPaint = new Paint(paint);
            //当这里很小的时候，透明度就会很小，个人测试在3.0左右比较靠谱
            //paint.alpha = (a / 3.0f).toInt()
            //第一个Rect 代表要绘制的bitmap 区域，第二个 Rect 代表的是要将bitmap 绘制在屏幕的什么地方
            brushBitmap?.let { bitmap ->
                paint.alpha = (a / 3.0f).toInt()
                canvas.drawBitmap(bitmap, brushBitmapRect, _tempRectF, paint)
            }
            x += deltaX
            y += deltaY
            w += deltaW
            a += deltaA
        }
    }*/

}