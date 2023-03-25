package com.angcyo.canvas.render.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.render.core.component.*
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.ex.mapPoint
import com.angcyo.library.ex.remove

/**
 * 4个点的控制器管理器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/22
 */
class CanvasControlManager(val delegate: CanvasRenderDelegate) : BaseTouchDispatch(),
    ICanvasRenderListener, ICanvasTouchListener, IRenderer {

    /**元素删除控制点*/
    var deleteControlPoint = DeleteControlPoint(this)

    /**元素旋转控制点*/
    var rotateControlPoint = RotateControlPoint(this)

    /**元素缩放控制点*/
    var scaleControlPoint = ScaleControlPoint(this)

    /**元素锁定控制点*/
    var lockControlPoint = LockControlPoint(this)

    /**元素平移操作*/
    var translateControl = TranslateRendererControl(this)

    /**当前按下的控制点*/
    val touchControlPoint: BaseControlPoint?
        get() = if (_interceptTarget is BaseControlPoint) _interceptTarget as BaseControlPoint else null

    override var renderFlags: Int = 0xff


    init {
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_VIEW)
            .remove(IRenderer.RENDERER_FLAG_ON_INSIDE)

        delegate.touchManager.touchListenerList.add(this)
        delegate.addCanvasRenderListener(this)

        //touch listener
        touchListenerList.add(deleteControlPoint)
        touchListenerList.add(rotateControlPoint)
        touchListenerList.add(scaleControlPoint)
        touchListenerList.add(lockControlPoint)
        touchListenerList.add(translateControl)
    }

    //region---core---

    override fun onRenderBoxMatrixUpdate(newMatrix: Matrix, reason: Reason, finish: Boolean) {
        if (delegate.selectorManager.isSelectorElement) {
            updateControlPointLocation()
        }
    }

    override fun onRendererPropertyChange(
        renderer: BaseRenderer,
        fromProperty: CanvasRenderProperty?,
        toProperty: CanvasRenderProperty?,
        reason: Reason
    ) {
        if (renderer == delegate.selectorManager.selectorComponent) {
            updateControlPointLocation()
        }
    }

    override fun onSelectorRendererChange(
        selectorComponent: CanvasSelectorComponent,
        from: List<BaseRenderer>,
        to: List<BaseRenderer>
    ) {
        val selectorRenderer = delegate.selectorManager.selectorComponent
        lockControlPoint.isLockScaleRatio = selectorRenderer.isLockScaleRatio
        scaleControlPoint.isLockScaleRatio = selectorRenderer.isLockScaleRatio
    }

    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        if (isEnableComponent) {
            if (delegate.selectorManager.isSelectorElement &&
                !delegate.selectorManager.isTouchInSelectorRenderer
            ) {
                deleteControlPoint.renderOnOutside(canvas, params)
                rotateControlPoint.renderOnOutside(canvas, params)
                scaleControlPoint.renderOnOutside(canvas, params)
                lockControlPoint.renderOnOutside(canvas, params)
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent) {
        dispatchTouchEventDelegate(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = haveInterceptTarget

    override fun onTouchEvent(event: MotionEvent): Boolean = haveInterceptTarget

    override fun onTouchEventIntercept(target: ICanvasTouchListener) {
        delegate.refresh()
    }

    //endregion---core---

    //region---操作---

    private val _tempPoint = PointF()
    private val _centerPoint = PointF()
    private val _tempRect = RectF()
    private val _tempMatrix = Matrix()

    /**更新控制点的位置*/
    fun updateControlPointLocation() {
        val selectorRenderer = delegate.selectorManager.selectorComponent

        //激活状态
        deleteControlPoint.isEnableComponent =
            selectorRenderer.isSupportControlPoint(deleteControlPoint.controlPointType)
        rotateControlPoint.isEnableComponent =
            selectorRenderer.isSupportControlPoint(rotateControlPoint.controlPointType)
        scaleControlPoint.isEnableComponent =
            selectorRenderer.isSupportControlPoint(scaleControlPoint.controlPointType)
        lockControlPoint.isEnableComponent =
            selectorRenderer.isSupportControlPoint(lockControlPoint.controlPointType)
        translateControl.isEnableComponent =
            selectorRenderer.isSupportControlPoint(BaseControlPoint.CONTROL_TYPE_TRANSLATE)

        //控制点位置
        selectorRenderer.renderProperty?.let {
            it.getRenderRect(_tempRect)
            updateDeleteControlPointLocation(_tempRect, it)
            updateRotateControlPointLocation(_tempRect, it)
            updateScaleControlPointLocation(_tempRect, it)
            updateLockControlPointLocation(_tempRect, it)
        }
    }

    /**删除控制点, 在左上角*/
    private fun updateDeleteControlPointLocation(bounds: RectF, property: CanvasRenderProperty) {
        if (!deleteControlPoint.isEnableComponent) return

        _centerPoint.set(bounds.left, bounds.top)
        delegate.renderViewBox.transformToOutside(_centerPoint)

        //先计算出控制点的中点
        val size = deleteControlPoint.controlPointSize / 2
        val offset = deleteControlPoint.controlPointOffset
        _centerPoint.x = _centerPoint.x - size - offset
        _centerPoint.y = _centerPoint.y - size - offset

        rotateAndSetBounds(
            property.angle,
            bounds.centerX(),
            bounds.centerY(),
            _centerPoint,
            deleteControlPoint
        )
    }

    /**旋转控制点, 在右上角*/
    private fun updateRotateControlPointLocation(bounds: RectF, property: CanvasRenderProperty) {
        if (!rotateControlPoint.isEnableComponent) return

        _centerPoint.set(bounds.right, bounds.top)
        delegate.renderViewBox.transformToOutside(_centerPoint)

        //先计算出控制点的中点
        val size = deleteControlPoint.controlPointSize / 2
        val offset = deleteControlPoint.controlPointOffset
        _centerPoint.x = _centerPoint.x + size + offset
        _centerPoint.y = _centerPoint.y - size - offset

        rotateAndSetBounds(
            property.angle,
            bounds.centerX(),
            bounds.centerY(),
            _centerPoint,
            rotateControlPoint
        )
    }

    /**缩放控制点, 在右下角*/
    private fun updateScaleControlPointLocation(bounds: RectF, property: CanvasRenderProperty) {
        if (!scaleControlPoint.isEnableComponent) return
        _centerPoint.set(bounds.right, bounds.bottom)
        delegate.renderViewBox.transformToOutside(_centerPoint)

        //先计算出控制点的中点
        val size = deleteControlPoint.controlPointSize / 2
        val offset = deleteControlPoint.controlPointOffset
        _centerPoint.x = _centerPoint.x + size + offset
        _centerPoint.y = _centerPoint.y + size + offset

        rotateAndSetBounds(
            property.angle,
            bounds.centerX(),
            bounds.centerY(),
            _centerPoint,
            scaleControlPoint
        )
    }

    /**锁定控制点, 在左下角*/
    private fun updateLockControlPointLocation(bounds: RectF, property: CanvasRenderProperty) {
        if (!lockControlPoint.isEnableComponent) return
        _centerPoint.set(bounds.left, bounds.bottom)
        delegate.renderViewBox.transformToOutside(_centerPoint)

        //先计算出控制点的中点
        val size = deleteControlPoint.controlPointSize / 2
        val offset = deleteControlPoint.controlPointOffset
        _centerPoint.x = _centerPoint.x - size - offset
        _centerPoint.y = _centerPoint.y + size + offset

        rotateAndSetBounds(
            property.angle,
            bounds.centerX(),
            bounds.centerY(),
            _centerPoint,
            lockControlPoint
        )
    }

    /**旋转[centerPoint], 并且设置[controlPoint]*/
    private fun rotateAndSetBounds(
        angle: Float,
        px: Float,
        py: Float,
        centerPoint: PointF,
        controlPoint: BaseControlPoint
    ) {
        _tempPoint.set(px, py)
        delegate.renderViewBox.transformToOutside(_tempPoint)
        _tempMatrix.reset()
        _tempMatrix.setRotate(angle, _tempPoint.x, _tempPoint.y)
        _tempMatrix.mapPoint(centerPoint)
        val size = controlPoint.controlPointSize / 2

        val left = centerPoint.x - size
        val top = centerPoint.y - size
        val right = centerPoint.x + size
        val bottom = centerPoint.y + size

        controlPoint.bounds.set(left, top, right, bottom)
    }

    /**锁定缩放比
     * [com.angcyo.canvas.render.core.component.CanvasSelectorComponent.updateLockScaleRatio]*/
    fun updateLockScaleRatio(lock: Boolean, reason: Reason, delegate: CanvasRenderDelegate?) {
        this.delegate.controlManager.lockControlPoint.isLockScaleRatio = lock
        this.delegate.controlManager.scaleControlPoint.isLockScaleRatio = lock
        this.delegate.selectorManager.selectorComponent.updateLockScaleRatio(lock, reason, delegate)
    }

    //endregion---操作---

}