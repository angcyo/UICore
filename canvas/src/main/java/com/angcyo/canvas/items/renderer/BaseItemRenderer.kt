package com.angcyo.canvas.items.renderer

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.BoundsOperateHandler
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.core.renderer.BaseRenderer
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.canvas.utils.limitMaxWidthHeight
import com.angcyo.library.L
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import com.angcyo.library.gesture.RectScaleGestureHandler

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

    /**是否锁定了缩放比例
     * [com.angcyo.canvas.items.renderer.BaseItemRenderer.updateLockScaleRatio]*/
    var isLockScaleRatio: Boolean = true

    /**需要渲染的数据*/
    var rendererItem: T? = null

    val _rotateBounds = emptyRectF()

    /**[_rotateRenderBounds]旋转后的坐标*/
    val _rotateRenderBounds = emptyRectF()

    /**[_visualBounds]旋转后的坐标*/
    val _visualRotateBounds = emptyRectF()

    /**[changeBoundsAction]之前的bounds*/
    val changeBeforeBounds = emptyRectF()

    //临时变量
    val _oldBounds = emptyRectF()

    //</editor-fold desc="属性">

    //<editor-fold desc="绘制相关属性">

    /**绘制的画笔属性*/
    var paint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 40 * dp //默认字体大小
    }

    //</editor-fold desc="绘制相关属性">

    //<editor-fold desc="计算属性">

    val _tempMatrix = Matrix()
    val _rotateMatrix = Matrix()
    val _tempPoint = PointF()

    //</editor-fold desc="计算属性">

    override fun getRendererRenderItem(): T? = rendererItem

    override fun setRendererRenderItem(item: T?) {
        val old = getRendererRenderItem()
        if (old != item) {
            rendererItem = item
            onUpdateRendererItem(item, old)
        }
    }

    /**[com.angcyo.canvas.items.renderer.BaseItemRenderer.itemBoundsChanged]*/
    override fun getRotateBounds(): RectF = _rotateBounds

    /**[com.angcyo.canvas.items.renderer.BaseItemRenderer.itemBoundsChanged]*/
    override fun getRenderRotateBounds(): RectF = _rotateRenderBounds

    /**[com.angcyo.canvas.items.renderer.BaseItemRenderer.itemBoundsChanged]*/
    override fun getVisualRotateBounds(): RectF = _visualRotateBounds

    override fun changeBoundsAction(reason: Reason, block: RectF.() -> Unit): Boolean {
        val bounds = getBounds()
        _oldBounds.set(bounds)
        changeBeforeBounds.set(bounds)

        //dsl
        bounds.block()

        //check
        if (reason.reason == Reason.REASON_USER && !canChangeBounds(getBounds())) {
            L.w("不允许修改Bounds->${getBounds()}")
            bounds.set(_oldBounds)
            return false
        }

        //changed
        //宽高有变化后
        onChangeBoundsAfter(reason)
        L.d(
            buildString {
                val endBounds = getBounds()
                appendLine(this@BaseItemRenderer)
                append(getRendererRenderItem()?.simpleHash())
                appendLine(":Bounds改变:(w:${changeBeforeBounds.width()} h:${changeBeforeBounds.height()} -> w:${endBounds.width()} h:${endBounds.height()})")
                append("Bounds->${changeBeforeBounds}->${endBounds}")
                //append("Anchor->${getBoundsScaleAnchor(changeBeforeBounds)}->${getBoundsScaleAnchor()}")
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

    /**当渲染的bounds改变后, 需要主动触发此方法, 用来更新主要的bounds和辅助的bounds*/
    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        canvasViewBox.calcItemRenderBounds(getBounds(), getRenderBounds())
        canvasViewBox.calcItemVisualBounds(getRenderBounds(), getVisualBounds())

        mapRotateRect(getBounds(), getRotateBounds())
        mapRotateRect(getRenderBounds(), getRenderRotateBounds())
        mapRotateRect(getVisualBounds(), getVisualRotateBounds())
    }

    /**是否可以改变bound*/
    open fun canChangeBounds(toBounds: RectF): Boolean {
        return BoundsOperateHandler.canChangeBounds(this, toBounds)
    }

    /**Bounds改变后, 触发. 可以再次限制Bounds的大小
     * [changeBoundsAction]*/
    open fun onChangeBoundsAfter(reason: Reason) {
        //getBounds().limitMinWidthHeight(100f, 100f, ADJUST_TYPE_LT)
        if (reason.flag == Reason.REASON_FLAG_BOUNDS || reason.flag == Reason.REASON_FLAG_ROTATE) {
            //旋转或者改变宽高后, 需要重新索引
            getRendererRenderItem()?.engraveIndex = null
        }
    }

    override fun onUpdateRendererItem(item: T?, oldItem: T?) {
        super.onUpdateRendererItem(item, oldItem)
        item?.let {
            requestRendererItemUpdate(oldItem)
        }
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
        return rendererItem?.run {
            val renderBounds = getRenderBounds()
            val renderWidth = renderBounds.width()
            val renderHeight = renderBounds.height()
            renderBounds.withSave(0f, 0f, renderWidth, renderHeight) {
                val rotateBounds = getRenderRotateBounds()

                val width = rotateBounds.width()
                val height = rotateBounds.height()

                val result = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
                    withRotation(rotate, width / 2, height / 2) {
                        withTranslation(
                            width / 2 - renderWidth / 2,
                            height / 2 - renderHeight / 2
                        ) {
                            render(this)
                        }
                    }
                })
                result
            }
        }
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

        val tempRect = acquireTempRectF()
        if (isLineShape()) {
            //如果是线段, 放大矩形高度区域
            tempRect.set(rendererBounds)
            tempRect.inset(0f, -10 * dp)
            rendererBounds = tempRect
        }

        val result = getRotateMatrix(rendererBounds.centerX(), rendererBounds.centerY()).run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(point.x.toInt(), point.y.toInt())
        }

        tempRect.release()

        return result
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

    val _anchorPoint = PointF()

    /**获取Bounds缩放时的锚点, 旋转后的坐标
     * 默认是左上角旋转后的坐标*/
    fun getBoundsScaleAnchor(bounds: RectF = getBounds()): PointF {
        _anchorPoint.set(bounds.left, bounds.top)
        _tempMatrix.reset()
        _tempMatrix.setRotate(rotate, bounds.centerX(), bounds.centerY())
        _tempMatrix.mapPoint(_anchorPoint, _anchorPoint)
        return _anchorPoint
    }

    //<editor-fold desc="scale control">

    /**开始拖拽缩放*/
    open fun onScaleControlStart(controlPoint: ScaleControlPoint) {

    }

    /**拖拽缩放结束*/
    open fun onScaleControlFinish(controlPoint: ScaleControlPoint, rect: RectF, end: Boolean) {
        L.i("拖动调整矩形:w:${rect.width()} h:${rect.height()} $rect $end")
        changeBoundsAction {
            set(rect)
        }
    }

    //</editor-fold desc="scale control">

    //<editor-fold desc="控制方法">

    /**平移元素
     * [distanceX] 横向需要移动的像素距离
     * [distanceY] 纵向需要移动的像素距离*/
    override fun translateBy(distanceX: Float, distanceY: Float) {
        L.i("平移by->dx:$distanceX dy:$distanceY")
        if (distanceX == 0f && distanceY == 0f) {
            return
        }
        changeBoundsAction(Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_TRANSLATE)) {
            offset(distanceX, distanceY)
        }
    }

    /**缩放元素, 在元素左上角位置开始缩放
     * [sx] 横向需要移动的像素距离
     * [sy] 纵向需要移动的像素距离
     * */
    override fun scaleBy(sx: Float, sy: Float, anchor: PointF) {
        L.i("缩放by->x:$sx y:$sy")
        val renderItem = getRendererRenderItem() ?: return
        val scaleX = renderItem.getItemScaleX(this) * sx
        val scaleY = renderItem.getItemScaleY(this) * sy
        scaleTo(scaleX, scaleY, anchor)
    }

    override fun scaleTo(scaleX: Float, scaleY: Float, anchor: PointF) {
        L.i("缩放to->x:$scaleX y:$scaleY")
        val bounds = getBounds()
        val newBounds = acquireTempRectF()
        RectScaleGestureHandler.rectScaleTo(
            bounds,
            newBounds,
            scaleX,
            scaleY,
            rotate,
            anchor.x,
            anchor.y
        )
        changeBoundsAction(Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_BOUNDS)) {
            set(newBounds)
        }
        newBounds.release()
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
        changeBoundsAction(Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_ROTATE)) {
            rotate += degrees
            rotate %= 360
        }
        itemRotateChanged(oldRotate, rotateFlag)
        L.i("旋转by->$degrees -> $rotate")
    }

    /**调整矩形的宽高, 支持旋转后的矩形*/
    override fun updateBounds(width: Float, height: Float, anchor: PointF) {
        L.i("调整宽高->w:${getBounds().width()}->$width h:${getBounds().height()}->${height} anchor:$anchor")
        changeBoundsAction(Reason(Reason.REASON_USER, true, Reason.REASON_FLAG_BOUNDS)) {
            //adjustSizeWithRotate(width, height, rotate, adjustType)
            RectScaleGestureHandler.rectUpdateTo(
                this,
                this,
                width,
                height,
                rotate,
                anchor.x,
                anchor.y
            )
        }
    }

    //</editor-fold desc="控制方法">

    //<editor-fold desc="绘制相关方法">

    /**当渲染的[drawable]改变后, 调用此方法, 更新bounds*/
    fun updateDrawableBounds(
        oldWidth: Float = getRendererRenderItem()?.itemWidth ?: 0f,
        oldHeight: Float = getRendererRenderItem()?.itemHeight ?: 0f
    ) {
        getRendererRenderItem()?.let { item ->
            var isUpdate = false

            val bounds = getBounds()

            val width = bounds.width()
            val height = bounds.height()

            val newWith = item.itemWidth
            val newHeight = item.itemHeight

            if (bounds.isNoSize() || oldWidth == 0f || oldHeight == 0f) {
                //首次更新bounds
                if (width != newWith || height != newHeight) {
                    isUpdate = true
                    updateBounds(newWith, newHeight, getBoundsScaleAnchor())
                }
            } else {
                //再次更新bounds
                val scaleWidth = width / oldWidth
                val scaleHeight = height / oldHeight
                if (scaleWidth == 1f && scaleHeight == 1f) {
                    if ((width >= height && newWith >= newHeight) || (width < height && newWith < newHeight)) {
                        //方向一致, 比如一致的宽图, 一致的长图

                        //限制目标大小到原来的大小
                        limitMaxWidthHeight(newWith, newHeight, oldWidth, oldHeight).apply {
                            isUpdate = true
                            updateBounds(this[0], this[1], getBoundsScaleAnchor())
                        }
                    } else {
                        //方向不一致, 使用新的宽高
                        isUpdate = true
                        updateBounds(newWith, newHeight, getBoundsScaleAnchor())
                    }
                } else {
                    //重新缩放当前的大小,达到和原来的缩放效果一致性
                    if ((width >= height && newWith >= newHeight) || (width < height && newWith < newHeight)) {
                        //方向一致, 比如一致的宽图, 一致的长图
                        isUpdate = true
                        updateBounds(
                            newWith * scaleWidth,
                            newHeight * scaleHeight,
                            getBoundsScaleAnchor()
                        )
                    } else {
                        //方向不一致
                        isUpdate = true
                        updateBounds(
                            newWith * scaleHeight,
                            newHeight * scaleWidth,
                            getBoundsScaleAnchor()
                        )
                    }
                }
            }

            //未被更新
            if (!isUpdate) {
                canvasView.dispatchItemRenderUpdate(this)
                refresh()
            }
        }
    }

    /**更新画笔绘制文本时的对齐方式*/
    open fun updatePaintAlign(align: Paint.Align, strategy: Strategy = Strategy.normal) {
        val oldValue = paint.textAlign
        if (oldValue == align) {
            return
        }
        paint.textAlign = align
        requestRendererItemUpdate()

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updatePaintAlign(oldValue ?: Paint.Align.LEFT, Strategy.undo)
                }

                override fun runRedo() {
                    updatePaintAlign(align, Strategy.redo)
                }
            })
        }
    }

    /**更新笔的颜色*/
    open fun updatePaintColor(color: Int, strategy: Strategy = Strategy.normal) {
        val oldValue = paint.color
        if (oldValue == color) {
            return
        }
        paint.color = color

        //更新需要绘制的元素
        requestRendererItemUpdate()

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
    }

    /**重写此方法, 更新需要渲染的元素.
     * 比如: 画笔颜色改变, 需要重绘文本; 图片更新; Drawable更新等
     * [fromItem] 之前的item, 如果有
     * */
    open fun requestRendererItemUpdate(fromItem: BaseItem? = null) {
        getRendererRenderItem()?.let { item ->
            val oldWidth = fromItem?.itemWidth ?: item.itemWidth
            val oldHeight = fromItem?.itemHeight ?: item.itemHeight
            item.updateItem(paint)
            updateDrawableBounds(oldWidth, oldHeight)
        }

        //刷新
        refresh()
        canvasView.dispatchItemRenderUpdate(this)
    }

    //</editor-fold desc="绘制相关方法">
}