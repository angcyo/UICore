package com.angcyo.crop

import android.graphics.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.withClip
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import androidx.core.view.GestureDetectorCompat
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.*
import com.angcyo.library.gesture.RotationGestureDetector
import kotlin.math.max

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

    /**动画时长*/
    var animatorDuration = 600L

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

    val maxWidth: Int
        get() = viewWidth - marginingHorizontal * 2

    val maxHeight: Int
        get() = viewHeight - marginingVertical * 2

    /**图片当前显示的矩形*/
    val bitmapRectMap: RectF = RectF()
        get() {
            _matrix.mapRect(field, bitmapRect)
            return field
        }

    /**图片原始尺寸*/
    val bitmapRect: RectF = RectF()
        get() {
            field.set(0f, 0f, _bitmapWidth.toFloat(), _bitmapHeight.toFloat())
            return field
        }

    //endregion ---property---

    //region ---touch---

    /**缩放手势*/
    val scaleDetector: ScaleGestureDetector = ScaleGestureDetector(view.context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                postScale(detector.scaleFactor, detector.scaleFactor, midPntX, midPntY, false)
                return true
            }
        })

    /**旋转手势*/
    val rotateDetector: RotationGestureDetector =
        RotationGestureDetector(object : RotationGestureDetector.SimpleOnRotationGestureListener() {
            override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
                return true
            }
        })

    /**双击/移动手势*/
    val gestureDetector: GestureDetectorCompat =
        GestureDetectorCompat(view.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                /*zoomImageToPosition(
                    getDoubleTapTargetScale(),
                    e.x,
                    e.y,
                    GestureCropImageView.DOUBLE_TAP_ZOOM_DURATION.toLong()
                )*/
                val scale = 1.5f//currentScale * 1.2f
                postScale(scale, scale, e.x, e.y, true)
                return super.onDoubleTap(e)
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                postTranslate(-distanceX, -distanceY, false)
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

        if (overlay.onTouchEvent(event)) {
            //被[overlay]处理
        } else {
            if (event.pointerCount > 1) {
                midPntX = (event.getX(0) + event.getX(1)) / 2
                midPntY = (event.getY(0) + event.getY(1)) / 2
            }

            gestureDetector.onTouchEvent(event)
            scaleDetector.onTouchEvent(event)
            rotateDetector.onTouchEvent(event)

            val action = event.actionMasked
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                imageWrapCropBounds(false, true)
            }
        }
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
        if (canvas !is CropCanvas) {
            overlay.onDraw(canvas)
        }
    }

    /**调整图片到剪切矩形内
     * [center] 是否将[bitmap]移动至剪切框的矩形中心
     * [anim] 是否需要动画*/
    fun imageWrapCropBounds(center: Boolean = false, anim: Boolean = true) {
        val matrix = Matrix(_matrix)

        val bitmapRect = bitmapRectMap
        val clipRect = overlay.clipRect

        if (bitmapRect.left > clipRect.left ||
            bitmapRect.top > clipRect.top ||
            bitmapRect.right < clipRect.right ||
            bitmapRect.bottom < clipRect.bottom
        ) {
            //需要调整

            var dx = 0f
            var dy = 0f

            val scaleX = clipRect.width() / bitmapRect.width()
            val scaleY = clipRect.height() / bitmapRect.height()
            val scale = max(scaleX, scaleY)
            if (scale > 1f) {
                //需要放大
                bitmapRect.scale(scale, scale, bitmapRect.left, bitmapRect.top)
                matrix.postScale(scale, scale, bitmapRect.left, bitmapRect.top)
            }

            //平移
            if (center) {
                dx = clipRect.centerX() - bitmapRect.centerX()
                dy = clipRect.centerY() - bitmapRect.centerY()
            } else {
                if (bitmapRect.left > clipRect.left) {
                    dx = clipRect.left - bitmapRect.left
                }
                if (bitmapRect.top > clipRect.top) {
                    dy = clipRect.top - bitmapRect.top
                }
                if (bitmapRect.right < clipRect.right) {
                    dx = clipRect.right - bitmapRect.right
                }
                if (bitmapRect.bottom < clipRect.bottom) {
                    dy = clipRect.bottom - bitmapRect.bottom
                }
            }

            matrix.postTranslate(dx, dy)

            //
            updateMatrix(matrix, anim)
        }
    }

    /**将[bitmap]显示在指定矩形之内*/
    fun showInRect(rect: Rect, anim: Boolean = true) {
        val targetMatrix = Matrix()

        val bitmapRect = RectF(0f, 0f, _bitmapWidth.toFloat(), _bitmapHeight.toFloat())
        val targetRect = rect

        var dx = 0f
        var dy = 0f

        //缩放
        val scaleX = targetRect.width() / bitmapRect.width()
        val scaleY = targetRect.height() / bitmapRect.height()
        val scale = max(scaleX, scaleY)
        bitmapRect.scale(scale, scale, bitmapRect.left, bitmapRect.top)
        targetMatrix.postScale(scale, scale, bitmapRect.left, bitmapRect.top)

        //平移
        dx = targetRect.centerX() - bitmapRect.centerX()
        dy = targetRect.centerY() - bitmapRect.centerY()

        targetMatrix.postTranslate(dx, dy)

        //更新
        updateMatrix(targetMatrix, anim)
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

        //
        reset(false)

        //
        overlay.updateWithBitmap(bitmap)
    }

    /**重置到默认状态*/
    fun reset(anim: Boolean = true) {
        val matrix = Matrix()
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val scaleWidth = maxWidth * 1f / _bitmapWidth
        val scaleHeight = maxHeight * 1f / _bitmapHeight
        val minScale = minOf(scaleWidth, scaleHeight)

        matrix.setTranslate(centerX - _bitmapWidth / 2f, centerY - _bitmapHeight / 2f)
        matrix.postScale(minScale, minScale, centerX.toFloat(), centerY.toFloat())

        val targetWidth: Int = (_bitmapWidth * minScale).toInt()
        val targetHeight: Int = (_bitmapHeight * minScale).toInt()

        //中心点, 最佳矩形, 能够撑满容纳图片的矩形
        _bestRect.set(
            centerX - targetWidth / 2,
            centerY - targetHeight / 2,
            centerX + targetWidth / 2,
            centerY + targetHeight / 2
        )

        updateMatrix(matrix, anim)
    }

    /**更新[matrix]*/
    fun updateMatrix(matrix: Matrix, anim: Boolean) {
        if (anim) {
            matrixAnimator(_matrix, matrix, animatorDuration) {
                _matrix.set(it)
                refresh()
            }
        } else {
            _matrix.set(matrix)
            refresh()
        }
    }

    /**平移*/
    fun postTranslate(deltaX: Float, deltaY: Float, anim: Boolean) {
        if (deltaX != 0f || deltaY != 0f) {
            if (anim) {
                val endMatrix = Matrix(_matrix)
                endMatrix.postTranslate(deltaX, deltaY)
                matrixAnimator(_matrix, endMatrix) {
                    _matrix.set(it)
                    refresh()
                }
            } else {
                _matrix.postTranslate(deltaX, deltaY)
                refresh()
            }
        }
    }

    /**缩放*/
    fun postScale(sx: Float, sy: Float, px: Float, py: Float, anim: Boolean) {
        if (sx != 0f || sy != 0f) {
            if (anim) {
                val endMatrix = Matrix(_matrix)
                endMatrix.postScale(sx, sy, px, py)
                matrixAnimator(_matrix, endMatrix) {
                    _matrix.set(it)
                    refresh()
                }
            } else {
                _matrix.postScale(sx, sy, px, py)
                refresh()
            }
        }
    }

    /**剪裁*/
    fun crop(): Bitmap {
        val clipRect = overlay.clipRect
        val bitmap =
            Bitmap.createBitmap(clipRect.width(), clipRect.height(), Bitmap.Config.ARGB_8888)
        val canvas = CropCanvas(bitmap)

        canvas.withTranslation(-clipRect.left.toFloat(), -clipRect.top.toFloat()) {
            withClip(overlay._clipPath) {
                onDraw(this)
            }
        }
        return bitmap
    }

    /**连带更新*/
    fun onClipRectUpdateTo(rect: Rect, pivotX: Float, pivotY: Float) {
        val bestRect = _bestRect
        val rectScaleX = bestRect.width() * 1f / rect.width()
        val rectScaleY = bestRect.height() * 1f / rect.height()
        val bitmapMatrix = Matrix(_matrix)
        bitmapMatrix.postScale(rectScaleX, rectScaleY, pivotX, pivotY)
        //中点需要偏移的距离
        //val dx = bestRect.centerX() - rect.centerX()
        //val dy = bestRect.centerY() - rect.centerY()
        //bitmapMatrix.postTranslate(dx.toFloat(), dy.toFloat())
        updateMatrix(bitmapMatrix, false)
    }

    //endregion ---operate---

    /**标识类*/
    class CropCanvas(bitmap: Bitmap) : Canvas(bitmap)
}