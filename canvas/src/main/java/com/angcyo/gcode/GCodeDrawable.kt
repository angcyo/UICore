package com.angcyo.gcode

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */
class GCodeDrawable : Drawable {

    val picture: Picture

    val gCodeBound: RectF = RectF()

    private var _alpha = 255
    private var scaleX = 1f
    private var scaleY = 1f

    constructor(picture: Picture) : super() {
        this.picture = picture
    }

    constructor(gcodeLineDataList: List<GCodeLineData>) : super() {
        val paint = createPaint(Color.BLUE)
        this.picture = Picture().apply {
            var minX = 0f
            var maxX = 0f

            var minY = 0f
            var maxY = 0f

            gcodeLineDataList.forEach { line ->
                if (line.isGCodeMoveDirective()) {
                    val x = line.getGCodeX()
                    val y = line.getGCodeY()

                    minX = min(x, minX)
                    maxX = max(x, maxX)

                    minY = min(y, minY)
                    maxY = max(y, maxY)
                }
            }

            val bounds = RectF(minX, minY, maxX, maxY)
            gCodeBound.set(bounds)
            
            val canvas =
                beginRecording(bounds.width().ceil().toInt(), bounds.height().ceil().toInt())
            canvas.translate(-bounds.left, -bounds.top)

            var path: Path? = null
            gcodeLineDataList.forEach { line ->
                if (line.isGCodeMoveDirective()) {
                    val number = line.list.first().number.toInt()
                    if (number == 0) {
                        //G0
                        path = if (path == null) {
                            Path()
                        } else {
                            canvas.drawPath(path!!, paint)
                            Path()
                        }
                        path?.moveTo(line.getGCodeX(), line.getGCodeY())
                    } else if (number == 1) {
                        //G1
                        if (path == null) {
                            path = Path()
                        }
                        path?.lineTo(line.getGCodeX(), line.getGCodeY())
                    }
                }
            }
            path?.run { canvas.drawPath(this, paint) }
        }
    }

    override fun draw(canvas: Canvas) {
        save(canvas)
        canvas.clipRect(bounds)
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(scaleX, scaleY, 0f, 0f)
        canvas.drawPicture(picture)
        canvas.restore()
    }

    private fun save(canvas: Canvas) {
        if (alpha == 255 || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            canvas.save()
        } else {
            canvas.saveLayerAlpha(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), alpha)
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        scaleX = width.toFloat() / picture.width.toFloat()
        scaleY = height.toFloat() / picture.height.toFloat()
        super.setBounds(left, top, right, bottom)
    }

    override fun getIntrinsicWidth(): Int {
        return picture.width
    }

    override fun getIntrinsicHeight(): Int {
        return picture.height
    }

    override fun getAlpha(): Int {
        return _alpha
    }

    override fun setAlpha(alpha: Int) {
        this._alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        //
    }

    override fun getOpacity(): Int {
        // not sure, so be safe
        return PixelFormat.TRANSLUCENT
    }
}