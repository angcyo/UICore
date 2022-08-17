package com.angcyo.crop

import android.graphics.*
import android.view.MotionEvent
import androidx.core.graphics.toRect
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.*
import com.angcyo.library.gesture.RectScaleGestureHandler
import kotlin.math.absoluteValue
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

    /**剪切的路径, 根据[clipRect]自动计算*/
    val _clipPath: Path = Path()

    /**圆角矩形的圆角半径
     * [clipType]
     * [TYPE_ROUND] 类型的圆角半径
     * */
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
        { _, _, pivotX, pivotY ->
            onClipRectChanged(clipRect, pivotX, pivotY)
        }

    /**矩形缩放处理*/
    val rectScaleGestureHandler = RectScaleGestureHandler().apply {
        val minSize = 2 * cornerHeight

        //限制裁剪框
        onLimitWidthAction = { newWidth, dx, dy ->
            clamp(newWidth, minSize, cropDelegate._bestRect.width().toFloat())
        }
        onLimitHeightAction = { newHeight, dx, dy ->
            clamp(newHeight, minSize, cropDelegate._bestRect.height().toFloat())
        }

        //剪切框改变后的回调
        onRectScaleChangeAction = { rect, end ->
            clipRect.set(
                rect.left.toInt(),
                rect.top.toInt(),
                rect.right.toInt(),
                rect.bottom.toInt()
            )
            updateClipPath()
            _tempRect.set(clipRect)
            if (_tempRect.isOverflowOf(cropDelegate.bitmapRectMap)) {
                cropDelegate.imageWrapCropBounds(false, false)
            }
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
                rectScaleGestureHandler.keepScaleRatio = clipRatio != null
                val rectPosition = findTouchRectPosition(event.x.toInt(), event.y.toInt())
                rectScaleGestureHandler.initialize(clipRect.toRectF(), 0f, rectPosition)
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
            canvas.drawPath(_clipPath, clipPaint)
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
        val factor = 5
        val refWidth = cornerWidth * factor

        //先判断是否在4个角上
        if ((x - clipRect.left).absoluteValue <= refWidth) {
            if ((y - clipRect.top).absoluteValue <= refWidth) {
                return RectScaleGestureHandler.RECT_LT
            }
            if ((y - clipRect.bottom).absoluteValue <= refWidth) {
                return RectScaleGestureHandler.RECT_LB
            }
            if (y >= clipRect.top && x <= clipRect.bottom) {
                return RectScaleGestureHandler.RECT_LEFT
            }
        }
        if ((x - clipRect.right).absoluteValue <= refWidth) {
            if ((y - clipRect.top).absoluteValue <= refWidth) {
                return RectScaleGestureHandler.RECT_RT
            }
            if ((y - clipRect.bottom).absoluteValue <= refWidth) {
                return RectScaleGestureHandler.RECT_RB
            }
            if (y >= clipRect.top && x <= clipRect.bottom) {
                return RectScaleGestureHandler.RECT_RIGHT
            }
        }

        //再判断是否在4个边上
        if (x >= clipRect.left && x <= clipRect.right) {
            if ((y - clipRect.top).absoluteValue <= refWidth) {
                return RectScaleGestureHandler.RECT_TOP
            }
            if ((y - clipRect.bottom).absoluteValue <= refWidth) {
                return RectScaleGestureHandler.RECT_BOTTOM
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

    /**使用图片比例*/
    fun setBitmapRatio() {
        clipRatio = cropDelegate._bestRect.width() * 1f / cropDelegate._bestRect.height()
    }

    /**更新提示框*/
    fun updateWithBitmap(bitmap: Bitmap) {
        if (clipRatio == null) {
            //默认使用图片比例
            setBitmapRatio()
        } else {
            updateClipRect()
            updateClipPath()
        }
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
        _clipPath.rewind()
        when (clipType) {
            TYPE_CIRCLE -> {
                //椭圆
                _clipPath.addOval(
                    clipRect.toRectF(),
                    Path.Direction.CW
                )
            }
            else -> {
                //圆角矩形
                _clipPath.addRoundRect(
                    clipRect.toRectF(),
                    roundRadius,
                    roundRadius,
                    Path.Direction.CW
                )
            }
        }
    }

    fun updateClipRect(matrix: Matrix) {
        val _matrix = Matrix()
        val rect = clipRect.toRectF()
        matrixAnimator(_matrix, matrix, cropDelegate.animatorDuration) {
            it.mapRect(_tempRectF, rect)
            clipRect.set(_tempRectF)
            updateClipPath()
        }
    }

    /**将剪切框改变到指定的矩形, 并缩放图片*/
    fun updateClipRect(
        endRect: Rect,
        pivotX: Float,
        pivotY: Float,
        endPivotX: Float,
        endPivotY: Float
    ) {
        val matrix = Matrix(cropDelegate._matrix)
        val startRect = Rect(clipRect)

        //锚点偏移的距离
        val dx = endPivotX - pivotX
        val dy = endPivotY - pivotY

        rectAnimatorFraction(startRect, endRect, cropDelegate.animatorDuration) { rect, fraction ->
            clipRect.set(rect)
            updateClipPath()

            val bitmapMatrix = Matrix(matrix)

            val rectScaleX = rect.width() * 1f / startRect.width()
            val rectScaleY = rect.height() * 1f / startRect.height()
            val scale = min(rectScaleX, rectScaleY)
            bitmapMatrix.postScale(scale, scale, pivotX, pivotY)

            bitmapMatrix.postTranslate(dx * fraction, dy * fraction)
            cropDelegate.updateMatrix(bitmapMatrix, false)
        }
    }

    /**剪切框改变后*/
    fun onClipRectChanged(rect: Rect, pivotX: Float, pivotY: Float) {
        val bestRect = cropDelegate._bestRect
        val rectMatrix = Matrix()
        //缩放剪切框到最佳位置
        val rectScaleX = bestRect.width() * 1f / rect.width()
        val rectScaleY = bestRect.height() * 1f / rect.height()
        val rectScale = min(rectScaleX, rectScaleY)

        rectMatrix.setScale(rectScale, rectScale, pivotX, pivotY)
        val endRect = rect.toRectF()
        rectMatrix.mapRect(endRect)

        //中点需要偏移的距离
        val dx = bestRect.centerX() - endRect.centerX()
        val dy = bestRect.centerY() - endRect.centerY()

        //rect
        rectMatrix.reset()
        rectMatrix.setTranslate(dx, dy)
        rectMatrix.mapRect(endRect)

        //锚点移动距离计算
        val point = RectScaleGestureHandler.getRectPositionPoint(
            endRect,
            rectScaleGestureHandler._rectPosition
        )
        val endPivotX = point?.x ?: pivotX
        val endPivotY = point?.y ?: pivotY

        //开始更新
        updateClipRect(endRect.toRect(), pivotX, pivotY, endPivotX, endPivotY)
    }

}