package com.angcyo.canvas.render.core.component

import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.OverScroller
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.size

/**
 * 画板快速滑动组件
 *
 * [CanvasTranslateComponent]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/16
 */
class CanvasFlingComponent(val delegate: CanvasRenderDelegate) : BaseTouchComponent() {

    /**速度检测*/
    private var velocityTracker: VelocityTracker? = null

    /**fling执行*/
    private val overScroller: OverScroller = OverScroller(delegate.view.context)

    /**是否发生过fling*/
    private var isFlingHappen = false

    /**最小的fling速率, 131*/
    private var minimumFlingVelocity: Int

    /**允许的最大速率, 21000*/
    private var maximumFlingVelocity: Int

    init {
        val configuration = ViewConfiguration.get(delegate.view.context)
        minimumFlingVelocity = 3000 //configuration.scaledMinimumFlingVelocity
        maximumFlingVelocity = configuration.scaledMaximumFlingVelocity
    }

    override fun dispatchTouchEvent(event: MotionEvent) {
        super.dispatchTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_DOWN ||
            event.actionMasked == MotionEvent.ACTION_POINTER_DOWN
        ) {
            overScroller.abortAnimation()
            isFlingHappen = false
        }
    }

    /**fling需要此方法配合使用*/
    @CallPoint
    fun onComputeScroll() {
        if (isEnable && isFlingHappen && overScroller.computeScrollOffset()) {
            //fling支持
            delegate.renderViewBox.translateTo(
                overScroller.currX.toFloat(),
                overScroller.currY.toFloat(),
                false
            )
            delegate.refresh()
        }
    }

    /**fling的速度*/
    private var lastVelocityX: Float? = null
    private var lastVelocityY: Float? = null

    override fun handleTouchEvent(event: MotionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastVelocityX = null
                lastVelocityY = null
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (isFlingIntent()) {
                    velocityTracker?.computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
                    val id = event.getPointerId(0)
                    val velocityX = velocityTracker?.getXVelocity(id)
                    val velocityY = velocityTracker?.getYVelocity(id)

                    if (velocityX.abs() > minimumFlingVelocity ||
                        velocityY.abs() > minimumFlingVelocity
                    ) {
                        isHandleTouch = true
                        lastVelocityX = velocityX
                        lastVelocityY = velocityY
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isHandleTouch) {
                    startFling(lastVelocityX!!, lastVelocityY!!)
                }
                velocityTracker?.clear()
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
    }

    /**开始fling操作*/
    private fun startFling(velocityX: Float, velocityY: Float) {
        L.d("快滑手势:velocityX:${velocityX} velocityY:${velocityY}")
        isFlingHappen = true
        val renderViewBox = delegate.renderViewBox
        overScroller.fling(
            renderViewBox.getTranslateX().toInt(),
            renderViewBox.getTranslateY().toInt(),
            velocityX.toInt(),
            velocityY.toInt(),
            Int.MIN_VALUE,
            Int.MAX_VALUE,
            Int.MIN_VALUE,
            Int.MAX_VALUE,
        )
        delegate.refresh()
    }

    /**是否是fling意图*/
    private fun isFlingIntent(): Boolean {
        if (_downPointList.size() < 2 || _movePointList.size() < 2) {
            return false
        }

        val dp1 = _downPointList[0]
        val dp2 = _downPointList[1]

        val mp1 = _movePointList[0]
        val mp2 = _movePointList[1]

        val dx1 = mp1.x - dp1.x
        val dy1 = mp1.y - dp1.y

        val dx2 = mp2.x - dp2.x
        val dy2 = mp2.y - dp2.y
        return (dx1 > dy1 && dx2 > dy2) || (dx1 < dy1 && dx2 < dy2)
    }
}