package com.angcyo.drawable

import android.graphics.*
import androidx.core.graphics.withSave
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library.ex.computeBounds

/** 用来绘制[Path]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/27
 */
class PathListDrawable : AbsDslDrawable() {

    /**需要更新的属性*/
    var pathList = mutableListOf<Path>()

    /**需要的缩放倍数*/
    var scalePictureX = 1f
    var scalePictureY = 1f

    /**缩放控制点*/
    var scalePointX = 0f
    var scalePointY = 0f

    init {
        textPaint.style = Paint.Style.STROKE
        textPaint.strokeWidth = 1f
        textPaint.color = Color.BLACK
    }

    override fun draw(canvas: Canvas) {
        if (pathList.isEmpty()) {
            return
        }
        canvas.withSave {
            //translate(pathBounds.left, pathBounds.top)
            scale(scalePictureX, scalePictureY, scalePointX, scalePointY)
            for (path in pathList) {
                canvas.drawPath(path, textPaint)
            }
        }
    }

    override fun getIntrinsicWidth(): Int {
        return pathBounds.width().toInt()
    }

    override fun getIntrinsicHeight(): Int {
        return pathBounds.height().toInt()
    }

    val pathBounds = RectF()

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        _updateScale(width.toFloat(), height.toFloat())
        super.setBounds(left, top, right, bottom)
    }

    fun setPath(path: Path) {
        pathList.clear()
        pathList.add(path)
        updatePath()
    }

    fun setAllPath(list: List<Path>) {
        pathList.clear()
        pathList.addAll(list)
        updatePath()
    }

    /**更新数据, 触发刷新*/
    fun updatePath() {
        pathList.computeBounds(pathBounds)
        scalePictureX = pathBounds.left
        scalePointY = pathBounds.top
        _updateScale(bounds.width().toFloat(), bounds.height().toFloat())
        invalidateSelf()
    }

    fun _updateScale(width: Float, height: Float) {
        val pathWidth = intrinsicWidth
        val pathHeight = intrinsicHeight
        if (pathWidth == 0 || pathHeight == 0) {
            return
        }
        scalePictureX = width / pathWidth
        scalePictureY = height / pathHeight
    }

}