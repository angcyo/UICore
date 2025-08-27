package com.angcyo.crop

import android.animation.ValueAnimator
import android.graphics.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.*
import androidx.core.view.GestureDetectorCompat
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import com.angcyo.library.gesture.RotationGestureDetector
import kotlin.math.max
import kotlin.math.roundToInt

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
    var marginingHorizontal: Int = 30 * dpi

    /**水平偏移*/
    var marginingVertical: Int = 30 * dpi

    /**动画时长*/
    var animatorDuration = 600L

    /**是否水平翻转图片*/
    var flipHorizontal = false

    /**是否垂直翻转图片*/
    var flipVertical = false

    /**图片旋转角度
     * [updateRotate]*/
    var rotate: Float = 0f

    /**要裁剪的图片*/
    var _bitmap: Bitmap? = null

    /**图片显示矩阵*/
    val _bitmapMatrix: Matrix = Matrix()

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

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
        get() = _bitmapMatrix.getScale()

    val maxWidth: Int
        get() = viewWidth - marginingHorizontal * 2

    val maxHeight: Int
        get() = viewHeight - marginingVertical * 2

    /**映射后的图片矩形坐标, 旋转缩放平移后*/
    val bitmapRectMap = RectF()
        get() {
            _bitmapMatrix.mapRect(field, _bitmapOriginRect.rectF)
            return field
        }

    /**图片仅旋转后的矩形, 相对于view左上角的矩形坐标*/
    val bitmapRotateRect = RectF()
        get() {
            field.set(_bitmapOriginRect)
            _rotate(field)
            return field
        }

    /**图片原始的矩形, 相对于View左上角的坐标*/
    val _bitmapOriginRect = Rect()

    //endregion ---property---

    //region ---touch---

    /**缩放手势*/
    val scaleDetector: ScaleGestureDetector = ScaleGestureDetector(
        view.context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (overlay.enableClipMoveMode) {
                    overlay.postScale(
                        detector.scaleFactor,
                        detector.scaleFactor,
                        midPntX,
                        midPntY,
                        false
                    )
                } else {
                    postScale(detector.scaleFactor, detector.scaleFactor, midPntX, midPntY, false)
                }
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
                val scale = 1.5f//currentScale * 1.2f
                if (overlay.enableClipMoveMode) {
                    overlay.postScale(scale, scale, e.x, e.y, true)
                } else {
                    postScale(scale, scale, e.x, e.y, true)
                }
                return super.onDoubleTap(e)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (overlay.enableClipMoveMode) {
                    overlay.postTranslate(-distanceX, -distanceY, false)
                } else {
                    postTranslate(-distanceX, -distanceY, false)
                }
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
        val action = event.actionMasked

        //down
        if (action == MotionEvent.ACTION_DOWN) {
            cancelAnimator()
        }

        //中点计算
        if (event.pointerCount > 1) {
            midPntX = (event.getX(0) + event.getX(1)) / 2
            midPntY = (event.getY(0) + event.getY(1)) / 2
        }

        if (overlay.enableClipMoveMode) {
            //移动剪切框的模式
            if (overlay.onTouchEvent(event)) {
                if (overlay._isTouchInClipRect) {
                    gestureDetector.onTouchEvent(event)
                    scaleDetector.onTouchEvent(event)
                    rotateDetector.onTouchEvent(event)
                }
            }
        } else {
            //剪切框自动缩放模式
            if (overlay.onTouchEvent(event)) {
                //被[overlay]处理
            } else {
                gestureDetector.onTouchEvent(event)
                scaleDetector.onTouchEvent(event)
                rotateDetector.onTouchEvent(event)

                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    if (!overlay.enableClipMoveMode) {
                        imageWrapCropBounds(false, true)
                    }
                }
            }
        }
        return true
    }

    /**[crop]*/
    @CallPoint
    fun onDraw(canvas: Canvas) {
        //移动到中心开始绘制
        _bitmap?.let { bitmap ->
            if (!bitmap.isRecycled) {
                canvas.withMatrix(_bitmapMatrix) {
                    canvas.withScale(
                        if (flipHorizontal) -1f else 1f,
                        if (flipVertical) -1f else 1f,
                        bitmap.width / 2f,
                        bitmap.height / 2f
                    ) {
                        canvas.drawBitmap(bitmap, 0f, 0f, paint)
                    }
                }
            }
        }
        if (canvas !is CropCanvas) {
            overlay.onDraw(canvas)
        }
    }

    /**调整图片到剪切矩形内
     * [moveToCenter] 是否将[bitmap]移动至剪切框的矩形中心
     * [anim] 是否需要动画*/
    fun imageWrapCropBounds(moveToCenter: Boolean = false, anim: Boolean = true) {
        val matrix = Matrix(_bitmapMatrix)

        val bitmapRect = bitmapRectMap
        val clipRect = overlay.clipRect

        if (bitmapRect.left > clipRect.left ||
            bitmapRect.top > clipRect.top ||
            bitmapRect.right < clipRect.right ||
            bitmapRect.bottom < clipRect.bottom
        ) {
            //需要调整
            val scaleX = clipRect.width() / bitmapRect.width()
            val scaleY = clipRect.height() / bitmapRect.height()
            val scale = max(scaleX, scaleY)
            if (scale > 1f) {
                //需要放大
                bitmapRect.scale(scale, scale, bitmapRect.left, bitmapRect.top)
                matrix.postScale(scale, scale, bitmapRect.left, bitmapRect.top)
            }

            //平移
            var dx = 0f
            var dy = 0f
            if (moveToCenter) {
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

    /**旋转矩形[rect]*/
    fun _rotate(rect: RectF) {
        val matrix = acquireTempMatrix()
        matrix.reset()
        matrix.setRotate(rotate, rect.centerX(), rect.centerY())
        matrix.mapRect(rect)
        matrix.release()
    }

    /**计算[from]能够完全显示的最佳矩形
     * [from] 未旋转之前的矩形*/
    fun calcBestRect(from: RectF): RectF {
        val fromRotateRect = RectF(from)
        _rotate(fromRotateRect)

        val result = RectF()

        val width = fromRotateRect.width()
        val height = fromRotateRect.height()

        val matrix = Matrix()
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val scaleWidth = maxWidth * 1f / width
        val scaleHeight = maxHeight * 1f / height
        val minScale = minOf(scaleWidth, scaleHeight)

        matrix.setTranslate(
            centerX - fromRotateRect.centerX(),
            centerY - fromRotateRect.centerY()
        )
        matrix.postScale(minScale, minScale, centerX.toFloat(), centerY.toFloat())

        val targetWidth = width * minScale
        val targetHeight = height * minScale

        //中心点, 最佳矩形, 能够撑满容纳图片的矩形
        result.set(
            centerX - targetWidth / 2,
            centerY - targetHeight / 2,
            centerX + targetWidth / 2,
            centerY + targetHeight / 2
        )

        return result
    }

    //endregion ---core---

    //region ---operate---

    /**移动图片显示到[rect]中, 完全贴合[rect]*/
    fun moveBitmapToRect(targetRect: RectF, anim: Boolean) {
        val targetMatrix = Matrix()
        val bitmapRotateRect = bitmapRotateRect

        targetMatrix.setRotate(rotate, bitmapRotateRect.centerX(), bitmapRotateRect.centerY())

        val scaleX = targetRect.width() / bitmapRotateRect.width()
        val scaleY = targetRect.height() / bitmapRotateRect.height()
        val scale = max(scaleX, scaleY)
        bitmapRotateRect.scale(scale, scale, bitmapRotateRect.left, bitmapRotateRect.top)
        targetMatrix.postScale(scale, scale, bitmapRotateRect.left, bitmapRotateRect.top)

        //平移
        val dx = targetRect.centerX() - bitmapRotateRect.centerX()
        val dy = targetRect.centerY() - bitmapRotateRect.centerY()

        targetMatrix.postTranslate(dx, dy)

        updateMatrix(targetMatrix, anim)
    }

    fun refresh() {
        //
        view.invalidate()
    }

    /**图片显示的最佳矩形*/
    val _bestRect = Rect()

    /**更新图片数据*/
    @CallPoint
    fun updateBitmap(bitmap: Bitmap) {
        if (!view.isLaidOut) {
            view.post {
                updateBitmap(bitmap)
            }
            return
        }
        _bitmap = bitmap
        _bitmapOriginRect.set(0, 0, bitmap.width, bitmap.height)
        updateRotate(rotate, false)
    }

    /**更新旋转角度 */
    fun updateRotate(rotate: Float, anim: Boolean = true) {
        this.rotate = rotate % 360

        val bestRect = calcBestRect(_bitmapOriginRect.rectF)
        _bestRect.setOut(true, bestRect)

        moveBitmapToRect(bestRect, anim)

        if (overlay.useBitmapRadio) {
            overlay.clipRatio = _bestRect.width() * 1f / _bestRect.height()//图片比例
        }

        //更新
        overlay.updateClipRatio(overlay.clipRatio, true)
    }

    /**重置到默认状态*/
    fun reset(anim: Boolean = true) {
        flipHorizontal = false
        flipVertical = false
        rotate = 0f
        updateRotate(rotate, anim)
    }

    var _animator: ValueAnimator? = null

    fun cancelAnimator() {
        _animator?.cancel()
        _animator = null
    }

    /**更新[matrix]*/
    fun updateMatrix(matrix: Matrix, anim: Boolean) {
        cancelAnimator()
        if (anim) {
            _animator = matrixAnimator(_bitmapMatrix, matrix, animatorDuration) {
                _bitmapMatrix.set(it)
                refresh()
            }
        } else {
            _bitmapMatrix.set(matrix)
            refresh()
        }
    }

    /**平移*/
    fun postTranslate(deltaX: Float, deltaY: Float, anim: Boolean) {
        if (deltaX != 0f || deltaY != 0f) {
            if (anim) {
                val endMatrix = Matrix(_bitmapMatrix)
                endMatrix.postTranslate(deltaX, deltaY)
                matrixAnimator(_bitmapMatrix, endMatrix) {
                    _bitmapMatrix.set(it)
                    refresh()
                }
            } else {
                _bitmapMatrix.postTranslate(deltaX, deltaY)
                refresh()
            }
        }
    }

    /**缩放*/
    fun postScale(sx: Float, sy: Float, px: Float, py: Float, anim: Boolean) {
        if (sx != 0f || sy != 0f) {
            if (anim) {
                val endMatrix = Matrix(_bitmapMatrix)
                endMatrix.postScale(sx, sy, px, py)
                matrixAnimator(_bitmapMatrix, endMatrix) {
                    _bitmapMatrix.set(it)
                    refresh()
                }
            } else {
                _bitmapMatrix.postScale(sx, sy, px, py)
                refresh()
            }
        }
    }

    /**将矩形反向映射到原始的图片中的位置*/
    fun mapOriginRect(src: Rect = overlay.clipRect, result: RectF = RectF()): RectF {
        result.set(src)
        val matrix = acquireTempMatrix()
        _bitmapMatrix.invert(matrix)
        matrix.mapRect(result)
        matrix.release()
        return result
    }

    /**开始剪裁
     * [onDraw]*/
    fun crop(): Bitmap? {
        val bitmap = _bitmap ?: return null
        val clipRect = overlay.clipRect
        if (clipRect.isEmpty) {
            return null
        }
        if (overlay.enableClipMoveMode) {
            //这种方式下的截图, 使用原始图片

            //获取剪切框在图片中未旋转时的对应的位置
            val targetRect = mapOriginRect(overlay.clipRect)
            val matrix = Matrix()
            matrix.setScale(
                if (flipHorizontal) -1f else 1f,
                if (flipVertical) -1f else 1f,
                bitmap.width / 2f,
                bitmap.height / 2f
            )
            matrix.postRotate(rotate, bitmap.width / 2f, bitmap.height / 2f)

            val left = max(0, targetRect.left.roundToInt())
            val top = max(0, targetRect.top.roundToInt())

            val width = minOf(targetRect.width().floorInt(), bitmap.width - left)
            val height = minOf(targetRect.height().floorInt(), bitmap.height - top)

            val originResult = Bitmap.createBitmap(bitmap, left, top, width, height, matrix, false)
            val result = Bitmap.createBitmap(
                originResult.width,
                originResult.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = CropCanvas(result)

            val rect = Rect(0, 0, originResult.width, originResult.height)
            canvas.withClip(overlay.updateClipPath(Path(), rect)) {//clip
                drawBitmap(originResult, 0f, 0f, paint)
            }
            originResult.recycle()
            return result
        } else {
            val result = Bitmap.createBitmap(
                clipRect.width(),
                clipRect.height(),
                bitmap.config
                    ?: Bitmap.Config.ARGB_8888//if (originBitmap.hasAlpha()) Bitmap.Config.ARGB_8888 else originBitmap.config
            )
            val canvas = CropCanvas(result)

            canvas.withTranslation(-clipRect.left.toFloat(), -clipRect.top.toFloat()) {
                withClip(overlay._clipPath) {
                    onDraw(this)
                }
            }
            return result
        }
    }

    //endregion ---operate---

    /**标识类*/
    class CropCanvas(bitmap: Bitmap) : Canvas(bitmap)
}