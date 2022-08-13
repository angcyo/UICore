package com.angcyo.crop

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.withMatrix
import androidx.core.view.GestureDetectorCompat
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.RotationGestureDetector
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.getScale
import com.angcyo.library.ex.interceptParentTouchEvent
import com.angcyo.library.ex.matrixAnimator

/**
 * 裁剪
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class CropDelegate(val view: View) {

    /**覆盖层*/
    val overlay: CropOverlay = CropOverlay(this)

    /**水平边距*/
    var marginingHorizontal: Int = 50 * dpi

    /**水平偏移*/
    var marginingVertical: Int = 50 * dpi

    /**要裁剪的图片*/
    var _bitmap: Bitmap? = null

    /**矩阵*/
    val _matrix: Matrix = Matrix()

    //region ---property---

    val viewWidth: Int
        get() = view.measuredWidth

    val viewHeight: Int
        get() = view.measuredHeight

    val centerX: Int
        get() = viewWidth / 2

    val centerY: Int
        get() = viewHeight / 2

    /**当前缩放的比例*/
    val currentScale: Float
        get() = _matrix.getScale()

    //endregion ---property---

    //region ---touch---

    val scaleDetector: ScaleGestureDetector = ScaleGestureDetector(view.context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                postScale(detector.scaleFactor, midPntX, midPntY, false)
                return true
            }
        })
    val rotateDetector: RotationGestureDetector =
        RotationGestureDetector(object : RotationGestureDetector.SimpleOnRotationGestureListener() {
            override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
                return true
            }
        })
    val gestureDetector: GestureDetectorCompat =
        GestureDetectorCompat(view.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                /*zoomImageToPosition(
                    getDoubleTapTargetScale(),
                    e.x,
                    e.y,
                    GestureCropImageView.DOUBLE_TAP_ZOOM_DURATION.toLong()
                )*/
                val scale = 1.2f//currentScale * 1.2f
                postScale(scale, e.x, e.y, true)
                return super.onDoubleTap(e)
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                postTranslate(-distanceX, -distanceY)
                return true
            }
        })

    //endregion ---touch---

    //region ---core---

    private var midPntX = 0f
    private var midPntY = 0f

    @CallPoint
    fun onTouchEvent(event: MotionEvent): Boolean {
        view.interceptParentTouchEvent(event)

        if (event.pointerCount > 1) {
            midPntX = (event.getX(0) + event.getX(1)) / 2
            midPntY = (event.getY(0) + event.getY(1)) / 2
        }

        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)
        rotateDetector.onTouchEvent(event)

        overlay.onTouchEvent(event)
        return true
    }

    @CallPoint
    fun onDraw(canvas: Canvas) {
        //移动到中心开始绘制
        _bitmap?.let {
            canvas.withMatrix(_matrix) {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }

        overlay.onDraw(canvas)
    }

    //endregion ---core---

    //region ---operate---

    fun refresh() {
        //
        view.invalidate()
    }

    var _bitmapWidth: Int = -1
    var _bitmapHeight: Int = -1

    /**图片显示的最佳矩形*/
    val _bestRect = Rect()

    /**更新图片数据*/
    fun updateBitmap(bitmap: Bitmap) {
        if (!view.isLaidOut) {
            view.post {
                updateBitmap(bitmap)
            }
            return
        }
        _bitmap = bitmap
        _bitmapWidth = bitmap.width
        _bitmapHeight = bitmap.height

        val maxWidth = viewWidth - marginingHorizontal * 2
        val maxHeight = viewHeight - marginingVertical * 2

        val scaleWidth = maxWidth * 1f / _bitmapWidth
        val scaleHeight = maxHeight * 1f / _bitmapHeight
        val minScale = minOf(scaleWidth, scaleHeight)

        val targetWidth: Int = (_bitmapWidth * minScale).toInt()
        val targetHeight: Int = (_bitmapHeight * minScale).toInt()

        //中心点, 最佳矩形, 能够撑满容纳图片的矩形
        _bestRect.set(
            centerX - targetWidth / 2,
            centerY - targetHeight / 2,
            centerX + targetWidth / 2,
            centerY + targetHeight / 2
        )

        //
        _matrix.reset()
        _matrix.setTranslate(centerX - _bitmapWidth / 2f, centerY - _bitmapHeight / 2f)
        _matrix.postScale(minScale, minScale, centerX.toFloat(), centerY.toFloat())

        overlay.updateWithBitmap(bitmap)

        //
        updateMatrix()

        //
        refresh()
    }

    fun updateMatrix() {
        _matrix
    }

    /**平移*/
    fun postTranslate(deltaX: Float, deltaY: Float) {
        if (deltaX != 0f || deltaY != 0f) {
            _matrix.postTranslate(deltaX, deltaY)
            refresh()
        }
    }

    /**缩放*/
    fun postScale(deltaScale: Float, px: Float, py: Float, anim: Boolean) {
        if (deltaScale != 0f) {
            if (anim) {
                val endMatrix = Matrix(_matrix)
                endMatrix.postScale(deltaScale, deltaScale, px, py)
                matrixAnimator(_matrix, endMatrix) {
                    _matrix.set(it)
                    refresh()
                }
            } else {
                _matrix.postScale(deltaScale, deltaScale, px, py)
                refresh()
            }
        }
    }

    //endregion ---operate---

}