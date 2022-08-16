package com.angcyo.crop

import android.graphics.*
import android.view.MotionEvent
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.contains
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.saveLayerAlpha
import com.angcyo.library.ex.toRectF
import com.angcyo.library.gesture.RectScaleGestureHandler
import kotlin.math.absoluteValue

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

        /**剪切框类型, 椭圆*/
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

    /**线框笔*/
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    /**4个角的笔*/
    val cornersPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /**clip镂空笔*/
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

    /**剪切框改变后的回调
     * [scaleX] [scaleY] 缩放的比例
     * [pivotX] [pivotY] 改变的锚点*/
    var onClipRectChangedAction: (scaleX: Float, scaleY: Float, pivotX: Float, pivotY: Float) -> Unit =
        { _, _, _, _ ->

        }

    /**矩形缩放处理*/
    val rectScaleGestureHandler = RectScaleGestureHandler().apply {
        onRectScaleChangeAction = { rect, end ->
            clipRect.set(
                rect.left.toInt(),
                rect.top.toInt(),
                rect.right.toInt(),
                rect.bottom.toInt()
            )
            updateClipPath()
            if (end) {
                onClipRectChangedAction(
                    rect.width() / targetRect.width(),
                    rect.height() / targetRect.height(),
                    rectAnchorX,
                    rectAnchorY
                )
            } else {
                cropDelegate.refresh()
            }
        }
    }

    //region ---core---

    @CallPoint
    fun onTouchEvent(event: MotionEvent): Boolean {
        var handle = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _isTouchDown = true
                cropDelegate.view.removeCallbacks(_delayRefreshRunnable)
                cropDelegate.refresh()

                //
                rectScaleGestureHandler.initialize(
                    RectF(clipRect),
                    0f,
                    findTouchRectPosition(event.x.toInt(), event.y.toInt()),
                    clipRatio != null
                )
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                cropDelegate.view.postDelayed(_delayRefreshRunnable, 1_000)
            }
        }
        //
        handle = rectScaleGestureHandler.onTouchEvent(event.actionMasked, event.x, event.y)
        return handle
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
        cornersPaint.color = cornerColor
        canvas.drawPath(getLTPath(), cornersPaint)
        canvas.drawPath(getRTPath(), cornersPaint)
        canvas.drawPath(getRBPath(), cornersPaint)
        canvas.drawPath(getLBPath(), cornersPaint)

        canvas.restoreToCount(saveCount)
    }

    /**查找坐标落在矩形的什么位置上*/
    fun findTouchRectPosition(x: Int, y: Int): Int {

        //先判断是否在4个角上
        if (getLTPath(cornerWidth * 2).contains(x, y)) {
            return RectScaleGestureHandler.RECT_LT
        }
        if (getRTPath(cornerWidth * 2).contains(x, y)) {
            return RectScaleGestureHandler.RECT_RT
        }
        if (getRBPath(cornerWidth * 2).contains(x, y)) {
            return RectScaleGestureHandler.RECT_RB
        }
        if (getLBPath(cornerWidth * 2).contains(x, y)) {
            return RectScaleGestureHandler.RECT_LB
        }

        //再判断是否在4个边上
        if (x >= clipRect.left && x <= clipRect.right) {
            if ((y - clipRect.top).absoluteValue <= cornerWidth) {
                return RectScaleGestureHandler.RECT_TOP
            }
            if ((y - clipRect.bottom).absoluteValue <= cornerWidth) {
                return RectScaleGestureHandler.RECT_BOTTOM
            }
        }
        if (y >= clipRect.top && x <= clipRect.bottom) {
            if ((x - clipRect.left).absoluteValue <= cornerWidth) {
                return RectScaleGestureHandler.RECT_LEFT
            }
            if ((x - clipRect.right).absoluteValue <= cornerWidth) {
                return RectScaleGestureHandler.RECT_RIGHT
            }
        }

        return 0
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
                //椭圆
                clipPath.addOval(
                    clipRect.toRectF(),
                    Path.Direction.CW
                )
            }
            else -> {
                //圆角矩形
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