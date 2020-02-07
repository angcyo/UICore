package com.angcyo.widget.layout.touch

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.annotation.CallSuper
import androidx.core.view.GestureDetectorCompat
import com.angcyo.widget.base.isTouchDown
import com.angcyo.widget.base.isTouchFinish
import kotlin.math.abs

/**
 * 支持 四方向手势
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2017/10/13 14:59
 */
open class TouchLayout(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    companion object {
        /**当滚动距离大于多少时, 视为滚动了*/
        const val scrollDistanceSlop = 0
        /**当Fling速度大于多少时, 视为Fling*/
        const val flingVelocitySlop = 0

        const val HANDLE_TOUCH_TYPE_DISPATCH = 1
        const val HANDLE_TOUCH_TYPE_INTERCEPT = 2
    }

    /**4个方向*/
    enum class ORIENTATION { LEFT, RIGHT, TOP, BOTTOM }

    /**采用什么方式, 处理touch事件 */
    var handleTouchType = HANDLE_TOUCH_TYPE_INTERCEPT

    val overScroller = OverScroller(context)

    var firstMotionEvent: MotionEvent? = null
    var secondMotionEvent: MotionEvent? = null

    var touchDownX = 0f
    var touchDownY = 0f

    var touchEventX = 0f
    var touchEventY = 0f

    /**是否处于长按状态下*/
    var isLongPress = false

    //<editor-fold desc="手势处理">

    var longPressRunnable = Runnable {
        performLongClick()
        isLongPress = true
    }

    /*用来检测手指滑动方向*/
    val orientationGestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                //L.e("call: onFling -> \n$e1 \n$e2 \n$velocityX $velocityY")

                firstMotionEvent = e1
                secondMotionEvent = e2

                val absX = abs(velocityX)
                val absY = abs(velocityY)

                if (absX > flingVelocitySlop || absY > flingVelocitySlop) {
                    if (absY > absX) {
                        //竖直方向的Fling操作
                        onFlingChange(
                            if (velocityY > 0) ORIENTATION.BOTTOM else ORIENTATION.TOP,
                            velocityX,
                            velocityY
                        )
                    } else if (absX > absY) {
                        //水平方向的Fling操作
                        onFlingChange(
                            if (velocityX > 0) ORIENTATION.RIGHT else ORIENTATION.LEFT,
                            velocityX,
                            velocityY
                        )
                    }
                }

                removeLongPressRunnable()

                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                //L.e("call: onScroll -> \n$e1 \n$e2 \n$distanceX $distanceY")
                firstMotionEvent = e1
                secondMotionEvent = e2

                val absX = Math.abs(distanceX)
                val absY = Math.abs(distanceY)

                if (absX > scrollDistanceSlop || absY > scrollDistanceSlop) {
                    if (absY > absX) {
                        //竖直方向的Scroll操作
                        onScrollChange(
                            if (distanceY > 0) ORIENTATION.TOP else ORIENTATION.BOTTOM,
                            distanceX,
                            distanceY
                        )
                    } else if (absX > absY) {
                        //水平方向的Scroll操作
                        onScrollChange(
                            if (distanceX > 0) ORIENTATION.LEFT else ORIENTATION.RIGHT,
                            distanceX,
                            distanceY
                        )
                    }
                }

                removeLongPressRunnable()

                return true
            }

            override fun onLongPress(e: MotionEvent?) {
                super.onLongPress(e)
            }
        }).apply {
            setIsLongpressEnabled(false)
        }

    @CallSuper
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            overScroller.abortAnimation()

            postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
        } else if (ev.isTouchFinish()) {
            removeLongPressRunnable()
            isLongPress = false
        }
        if (handleTouchType == HANDLE_TOUCH_TYPE_DISPATCH) {
            orientationGestureDetector.onTouchEvent(ev)
        }
        handleCommonTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    @CallSuper
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (handleTouchType == HANDLE_TOUCH_TYPE_INTERCEPT) {
            orientationGestureDetector.onTouchEvent(ev)
        }
        handleCommonTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        orientationGestureDetector.onTouchEvent(event)
        handleCommonTouchEvent(event)
        super.onTouchEvent(event) //防止onClickListener无效
        return true
    }

    open fun handleCommonTouchEvent(event: MotionEvent) {
        if (event.isTouchDown()) {
            touchDownX = event.x
            touchDownY = event.y
        }
        touchEventX = event.x
        touchEventY = event.y
    }

    open fun removeLongPressRunnable() {
        removeCallbacks(longPressRunnable)
    }

    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.currX, overScroller.currY)
            postInvalidate()
        }
    }

    //</editor-fold desc="手势处理">

    //<editor-fold desc="滚动操作">

    fun isVertical(orientation: ORIENTATION) =
        orientation == ORIENTATION.TOP || orientation == ORIENTATION.BOTTOM

    fun isHorizontal(orientation: ORIENTATION) =
        orientation == ORIENTATION.LEFT || orientation == ORIENTATION.RIGHT

    open fun startScrollY(dy: Int) {
        startScroll(0, dy)
    }

    open fun startScrollX(dx: Int) {
        startScroll(dx, 0)
    }

    open fun startScroll(dx: Int, dy: Int) {
        overScroller.abortAnimation()
        overScroller.startScroll(scrollX, scrollY, dx, dy, 300)
        postInvalidate()
    }

    /**开始滚动到某个位置*/
    open fun startScrollTo(startX: Int, toX: Int) {
        overScroller.abortAnimation()
        overScroller.startScroll(startX, scrollY, toX - startX, 0, 300)
        postInvalidate()
    }

    open fun startFlingY(velocityY: Int, maxDy: Int) {
        startFling(0, velocityY, 0, maxDy)
    }

    open fun startFlingX(velocityX: Int, maxDx: Int) {
        startFling(velocityX, 0, maxDx, 0)
    }

    open fun startFling(velocityX: Int, velocityY: Int, maxDx: Int, maxDy: Int) {
        overScroller.abortAnimation()
        overScroller.fling(
            scrollX,
            scrollY,
            velocityX,
            velocityY,
            0,
            maxDx,
            0,
            maxDy,
            measuredWidth,
            measuredHeight
        )
        postInvalidate()
    }

    //</editor-fold desc="滚动操作">

    //<editor-fold desc="回调处理">

    /**Fling操作的处理方法*/
    @Deprecated("不推荐使用")
    open fun onFlingChange(orientation: ORIENTATION, velocity: Float /*瞬时值*/) {
        //L.e("call: onFlingChange -> $orientation $velocity")
    }

    open fun onFlingChange(orientation: ORIENTATION, velocityX: Float, velocityY: Float) {
        onFlingChange(orientation, if (isVertical(orientation)) velocityY else velocityX)
    }

    /**Scroll操作的处理方法*/
    @Deprecated("不推荐使用")
    open fun onScrollChange(orientation: ORIENTATION, distance: Float /*瞬时值*/) {
    }

    open fun onScrollChange(orientation: ORIENTATION, distanceX: Float, distanceY: Float) {
        onScrollChange(orientation, if (isVertical(orientation)) distanceY else distanceX)
    }

    //</editor-fold desc="回调处理">
}