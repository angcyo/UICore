package com.angcyo.crop

import android.graphics.*
import android.view.MotionEvent
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.saveLayerAlpha
import com.angcyo.library.ex.toRectF
import kotlin.math.min

/**
 * 覆盖层
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class CropOverlay(val cropDelegate: CropDelegate) {

    companion object {
        /**剪切框类型, 圆角矩形*/
        const val TYPE_ROUND = 1

        /**剪切框类型, 圆*/
        const val TYPE_CIRCLE = 2
    }

    /**蒙层的颜色*/
    var overlayColor: Int = Color.parseColor("#80000000")

    /**矩形边框颜色*/
    var rectBorderColor: Int = Color.parseColor("#16000000")

    var rectBorderWidth: Float = 8 * dp

    /**4个触角的宽高*/
    var cornerWidth: Float = 3 * dp
    var cornerHeight: Float = 20 * dp

    /**操作块的颜色*/
    var cornerColor: Int = Color.WHITE

    /**剪切的矩形*/
    var clipRect: Rect = Rect()

    /**剪切的路径*/
    val clipPath: Path = Path()

    /**圆角矩形的圆角半径*/
    var roundRadius: Float = 0f

    /**剪切框类型*/
    var clipType: Int = TYPE_ROUND
        set(value) {
            field = value
            updateClipPath()
            cropDelegate.refresh()
        }

    /**剪切框的比例, null 表示原始比例*/
    var clipRatio: Float? = null
        set(value) {
            field = value
            updateClipRect()
            updateClipPath()
            cropDelegate.showInRect(clipRect, true)
        }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    /**手势是否按下*/
    var _isTouchDown: Boolean = false

    var _delayRefreshRunnable = Runnable {
        _isTouchDown = false
        cropDelegate.refresh()
    }

    //region ---core---

    @CallPoint
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _isTouchDown = true
                cropDelegate.view.removeCallbacks(_delayRefreshRunnable)
                cropDelegate.refresh()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                cropDelegate.view.postDelayed(_delayRefreshRunnable, 1_000)
            }
        }
        return true
    }

    @CallPoint
    fun onDraw(canvas: Canvas) {
        val saveCount = canvas.saveLayerAlpha(255)

        //镂空
        if (!_isTouchDown) {
            canvas.drawColor(overlayColor)
            canvas.drawPath(clipPath, clipPaint)
        }

        //灰色透明边框
        paint.style = Paint.Style.STROKE
        paint.color = rectBorderColor
        paint.strokeWidth = rectBorderWidth
        canvas.drawRect(clipRect, paint)

        //白边框
        paint.color = cornerColor
        paint.strokeWidth = 2 * dp
        canvas.drawRect(clipRect, paint)

        //网格线
        paint.strokeWidth = 1 * dp
        for (i in 0..2) {
            val y = clipRect.top + clipRect.height() / 3f * i
            canvas.drawLine(clipRect.left.toFloat(), y, clipRect.right.toFloat(), y, paint)
        }
        for (i in 0..2) {
            val x = clipRect.left + clipRect.width() / 3f * i
            canvas.drawLine(x, clipRect.top.toFloat(), x, clipRect.bottom.toFloat(), paint)
        }

        //4个角
        paint.style = Paint.Style.FILL
        canvas.drawPath(getLTPath(), paint)
        canvas.drawPath(getRTPath(), paint)
        canvas.drawPath(getRBPath(), paint)
        canvas.drawPath(getLBPath(), paint)

        canvas.restoreToCount(saveCount)
    }

    val _ltPath = Path()
    val _rtPath = Path()
    val _rbPath = Path()
    val _lbPath = Path()

    fun getLTPath(
        width: Float = cornerWidth,
        height: Float = cornerHeight,
        result: Path = _ltPath
    ): Path {
        val rect = clipRect
        val x = rect.left.toFloat()
        val y = rect.top.toFloat()
        result.rewind()

        result.moveTo(x, y)
        result.lineTo(x, y + height)
        result.lineTo(x - width, y + height)
        result.lineTo(x - width, y - width)
        result.lineTo(x + height, y - width)
        result.lineTo(x + height, y)
        result.close()

        return result
    }

    fun getLBPath(
        width: Float = cornerWidth,
        height: Float = cornerHeight,
        result: Path = _lbPath
    ): Path {
        val rect = clipRect
        val x = rect.left.toFloat()
        val y = rect.bottom.toFloat()
        result.rewind()

        result.moveTo(x, y)
        result.lineTo(x + height, y)
        result.lineTo(x + height, y + width)
        result.lineTo(x - width, y + width)
        result.lineTo(x - width, y - height)
        result.lineTo(x, y - height)
        result.close()

        return result
    }

    fun getRTPath(
        width: Float = cornerWidth,
        height: Float = cornerHeight,
        result: Path = _rtPath
    ): Path {
        val rect = clipRect
        val x = rect.right.toFloat()
        val y = rect.top.toFloat()
        result.rewind()

        result.moveTo(x, y)
        result.lineTo(x, y + height)
        result.lineTo(x + width, y + height)
        result.lineTo(x + width, y - width)
        result.lineTo(x - height, y - width)
        result.lineTo(x - height, y)
        result.close()

        return result
    }

    fun getRBPath(
        width: Float = cornerWidth,
        height: Float = cornerHeight,
        result: Path = _rbPath
    ): Path {
        val rect = clipRect
        val x = rect.right.toFloat()
        val y = rect.bottom.toFloat()
        result.rewind()

        result.moveTo(x, y)
        result.lineTo(x, y - height)
        result.lineTo(x + width, y - height)
        result.lineTo(x + width, y + width)
        result.lineTo(x - height, y + width)
        result.lineTo(x - height, y)
        result.close()

        return result
    }


    //endregion ---core---

    /**更新提示框*/
    fun updateWithBitmap(bitmap: Bitmap) {
        /*clipRect.set(
            -bitmap.width / 2, -bitmap.height / 2,
            bitmap.width / 2, bitmap.height / 2
        )*/
        updateClipRect()
        updateClipPath()
    }

    fun updateClipRect() {
        val ratio = clipRatio
        if (ratio == null) {
            //原始比例
            clipRect.set(cropDelegate._bestRect)
        } else {
            var width = cropDelegate._bestRect.width()
            var height = cropDelegate._bestRect.height()

            val s1 = width * 1f / height
            val s2 = ratio

            if (width <= height) {
                height = (height * s1 / s2).toInt()
            } else {
                width = (width * s2 / s1).toInt()
            }

            val maxWidth = cropDelegate.maxWidth
            val maxHeight = cropDelegate.maxHeight
            val scaleWidth = maxWidth * 1f / width
            val scaleHeight = maxHeight * 1f / height
            val minScale = minOf(scaleWidth, scaleHeight)

            width = (width * minScale).toInt()
            height = (height * minScale).toInt()

            val centerX = cropDelegate._bestRect.centerX()
            val centerY = cropDelegate._bestRect.centerY()

            clipRect.set(
                centerX - width / 2,
                centerY - height / 2,
                centerX + width / 2,
                centerY + height / 2
            )
        }
    }

    /**更新剪切矩形*/
    fun updateClipPath() {
        clipPath.rewind()
        when (clipType) {
            TYPE_CIRCLE -> {
                val radius = min(clipRect.width() / 2f, clipRect.height() / 2f)
                clipPath.addCircle(
                    clipRect.centerX().toFloat(),
                    clipRect.centerY().toFloat(),
                    radius,
                    Path.Direction.CW
                )
            }
            else -> {
                clipPath.addRoundRect(
                    clipRect.toRectF(),
                    roundRadius,
                    roundRadius,
                    Path.Direction.CW
                )
            }
        }
    }

}