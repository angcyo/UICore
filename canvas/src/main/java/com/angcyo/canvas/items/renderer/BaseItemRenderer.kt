package com.angcyo.canvas.items.renderer

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.ItemsOperateHandler
import com.angcyo.canvas.core.RenderParams
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.core.renderer.BaseRenderer
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.graphics.IEngraveProvider
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.library.L
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import com.angcyo.library.gesture.RectScaleGestureHandler
import com.angcyo.library.isInEditMode

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseItemRenderer<T : BaseItem>(canvasView: ICanvasView) :
    BaseRenderer(canvasView), IItemRenderer<T>, IEngraveProvider {

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

    /**是否需要限制渲染器的Bounds*/
    var needLimitRendererBounds: Boolean = true

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

    /**绘制时, 画布需要旋转的角度*/
    override fun getDrawRotate(): Float = rotate

    /**当前的渲染器是否超过可视化渲染区域, 超过区域的渲染器不会被渲染 */
    override fun isOutOfVisualRect(@Pixel visualRect: RectF): Boolean {
        return if (getBounds().isInitialize(Float.MIN_VALUE)) {
            getRenderRotateBounds().isOutOf(visualRect)
        } else false
    }

    /**[com.angcyo.canvas.items.renderer.BaseItemRenderer.renderItemBoundsChanged]*/
    override fun getRotateBounds(): RectF = _rotateBounds

    /**[com.angcyo.canvas.items.renderer.BaseItemRenderer.renderItemBoundsChanged]*/
    override fun getRenderRotateBounds(): RectF = _rotateRenderBounds

    /**[com.angcyo.canvas.items.renderer.BaseItemRenderer.renderItemBoundsChanged]*/
    override fun getVisualRotateBounds(): RectF = _visualRotateBounds

    override fun changeBoundsAction(reason: Reason, block: RectF.() -> Unit): Boolean {
        val bounds = getBounds()
        _oldBounds.set(bounds)
        changeBeforeBounds.set(bounds)

        //dsl
        bounds.block()

        if (reason.flag == Reason.REASON_FLAG_BOUNDS) {
            if (getBounds().isChanged(changeBeforeBounds)) {
                //大小/位置有变化
            } else {
                //大小没有变化
                return false
            }
        }

        if (reason.flag == Reason.REASON_FLAG_TRANSLATE) {
            if (getBounds().isTranslationChanged(changeBeforeBounds)) {
                //位置有变化
            } else {
                //位置没有变化
                return false
            }
        }

        //check
        if (reason.reason == Reason.REASON_USER && !canChangeBounds(getBounds())) {
            if (!isInEditMode) {
                L.w("不允许修改Bounds->${getBounds()}")
            }
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
        renderItemBoundsChanged(reason, changeBeforeBounds)
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
    override fun renderItemBoundsChanged(reason: Reason, oldBounds: RectF) {
        canvasViewBox.calcItemRenderBounds(getBounds(), getRenderBounds())
        canvasViewBox.calcItemVisualBounds(getRenderBounds(), getVisualBounds())

        mapRotateRect(getBounds(), getRotateBounds())
        mapRotateRect(getRenderBounds(), getRenderRotateBounds())
        mapRotateRect(getVisualBounds(), getVisualRotateBounds())
    }

    /**首次输出化渲染器的渲染范围*/
    open fun initRendererBounds(
        bounds: RectF? = null,
        reason: Reason = Reason(
            Reason.REASON_CODE,
            false,
            Reason.REASON_FLAG_BOUNDS
        ),
        block: RectF.() -> Unit = {}
    ) {
        changeBoundsAction(reason) {
            bounds?.let { set(bounds) }
            block()
        }
    }

    /**是否可以改变bound*/
    open fun canChangeBounds(toBounds: RectF): Boolean {
        return ItemsOperateHandler.canChangeBounds(this, toBounds)
    }

    /**Bounds改变后, 触发. 可以再次限制Bounds的大小
     * [changeBoundsAction]*/
    open fun onChangeBoundsAfter(reason: Reason) {
        if (needLimitRendererBounds) {
            ItemsOperateHandler.BOUNDS_LIMIT?.let { getBounds().limitInRect(it) }
        }
        /*if (reason.flag == Reason.REASON_FLAG_BOUNDS || reason.flag == Reason.REASON_FLAG_ROTATE) {

        }*/
    }

    override fun onUpdateRendererItem(item: T?, oldItem: T?) {
        super.onUpdateRendererItem(item, oldItem)
        //刷新
        refresh()
        canvasView.dispatchItemRenderUpdate(this)
    }

    override fun renderItemDataChanged(reason: Reason) {
        canvasView.dispatchItemDataChanged(this, reason)
    }

    override fun updateLockScaleRatio(lock: Boolean) {
        isLockScaleRatio = lock
    }

    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldMatrix: Matrix,
        isEnd: Boolean
    ) {
        //super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldMatrix)
        changeBeforeBounds.set(getBounds())
        renderItemBoundsChanged(Reason(), changeBeforeBounds)
    }

    //---

    val _flipMatrix = Matrix()
    val _flipRect = emptyRectF()

    override fun render(canvas: Canvas, renderParams: RenderParams) {
        rendererItem?.getDrawDrawable(renderParams)?.let { drawable ->
            val renderBounds = renderParams.itemRenderBounds ?: getRenderBounds()
            //需要处理矩形翻转的情况
            if (drawable is ScalePictureDrawable) {
                drawable.setBounds(
                    renderBounds.left.toInt(),
                    renderBounds.top.toInt(),
                    renderBounds.right.toInt(),
                    renderBounds.bottom.toInt()
                )
                drawable.draw(canvas)
            } else {
                //用于支持水平/垂直镜像绘制
                renderBounds.adjustFlipRect(_flipRect)
                var sx = 1f
                var sy = 1f
                if (getBounds().isFlipHorizontal) {
                    sx = -1f
                }
                if (getBounds().isFlipVertical) {
                    sy = -1f
                }
                _flipMatrix.reset()
                _flipMatrix.postScale(sx, sy, _flipRect.centerX(), _flipRect.centerY())//是否需要水平翻转
                canvas.withMatrix(_flipMatrix) {
                    drawable.setBounds(
                        _flipRect.left.toInt(),
                        _flipRect.top.toInt(),
                        _flipRect.right.toInt(),
                        _flipRect.bottom.toInt()
                    )
                    drawable.draw(canvas)
                }
            }
        }
    }

    override fun preview(renderParams: RenderParams): Drawable? {
        return rendererItem?.run {
            val renderBounds = getRenderBounds()
            val renderWidth = renderBounds.width()
            val renderHeight = renderBounds.height()
            renderBounds.withSave(0f, 0f, renderWidth, renderHeight) {
                val rotateBounds = getRenderRotateBounds()

                val width = rotateBounds.width().ceil()
                val height = rotateBounds.height().ceil()

                val result = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
                    withRotation(rotate, width / 2, height / 2) {
                        withTranslation(
                            width / 2 - renderWidth / 2,
                            height / 2 - renderHeight / 2
                        ) {
                            render(this, renderParams)
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
        if (isLock()) {
            return false
        }
        var rendererBounds = getRenderBounds()

        val tempRect = acquireTempRectF()
        if (isLineShape()) {
            //如果是线段, 放大矩形高度区域
            tempRect.set(rendererBounds)
            tempRect.inset(0f, -10 * dp / canvasViewBox.getScaleX()) //抵消坐标系的缩放
            rendererBounds = tempRect
        }

        var result = getRotateMatrix(rendererBounds.centerX(), rendererBounds.centerY()).run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(point.x.toInt(), point.y.toInt()) //在坐标15000px左右时, 此方法会实现
        }

        if (!result) {
            tempRect.set(point.x, point.y, point.x + 1, point.y + 1)
            result = containsRect(tempRect) //此时使用1像素的矩形,进行碰撞
        }

        tempRect.release()

        return result
    }

    override fun containsRect(rect: RectF): Boolean {
        if (isLock()) {
            return false
        }
        val rendererBounds = getRenderBounds()
        return getRotateMatrix(rendererBounds.centerX(), rendererBounds.centerY()).run {
            rotatePath.reset()
            rotatePath.addRect(rendererBounds, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(rect)
        }
    }

    override fun intersectRect(rect: RectF): Boolean {
        if (isLock()) {
            return false
        }
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
        L.i("拖动调整矩形开始:${getBounds()}")
    }

    /**拖拽缩放结束*/
    open fun onScaleControlFinish(controlPoint: ScaleControlPoint, rect: RectF, end: Boolean) {
        L.i("拖动调整矩形完成:w:${rect.width()} h:${rect.height()} $rect $end")
        /*//因为需要undo, 所以这里不能调整bounds
        changeBoundsAction {
            set(rect)
        }*/
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
        renderItemRotateChanged(oldRotate, rotate, rotateFlag)
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

    //<editor-fold desc="IEngraveProvider">

    override fun getEngraveBitmap(renderParams: RenderParams): Bitmap? =
        preview(renderParams)?.toBitmap()

    override fun getEngraveRenderer(): IItemRenderer<*>? = this

    override fun getEngraveBounds(): RectF = getBounds()

    override fun getEngraveRotateBounds(): RectF = getRotateBounds()

    //</editor-fold desc="IEngraveProvider">

    //<editor-fold desc="Api">

    /**复制数据*/
    open fun copyItemRendererData(strategy: Strategy): List<CanvasProjectItemBean>? = null

    /**获取相关的渲染器, 这样就支持1对N, 支持组合的功能*/
    open fun getDependRendererList(): List<BaseItemRenderer<*>> = listOf(this)

    /**在[bounds]内对齐*/
    open fun alignInBounds(
        bounds: RectF,
        align: Int = Gravity.CENTER,
        strategy: Strategy = Strategy.normal
    ) {
        val itemBounds = getBounds()
        val dx = when (align) {
            Gravity.CENTER -> bounds.centerX() - itemBounds.centerX()
            else -> 0f
        }
        val dy = when (align) {
            Gravity.CENTER -> bounds.centerY() - itemBounds.centerY()
            else -> 0f
        }
        canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            val unDx = -dx
            val unDy = -dy
            translateBy(unDx, unDy)
        }) {
            translateBy(dx, dy)
        }
    }

    //</editor-fold desc="Api">


}