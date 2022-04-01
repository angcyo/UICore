package com.angcyo.drawable

import android.graphics.*
import android.graphics.drawable.Drawable


/**
 * 棋盘背景
 *
 * https://github.com/angcyo/RRes
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/28
 */
class CheckerboardDrawable(builder: Builder) : Drawable() {

    companion object {

        /**创建棋盘背景*/
        fun create(): CheckerboardDrawable {
            return CheckerboardDrawable(Builder())
        }
    }

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val size: Int
    private val colorOdd: Int
    private val colorEven: Int

    init {
        size = builder.size
        colorOdd = builder.colorOdd
        colorEven = builder.colorEven
        configurePaint()
    }

    private fun configurePaint() {
        val bitmap = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888)
        val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bitmapPaint.style = Paint.Style.FILL
        val canvas = Canvas(bitmap)
        val rect = Rect(0, 0, size, size)

        bitmapPaint.color = colorOdd
        canvas.drawRect(rect, bitmapPaint)

        rect.offset(size, size)
        canvas.drawRect(rect, bitmapPaint)

        bitmapPaint.color = colorEven
        rect.offset(-size, 0)
        canvas.drawRect(rect, bitmapPaint)

        rect.offset(size, -size)
        canvas.drawRect(rect, bitmapPaint)

        paint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPaint(paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    class Builder {

        var size = 40
        var colorOdd = -0x3d3d3e
        var colorEven = -0xc0c0d

        fun size(size: Int): Builder {
            this.size = size
            return this
        }

        fun colorOdd(color: Int): Builder {
            colorOdd = color
            return this
        }

        fun colorEven(color: Int): Builder {
            colorEven = color
            return this
        }

        fun build(): CheckerboardDrawable {
            return CheckerboardDrawable(this)
        }
    }
}