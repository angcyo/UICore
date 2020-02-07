package com.angcyo.widget.layout.touch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.angcyo.library.ex.dpi
import com.angcyo.library.getStatusBarHeight
import com.angcyo.widget.R
import com.angcyo.widget.layout.RSoftInputLayout
import kotlin.math.max
import kotlin.math.min

/**
 * 支持滑动退出的父布局
 * Created by angcyo on 2016-12-18.
 */
abstract class SwipeBackLayout : TouchLayout {

    companion object {
        /**
         * 限制 value 在 min 和 max 之间
         */
        fun clamp(value: Int, min: Int, max: Int): Int {
            return max(min, min(max, value))
        }
    }

    var viewDragState = ViewDragHelper.STATE_IDLE
    var dragHelper: ViewDragHelper? = null

    /**
     * 正在滑动的view
     */
    var targetView: View? = null

    private var statusPaint: Paint? = null
    private var mListener: OnPanelSlideListener? = null
    private var mScreenWidth = 0
    private var mScreenHeight = 0

    private var drawDimStatusBar = false
    /**
     * 阴影的绘制区域
     */
    private var dimRect: Rect = Rect()
    private var dimWidth: Int = 20 * dpi //阴影的宽度 = 0

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**激活功能*/
    private var enableSwipeBack = true

    private var mIsLocked = false
    private var mIsLeftEdge = false
    private var mRawDownX = 0f
    /**
     * 侧滑被中断了, 需要恢复到原始状态
     */
    private var isCaptureAbort = false

    /**
     * The drag helper callback interface for the Left position
     */
    private val mLeftCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(
            child: View,
            pointerId: Int
        ): Boolean {
            isCaptureAbort = false
            if (!enableSwipeBack) {
                return false
            }
            if (child.left != 0 || child.translationX != 0f || viewDragState != ViewDragHelper.STATE_IDLE) {
                return false
            }
            mIsLeftEdge = dragHelper!!.isEdgeTouched(ViewDragHelper.EDGE_LEFT, pointerId)
            targetView = child
            return if (mIsLeftEdge || isForceIntercept) {
                canTryCaptureView(child)
            } else false
        }

        override fun clampViewPositionHorizontal(
            child: View,
            left: Int,
            dx: Int
        ): Int {
            return clamp(left, 0, mScreenWidth)
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return mScreenWidth
        }

        override fun onViewReleased(
            releasedChild: View,
            xvel: Float,
            yvel: Float
        ) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val left = releasedChild.left
            var settleLeft = 0
            val leftThreshold = minCloseWidth.toInt() //当滑动的距离达到1/5时, 判断为滑动退出
            val isVerticalSwiping = Math.abs(yvel) > 5f //垂直滑动的距离大于5
            if (xvel > 0) { //快速滑动的时候, 滑动的速度大于 5, 并且垂直滑动的速度小于 5, 也视为滑动删除
                if (Math.abs(xvel) > 5f && !isVerticalSwiping) {
                    settleLeft = mScreenWidth
                } else if (left > leftThreshold) {
                    settleLeft = mScreenWidth
                }
            } else if (xvel == 0f) {
                if (left > leftThreshold) {
                    settleLeft = mScreenWidth
                }
            }
            dragHelper!!.settleCapturedViewAt(settleLeft, releasedChild.top)
            postInvalidateOnAnimation()
        }

        private val minCloseWidth: Float
            private get() = measuredWidth * 0.2f

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            val percent = 1f - left.toFloat() / mScreenWidth.toFloat()
            onSlideChange(percent)
            if (mListener != null) {
                mListener!!.onSlideChange(percent)
            }
            postInvalidateOnAnimation()
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            if (mListener != null) {
                mListener!!.onStateChanged(state)
            }
            this@SwipeBackLayout.onViewDragStateChanged(state)
            when (state) {
                ViewDragHelper.STATE_IDLE -> {
                    onStateIdle()
                    if (isCaptureAbort) {
                        onRequestOpened()
                    } else { //滚动结束
                        if (targetView!!.left < minCloseWidth) { // State Open
                            onRequestOpened()
                            if (mListener != null) {
                                mListener!!.onRequestOpened()
                            }
                        } else { // State Closed
                            onRequestClose()
                            if (mListener != null) {
                                mListener!!.onRequestClose()
                            }
                        }
                        targetView = null
                    }
                    isCaptureAbort = false
                }
                ViewDragHelper.STATE_DRAGGING ->  //开始滚动
                    onStateDragging()
                ViewDragHelper.STATE_SETTLING -> {
                }
                else -> {
                }
            }
        }
    }

    constructor(context: Context?) : super(context!!, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    )

    fun restoreCaptureView() {
        if (targetView != null) {
            isCaptureAbort = true
            dragHelper!!.abort()
        }
    }

    /**
     * 是否激活滑动删除
     */
    fun setEnableSwipeBack(enableSwipeBack: Boolean) {
        this.enableSwipeBack = enableSwipeBack
    }

    /**
     * @return true 表示可以抓起 child
     */
    abstract fun canTryCaptureView(child: View): Boolean

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setWillNotDraw(false)
        dragHelper = ViewDragHelper.create(this, 0.5f, mLeftCallback)
        dragHelper?.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT) //设置只支持左边缘滑动返回.
        dimWidth = (20 * resources.displayMetrics.density).toInt()
        //状态栏遮罩
        setDimStatusBar(drawDimStatusBar)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dragHelper!!.abort()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawSwipeLine(canvas)
        drawDimStatusBar(canvas)
    }

    /**
     * 绘制侧滑时, 左边的渐变线
     */
    open fun drawSwipeLine(canvas: Canvas) {
        if (targetView != null && targetView!!.left != measuredWidth) {
            dimRect[targetView!!.left - dimWidth, 0, targetView!!.left] = measuredHeight
            paint.alpha = (255 * (1 - targetView!!.left * 1f / measuredWidth)).toInt()
            paint.shader = LinearGradient(
                dimRect.left.toFloat(),
                0f,
                dimRect.right.toFloat(),
                0f,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.parseColor("#40000000")
                ),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(dimRect, paint)
        }
    }

    /**
     * 绘制状态栏遮罩
     */
    open fun drawDimStatusBar(canvas: Canvas) {
        if (drawDimStatusBar) {
            canvas.drawRect(
                0f, 0f,
                measuredWidth.toFloat(),
                getStatusBarHeight().toFloat(),
                statusPaint!!
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mScreenWidth = w
        mScreenHeight = h
    }

    fun setOnPanelSlideListener(listener: OnPanelSlideListener?) {
        mListener = listener
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        super.onInterceptTouchEvent(ev)
        var interceptForDrag = false
        if (mIsLocked) {
            return false
        }
        //canDragFromEdge(ev);
        if (ev.action == MotionEvent.ACTION_DOWN) {
            mRawDownX = ev.rawX
        } else if (ev.action == MotionEvent.ACTION_UP) {
            mRawDownX = -1f
        }
        // Fix for pull request #13 and issue #12
        interceptForDrag = try {
            dragHelper!!.shouldInterceptTouchEvent(ev)
        } catch (e: Exception) {
            false
        }
        return interceptForDrag && !mIsLocked
    }

    //        if (Build.USER.contains("nubia") && mRawDownX > 0 && mRawDownX <= 100f) {
//            return true;
//        }
    val isForceIntercept: Boolean
        get() {
//        if (Build.USER.contains("nubia") && mRawDownX > 0 && mRawDownX <= 100f) {
//            return true;
//        }
            val density = resources.displayMetrics.density
            return mRawDownX > 0 && mRawDownX <= 26f * density
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if (mIsLocked) {
            return false
        }
        try {
            dragHelper!!.processTouchEvent(event)
        } catch (e: IllegalArgumentException) {
            return false
        }
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (dragHelper!!.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**
     * Lock this sliding panel to ignore touch inputs.
     */
    fun lock() {
        if (dragHelper != null) {
            dragHelper!!.abort()
        }
        mIsLocked = true
    }

    /**
     * Unlock this sliding panel to listen to touch inputs.
     */
    fun unlock() {
        dragHelper!!.abort()
        mIsLocked = false
    }

    private fun canDragFromEdge(ev: MotionEvent): Boolean {
        val x = ev.x
        val y = ev.y
        return x < 0.18f * width
    }

    /**
     * 需要返回了
     */
    open fun onRequestClose() {}

    /**
     * 需要打开
     */
    open fun onRequestOpened() {}

    /**
     * @param percent 已经滑动的距离比例 (1-0取值)
     */
    open fun onSlideChange(percent: Float) {}

    /**
     * 开始拖拽的时候回调
     */
    open fun onStateDragging() {}

    /**
     * 结束拖拽的时候回调
     */
    open fun onStateIdle() {}

    /**
     * 状态栏是否变暗, 5.0以上有效
     */
    fun setDimStatusBar(dim: Boolean) {
        setDimStatusBar(dim, ContextCompat.getColor(context, R.color.lib_status_bar_dim))
    }

    fun setDimStatusBar(dim: Boolean, color: Int) {
        val layoutFullScreen = RSoftInputLayout.isLayoutFullScreen(context)
        if (dim) {
            if (layoutFullScreen) {
                drawDimStatusBar = true
                if (statusPaint == null) {
                    statusPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                }
                statusPaint!!.color = color
            }
        } else {
            drawDimStatusBar = false
        }
    }

    /**
     * 获取屏幕方向
     *
     * @see android.content.res.Configuration.ORIENTATION_LANDSCAPE
     *
     * @see android.content.res.Configuration.ORIENTATION_PORTRAIT
     */
    val screenOrientation: Int get() = resources.configuration.orientation

    fun onViewDragStateChanged(state: Int) {
        viewDragState = state
    }

    interface OnPanelSlideListener {
        fun onStateChanged(state: Int)
        fun onRequestClose()
        fun onRequestOpened()
        fun onSlideChange(percent: Float)
    }
}