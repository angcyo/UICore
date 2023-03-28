package com.angcyo.canvas.render.core.component

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.annotation.CanvasOutsideCoordinate
import com.angcyo.canvas.render.core.*
import com.angcyo.canvas.render.data.ControlRendererInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.ex.dp

/**
 * 控制编辑操作基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/24
 */
abstract class BaseControl(val controlManager: CanvasControlManager) : ICanvasTouchListener,
    IComponent {

    /**当前控制产生的编辑矩阵, 最后要映射到[CanvasRenderProperty]中*/
    var controlMatrix: Matrix = Matrix()

    /**是否要处理控制事件*/
    var handleControl = false

    /**是否产生了控制*/
    var isControlHappen = false

    /**第一个手指的id, -1:无效的手指索引
     * [IllegalArgumentException]*/
    var firstTouchPointerId: Int = -1

    /**手指移动多少距离后, 才算作移动了*/
    var translateThreshold = 5 * dp

    override var isEnableComponent: Boolean = true

    /**当前正在控制的渲染器信息*/
    protected var controlRendererInfo: ControlRendererInfo? = null

    /**当前手势按下时的点位*/
    @CanvasOutsideCoordinate
    protected var touchDownPoint = PointF()

    @CanvasInsideCoordinate
    protected var touchDownPointInside = PointF()

    /**当前手势移动时的点位*/
    @CanvasOutsideCoordinate
    protected var touchMovePoint = PointF()

    @CanvasInsideCoordinate
    protected var touchMovePointInside = PointF()

    /**上一次移动的点位*/
    @CanvasOutsideCoordinate
    protected var lastTouchMovePoint = PointF()

    /**上一次移动的点位, inside*/
    @CanvasInsideCoordinate
    protected var lastTouchMovePointInside = PointF()

    val delegate: CanvasRenderDelegate
        get() = controlManager.delegate

    val smartAssistantComponent: SmartAssistantComponent
        get() = controlManager.smartAssistantComponent

    /**当前控制元素的边界*/
    val controlRendererBounds: RectF?
        get() = controlRendererInfo?.state?.renderProperty?.getRenderBounds()

    /**当前控制元素的边界*/
    val controlRendererAngle: Float?
        get() = controlRendererInfo?.state?.renderProperty?.angle

    override fun dispatchTouchEvent(event: MotionEvent) {
        /*val actionIndex = event.actionIndex //当前事件手指的索引, 第几个手指
        val id = event.getPointerId(actionIndex) //当前手指对应的id, 保存此id, 可以获取单独每个手指的对应数据
        val idIndex = event.findPointerIndex(id) //id对应的索引
        L.d("$actionIndex $id $idIndex ${event.findPointerIndex(firstTouchPointerId)}")*/
        //event.getX(idIndex)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                firstTouchPointerId = event.getPointerId(event.actionIndex)

                touchDownPoint.set(event.x, event.y)
                controlManager.delegate.renderViewBox.transformToInside(
                    touchDownPoint,
                    touchDownPointInside
                )

                lastTouchMovePoint.set(touchDownPoint)
                lastTouchMovePointInside.set(touchDownPointInside)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //controlMatrix.reset()
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean =
        isEnableComponent && handleControl

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val firstIndex = event.findPointerIndex(firstTouchPointerId)
                if (firstIndex >= 0) {
                    //只处理第一个按下的手指事件
                    touchMovePoint.set(event.getX(firstIndex), event.getY(firstIndex))
                    controlManager.delegate.renderViewBox.transformToInside(
                        touchMovePoint,
                        touchMovePointInside
                    )
                    onTouchMoveEvent(event)//wrap
                    lastTouchMovePoint.set(touchMovePoint)
                    lastTouchMovePointInside.set(touchMovePointInside)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isEnableComponent) {
                    endControl()
                }
            }
        }
        return true
    }

    /**手势移动事件, 自动处理[touchMovePoint] [lastTouchMovePoint]*/
    open fun onTouchMoveEvent(event: MotionEvent) {

    }

    /**当前手势移动了多少距离*/
    @CanvasOutsideCoordinate
    fun getTouchMoveDx() = touchMovePoint.x - lastTouchMovePoint.x

    @CanvasOutsideCoordinate
    fun getTouchMoveDy() = touchMovePoint.y - lastTouchMovePoint.y

    /**获取手势在画布内移动的距离*/
    @CanvasInsideCoordinate
    fun getTouchTranslateDxInside() = touchMovePointInside.x - touchDownPointInside.x

    @CanvasInsideCoordinate
    fun getTouchTranslateDyInside() = touchMovePointInside.y - touchDownPointInside.y

    /**结束控制之后, 是否需要应用改变*/
    fun isNeedApply() = isControlHappen && handleControl

    /**控制是否产生了*/
    fun updateControlHappen(happen: Boolean) {
        isControlHappen = happen
    }

    //region---core---

    private fun Reason.needToStack() = reason == Reason.REASON_CODE || reason == Reason.REASON_USER

    /**平移编辑*/
    protected fun applyTranslate(reason: Reason, delegate: CanvasRenderDelegate?) {
        controlRendererInfo?.let { controlInfo ->
            val controlRenderer = controlInfo.controlRenderer
            controlInfo.restoreState(controlRenderer, reason, Strategy.preview, delegate)

            delegate?.dispatchApplyControlMatrix(
                this,
                controlRenderer,
                controlMatrix,
                BaseControlPoint.CONTROL_TYPE_TRANSLATE
            )
            controlRenderer.applyTranslateMatrix(controlMatrix, reason, delegate)

            //自动加入回退栈
            if (reason.needToStack()) {
                controlManager.delegate.undoManager.addToStack(
                    controlInfo,
                    false,
                    Reason.user,
                    Strategy.normal
                )
            }
        }
    }

    /**旋转编辑*/
    protected fun applyRotate(reason: Reason, delegate: CanvasRenderDelegate?) {
        controlRendererInfo?.let { controlInfo ->
            val controlRenderer = controlInfo.controlRenderer
            controlInfo.restoreState(controlRenderer, reason, Strategy.preview, delegate)

            delegate?.dispatchApplyControlMatrix(
                this,
                controlRenderer,
                controlMatrix,
                BaseControlPoint.CONTROL_TYPE_ROTATE
            )
            controlRenderer.applyRotateMatrix(controlMatrix, reason, delegate)

            //自动加入回退栈
            if (reason.needToStack()) {
                controlManager.delegate.undoManager.addToStack(
                    controlInfo,
                    false,
                    Reason.user,
                    Strategy.normal
                )
            }
        }
    }

    /**缩放编辑*/
    protected fun applyScale(reason: Reason, delegate: CanvasRenderDelegate?) {
        controlRendererInfo?.let { controlInfo ->
            val controlRenderer = controlInfo.controlRenderer
            controlInfo.restoreState(controlRenderer, reason, Strategy.preview, delegate)

            delegate?.dispatchApplyControlMatrix(
                this,
                controlRenderer,
                controlMatrix,
                BaseControlPoint.CONTROL_TYPE_SCALE
            )
            controlRenderer.applyScaleMatrix(controlMatrix, reason, delegate)

            //自动加入回退栈
            if (reason.needToStack()) {
                controlManager.delegate.undoManager.addToStack(
                    controlInfo,
                    false,
                    Reason.user,
                    Strategy.normal
                )
            }
        }
    }

    //endregion---core---

    //region---操作---

    /**开始控制*/
    open fun startControl(render: BaseRenderer) {
        controlMatrix.reset()
        handleControl = true
        controlRendererInfo = ControlRendererInfo(render)
        if (delegate.controlManager.smartAssistantComponent.isEnableComponent) {
            delegate.controlManager.smartAssistantComponent.initSmartAssistant()
        }
        controlManager.delegate.dispatchControlHappen(this, false)
    }

    /**结束控制*/
    open fun endControl() {
        delegate.controlManager.smartAssistantComponent.clearSmartAssistant()
        controlManager.delegate.dispatchControlHappen(this, true)
        handleControl = false
        isControlHappen = false
        controlRendererInfo = null
    }

    //endregion---操作---
}