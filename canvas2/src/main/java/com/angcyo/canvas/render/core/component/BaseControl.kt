package com.angcyo.canvas.render.core.component

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import android.view.VelocityTracker
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.data.ControlRendererInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.L
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.canvas.annotation.CanvasOutsideCoordinate
import com.angcyo.library.canvas.core.ICanvasComponent
import com.angcyo.library.canvas.core.ICanvasTouchListener
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.component.Strategy
import com.angcyo.library.component.hawk.LibLpHawkKeys
import com.angcyo.library.ex.dp
import kotlin.math.absoluteValue

/**
 * 控制编辑操作基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/24
 */
abstract class BaseControl(val controlManager: CanvasControlManager) : ICanvasTouchListener,
    ICanvasComponent {

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

    /**智能推荐的最小速率*/
    var minimumSmartAssistantVelocity: Float = LibLpHawkKeys.minimumSmartAssistantVelocity

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

    /**速度检测*/
    var velocityTracker: VelocityTracker? = null

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
        if (isEnableComponent && smartAssistantComponent.isEnableComponent) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain()
            }
            velocityTracker?.addMovement(event)
        }

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

    /**当前的速率是否可以触发智能推荐
     * x/y速率都小于[minimumSmartAssistantVelocity]时, 触发智能推荐
     * */
    fun needSmartAssistantVelocity() = velocityTracker?.let {
        it.computeCurrentVelocity(1000)
        L.i("速率:x:${it.xVelocity} y:${it.yVelocity}")
        it.xVelocity.absoluteValue <= minimumSmartAssistantVelocity &&
                it.yVelocity.absoluteValue <= minimumSmartAssistantVelocity
    } ?: false

    /**清除智能提示绘制信息*/
    fun clearSmartAssistant() {
        smartAssistantComponent.clearSmartAssistant()
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

    /**直接将渲染器移动多少距离
     * [tx] [ty] 如果是手指触发的, 那么距离应该是移动的位置减去首次按下时的距离*/
    fun translate(tx: Float, ty: Float) {
        if (handleControl) {
            L.d("移动元素:tx:$tx ty:$ty")
            controlRendererInfo?.let {
                isControlHappen = true
                controlMatrix.setTranslate(tx, ty)

                applyTranslate(Reason.preview, controlManager.delegate)
            }
        }
    }

    /**移动元素, 在上一次的位置上进行增量移动*/
    fun translateBy(dx: Float, dy: Float) {
        if (handleControl) {
            L.d("移动元素By:dx:$dx dy:$dy")
            controlRendererInfo?.let {
                isControlHappen = true
                controlMatrix.postTranslate(dx, dy)

                applyTranslate(Reason.preview, controlManager.delegate)

                //移动到边缘时, 自动移动画布
                controlManager.delegate.autoTranslateCanvas(it.controlRenderer)
            }
        }
    }

    /**直接从原始位置(存档的位置), 旋转多少角度
     * 非旋转到多少度
     * [angle] 当前旋转了多少度*/
    fun rotate(angle: Float, px: Float, py: Float) {
        L.d("旋转元素:angle:$angle")
        controlRendererInfo?.let {
            isControlHappen = true
            controlMatrix.setRotate(angle, px, py)
            applyRotate(Reason.preview, controlManager.delegate)
        }
    }

    /**在当前可视的角度上, 旋转多少角度
     *  [angle] 需要旋转的角度*/
    fun rotateBy(angle: Float, px: Float, py: Float) {
        L.d("旋转元素:by:$angle")
        controlRendererInfo?.let {
            it.controlRenderer.renderProperty?.angle?.let {
                rotate(it + angle, px, py)
            }
        }
    }

    /**[rotateBy]*/
    fun rotateBy(angle: Float) {
        controlRendererInfo?.let {
            it.state.renderProperty?.getRenderCenter()?.let { centerPoint ->
                rotateBy(angle, centerPoint.x, centerPoint.y)
            }
        }
    }

    //endregion---操作---
}