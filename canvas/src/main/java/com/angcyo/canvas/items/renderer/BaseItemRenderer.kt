package com.angcyo.canvas.items.renderer

import android.graphics.*
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.*
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.OperateHandler
import com.angcyo.canvas.core.renderer.BaseRenderer
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.PictureShapeItem
import com.angcyo.canvas.utils._tempPoint
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.mapRectF
import com.angcyo.library.L
import com.angcyo.library.ex.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseItemRenderer<T : BaseItem>(canvasView: ICanvasView) :
    BaseRenderer(canvasView), IItemRenderer<T> {

    //<editor-fold desc="属性">

    /**当前旋转的角度, 在[CanvasView]中处理此属性
     * [com.angcyo.canvas.CanvasView.onDraw]*/
    var rotate: Float = 0f

    /**当前宽度的缩放比例*/
    var scaleX: Float = 1f

    /**当前高度的缩放比例*/
    var scaleY: Float = 1f

    /**是否锁定了缩放比例
     * [com.angcyo.canvas.items.renderer.BaseItemRenderer.updateLockScaleRatio]*/
    var isLockScaleRatio: Boolean = true

    /**需要渲染的数据*/
    var _rendererItem: T? = null
        set(value) {
            val old = field
            field = value
            if (old != value) {
                onUpdateRendererItem(value, old)
            }
        }

    val _rotateBounds = emptyRectF()

    /**[_rotateRenderBounds]旋转后的坐标*/
    val _rotateRenderBounds = emptyRectF()

    /**[_visualBounds]旋转后的坐标*/
    val _visualRotateBounds = emptyRectF()

    /**[changeBounds]之前的bounds*/
    val changeBeforeBounds = emptyRectF()

    val _oldBounds = emptyRectF()

    //</editor-fold desc="属性">

    //<editor-fold desc="计算属性">

    val _tempMatrix = Matrix()
    val _rotateMatrix: Matrix = Matrix()

    //</editor-fold desc="计算属性">

    override fun getRendererItem(): T? = _rendererItem

    override fun getRotateBounds(): RectF = _rotateBounds

    override fun getRenderRotateBounds(): RectF = _rotateRenderBounds

    override fun getVisualRotateBounds(): RectF = _visualRotateBounds

    override fun changeBounds(reason: Reason, block: RectF.() -> Unit): Boolean {
        val bounds = getBounds()
        _oldBounds.set(bounds)
        changeBeforeBounds.set(bounds)

        //dsl
        bounds.block()

        //check
        if (!canChangeBounds(getBounds())) {
            bounds.set(_oldBounds)
            return false
        }

        //changed
        //宽高有变化后
        onChangeBoundsAfter(reason)
        L.d(
            buildString {
                appendLine(this@BaseItemRenderer)
                append(getRendererItem()?.simpleHash())
                appendLine(":Bounds改变:(w:${changeBeforeBounds.width()} h:${changeBeforeBounds.height()} -> w:${getBounds().width()} h:${getBounds().height()})")
                append("->${changeBeforeBounds}->${getBounds()}")
            }
        )
        itemBoundsChanged(reason, changeBeforeBounds)
        //notify
        if (reason.notify) {
            canvasView.dispatchItemBoundsChanged(this, reason, changeBeforeBounds)
        }
        canvasView.dispatchItemRenderUpdate(this)
        //invalidate
        refresh()

        return true
    }

    /**是否可以改变bound*/
    open fun canChangeBounds(toBounds: RectF): Boolean {
        return OperateHandler.canChangeBounds(this, toBounds)
    }

    /**Bounds改变后, 触发. 可以再次限制Bounds的大小
     * [changeBounds]*/
    open fun onChangeBoundsAfter(reason: Reason) {
        //getBounds().limitMinWidthHeight(100f, 100f, ADJUST_TYPE_LT)
        if (reason.flag == Reason.REASON_FLAG_BOUNDS || reason.flag == Reason.REASON_FLAG_ROTATE) {
            //旋转或者改变宽高后, 需要重新索引
            getRendererItem()?.engraveIndex = null
        }
    }

    override fun onUpdateRendererItem(item: T?, oldItem: T?) {
        super.onUpdateRendererItem(item, oldItem)
        _rendererItem = item
    }

    override fun updateLockScaleRatio(lock: Boolean) {
        isLockScaleRatio = lock
    }

    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldValue: Matrix
    ) {
        //super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldValue)
        changeBeforeBounds.set(getBounds())
        itemBoundsChanged(Reason(), changeBeforeBounds)
    }

    override fun preview(): Drawable? {
        return _rendererItem?.run {
            val renderBounds = getRenderBounds()
            renderBounds.withSave(0f, 0f, renderBounds.width(), renderBounds.height()) {
                val rotateBounds = getRenderRotateBounds()

                val width = rotateBounds.width()
                val height = rotateBounds.height()

                val result = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
                    withRotation(rotate, width / 2, height / 2) {
                        withTranslation(
                            width / 2 - renderBounds.width() / 2,
                            height / 2 - renderBounds.height() / 2
                        ) {
                            render(this)
                        }
                    }
                })
                result
            }
        }
    }

    /**当渲染的bounds改变后, 需要主动触发此方法, 用来更新主要的bounds和辅助的bounds*/
    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        canvasViewBox.calcItemRenderBounds(getBounds(), getRenderBounds())
        canvasViewBox.calcItemVisualBounds(getRenderBounds(), getVisualBounds())

        mapRotateRect(getBounds(), getRotateBounds())
        mapRotateRect(getRenderBounds(), getRenderRotateBounds())
        mapRotateRect(getVisualBounds(), getVisualRotateBounds())
    }

    fun getRotateMatrix(rotateCenterX: Float, rotateCenterY: Float): Matrix {
        _rotateMatrix.reset()
        _rotateMatrix.postRotate(rotate, rotateCenterX, rotateCenterY)
        return _rotateMatrix
    }

    override fun mapRotatePoint(
        rotateCenterX: Float,
        rotateCenterY: Float,
        point: PointF,
        result: PointF
    ): PointF {
        return getRotateMatrix(rotateCenterX, rotateCenterY).mapPoint(point, result)
    }

    override fun mapRotateRect(
        rotateCenterX: Float,
        rotateCenterY: Float,
        rect: RectF,
        result: RectF
    ): RectF {
        return getRotateMatrix(rotateCenterX, rotateCenterY).mapRectF(rect, result)
    }

    override fun mapRotateRect(rect: RectF, result: RectF): RectF {
        return mapRotateRect(rect.centerX(), rect.centerY(), rect, result)
    }

    override fun mapRotatePoint(point: PointF, result: PointF): PointF {
        val rendererBounds = getRenderBounds()
        return mapRotatePoint(rendererBounds.centerX(), rendererBounds.centerY(), point, result)
    }

    val rotatePath: Path = Path()

    override fun containsPoint(point: PointF): Boolean {
        var rendererBounds = getRenderBounds()

        val item = getRendererItem()
        if (item is PictureShapeItem && item.shapePath is LinePath) {
            //如果是线段, 方法矩形区域
            _tempRectF.set(rendererBounds)
            if ((item.shapePath as LinePath).orientation == LinearLayout.VERTICAL) {
                _tempRectF.inset(-10 * dp, 0f)
            } else {
                _tempRectF.inset(0f, -10 * dp)
            }
            rendererBounds = _tempRectF
        }

        return getRotateMatrix(rendererBounds.centerX(), rendererBounds.centerY()).run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(point.x.toInt(), point.y.toInt())
        }
    }

    override fun containsRect(rect: RectF): Boolean {
        val rendererBounds = getRenderBounds()
        return getRotateMatrix(rendererBounds.centerX(), rendererBounds.centerY()).run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(rect)
        }
    }

    override fun intersectRect(rect: RectF): Boolean {
        val rendererBounds = getRenderBounds()
        return getRotateMatrix(rendererBounds.centerX(), rendererBounds.centerY()).run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.intersect(rect)
        }
    }

    //<editor-fold desc="控制方法">

    /**平移元素
     * [distanceX] 横向需要移动的像素距离
     * [distanceY] 纵向需要移动的像素距离*/
    override fun translateBy(distanceX: Float, distanceY: Float) {
        L.i("平移by->dx:$distanceX dy:$distanceY")
        if (distanceX == 0f && distanceY == 0f) {
            return
        }
        changeBounds(Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_TRANSLATE)) {
            offset(distanceX, distanceY)
        }
    }

    /**缩放元素, 在元素左上角位置开始缩放
     * [scaleX] 横向需要移动的像素距离
     * [scaleY] 纵向需要移动的像素距离
     * [withCenter] 缩放缩放是否使用中点坐标, 默认是左上角
     * */
    override fun scaleBy(scaleX: Float, scaleY: Float, adjustType: Int) {
        L.i("缩放by->x:$scaleX y:$scaleY")
        _tempMatrix.reset()
        this.scaleX *= scaleX
        this.scaleY *= scaleY
        _adjustType = adjustType
        changeBounds(Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_BOUNDS)) {
            val x = when (adjustType) {
                ADJUST_TYPE_LT, ADJUST_TYPE_LB -> left
                ADJUST_TYPE_RB, ADJUST_TYPE_RT -> right
                else -> centerX()
            }
            val y = when (adjustType) {
                ADJUST_TYPE_LT, ADJUST_TYPE_RT -> top
                ADJUST_TYPE_RB, ADJUST_TYPE_LB -> bottom
                else -> centerY()
            }
            _tempPoint.set(x, y)
            mapRotatePoint(centerX(), centerY(), _tempPoint, _tempPoint)
            _tempMatrix.postScale(scaleX, scaleY, _tempPoint.x, _tempPoint.y)
            _tempMatrix.mapRect(this, this)
        }
    }

    override fun scaleTo(scaleX: Float, scaleY: Float, adjustType: Int) {
        L.i("缩放to->x:$scaleX y:$scaleY")
        _tempMatrix.reset()
        //复原矩形
        val bounds = getBounds()
        val oldScaleX = this.scaleX
        val oldScaleY = this.scaleY
        bounds.apply {
            val x = when (adjustType) {
                ADJUST_TYPE_LT, ADJUST_TYPE_LB -> left
                ADJUST_TYPE_RB, ADJUST_TYPE_RT -> right
                else -> centerX()
            }
            val y = when (adjustType) {
                ADJUST_TYPE_LT, ADJUST_TYPE_RT -> top
                ADJUST_TYPE_RB, ADJUST_TYPE_LB -> bottom
                else -> centerY()
            }
            _tempPoint.set(x, y)
            mapRotatePoint(centerX(), centerY(), _tempPoint, _tempPoint)
            _tempMatrix.postScale(1f / oldScaleX, 1f / oldScaleY, _tempPoint.x, _tempPoint.y)
            _tempMatrix.mapRect(this, this)
        }

        this.scaleX = scaleX
        this.scaleY = scaleY
        _adjustType = adjustType
        changeBounds(Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_BOUNDS)) {
            val x = when (adjustType) {
                ADJUST_TYPE_LT, ADJUST_TYPE_LB -> left
                ADJUST_TYPE_RB, ADJUST_TYPE_RT -> right
                else -> centerX()
            }
            val y = when (adjustType) {
                ADJUST_TYPE_LT, ADJUST_TYPE_RT -> top
                ADJUST_TYPE_RB, ADJUST_TYPE_LB -> bottom
                else -> centerY()
            }
            _tempPoint.set(x, y)
            mapRotatePoint(centerX(), centerY(), _tempPoint, _tempPoint)
            _tempMatrix.setScale(scaleX, scaleY, _tempPoint.x, _tempPoint.y)
            _tempMatrix.mapRect(this, this)
        }
    }

    /**旋转元素, 旋转操作不能用matrix, 不能将操作数据更新到bounds
     * [degrees] 需要旋转的角度
     * [rotateFlag] 旋转标识
     * [com.angcyo.canvas.items.renderer.IItemRenderer.ROTATE_FLAG_NORMAL]
     * [com.angcyo.canvas.items.renderer.IItemRenderer.ROTATE_FLAG_MOVE]
     * */
    override fun rotateBy(degrees: Float, rotateFlag: Int) {
        /*_tempMatrix.reset()
       getRendererBounds().apply {
           _tempMatrix.postRotate(degrees, centerX(), centerY())
           _tempMatrix.mapRect(this, this)
       }*/
        L.i("旋转by->$degrees")
        if (degrees == 0f) {
            return
        }
        val oldRotate = rotate
        changeBounds(Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_ROTATE)) {
            rotate += degrees
            rotate %= 360
        }
        itemRotateChanged(oldRotate, rotateFlag)
        L.i("旋转by->$degrees -> $rotate")
    }

    /**临时存储一下更新Bounds的方式*/
    var _adjustType: Int = ADJUST_TYPE_LT

    /**调整矩形的宽高, 支持旋转后的矩形*/
    override fun updateBounds(width: Float, height: Float, adjustType: Int) {
        L.i("调整宽高->w:${getBounds().width()}->$width h:${getBounds().height()}->${height} type:$adjustType")
        _adjustType = adjustType
        changeBounds(Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_BOUNDS)) {
            adjustSizeWithRotate(width, height, rotate, adjustType)
        }
    }

    /**更新笔的颜色*/
    open fun updatePaintColor(color: Int, strategy: Strategy = Strategy.normal) {
        val rendererItem = getRendererItem() ?: return
        val oldValue = rendererItem.paint.color
        if (oldValue == color) {
            return
        }
        rendererItem.paint.color = color
        rendererItem.updatePaint()

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updatePaintColor(oldValue, Strategy.undo)
                }

                override fun runRedo() {
                    updatePaintColor(color, Strategy.redo)
                }
            })
        }
        canvasView.dispatchItemRenderUpdate(this)
        //刷新
        refresh()
    }

    //</editor-fold desc="控制方法">

}