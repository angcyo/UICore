package com.angcyo.layout

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.customview.widget.ViewDragHelper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.base.*
import com.angcyo.fragment.IFragment
import com.angcyo.library.L
import com.angcyo.library.L.d
import com.angcyo.library.LTime
import com.angcyo.library.ex.*
import com.angcyo.tablayout.exactlyMeasure
import com.angcyo.widget.R
import com.angcyo.widget.base.findView
import com.angcyo.widget.base.getChildOrNull
import com.angcyo.widget.base.scaleAnimator
import com.angcyo.widget.layout.touch.SwipeBackLayout
import kotlin.toString

/**
 * 可以用来显示IView的布局, 每一层的管理, 重写于2018-3-2
 * Created by angcyo on 2016-11-12.
 */
class FragmentSwipeBackLayout(context: Context, attrs: AttributeSet? = null) :
    SwipeBackLayout(context, attrs) {

    companion object {
        private const val TAG = "FragmentSwipeBackLayout"

        /**
         * 调试布局 触发手上按下数量
         */
        var DEBUG_LAYOUT_POINTER = 6

        /**
         * 多指是否显示debug layout
         */
        var showDebugLayout = true
        var showDebugInfo = isDebug()
        var SHOW_DEBUG_TIME: Boolean = L.debug

        /**
         * inflate之后, 有时会返回 父布局, 这个时候需要处理一下, 才能拿到真实的RootView.
         */
        fun safeAssignView(
            parentView: View,
            childView: View
        ): View {
            return if (parentView === childView) {
                if (parentView is ViewGroup) {
                    return parentView.getChildAt(parentView.childCount - 1)
                }
                childView
            } else {
                childView
            }
        }

        fun name(obj: Any?): String {
            if (obj == null) {
                return "null object"
            }
            return if (obj is String) {
                "String:$obj"
            } else obj.javaClass.simpleName
        }

        internal fun FragmentManager.findFragmentByView(view: View): Fragment? {
            return fragments.find { it.view == view }
        }

        /**获取[Fragment]上层的所有[Fragment]*/
        internal fun FragmentManager.getOverlayFragment(anchor: Fragment): List<Fragment> {
            val result = mutableListOf<Fragment>()

            var findAnchor = false
            fragments.forEach {
                if (findAnchor) {
                    result.add(it)
                } else if (it == anchor) {
                    findAnchor = true
                }
            }

            return result
        }
    }

    var hSpace = (10 * resources.displayMetrics.density).toInt()
    var vSpace = (22 * resources.displayMetrics.density).toInt()
    var viewMaxHeight = 0 //debug模式下的成员变量
    var isInDebugLayout = false
    var debugPaint: Paint? = null
    var measureLogBuilder: StringBuilder? = null
    var viewVisibleRectTemp = Rect()

    /**
     * 已经按下返回键
     */
    private val isBackPress = false
    private val mInsets = IntArray(4)

    /**
     * 锁定高度, 当键盘弹出的时候, 可以不改变size
     */
    private var lockHeight = false

    /**上一个视图的x偏移距离, 顶层布局通过手指控制偏移*/
    private var translationOffsetX = 0f

    /**
     * 如果只剩下最后一个View, 是否激活滑动删除
     */
    private var enableRootSwipe = false

    /**
     * 是否正在拖拽返回.
     */
    var isSwipeDrag = false

    /**
     * 是否需要滑动返回, 如果正在滑动返回,则阻止onLayout的进行
     */
    private var isWantSwipeBack = false

    /**
     * 三指首次按下的时间
     */
    private var firstDownTime: Long = 0

    /**
     * 拦截所有touch事件
     */
    var interceptTouchEvent = false

    /**
     * 覆盖在的所有IView上的Drawable
     */
    private var overlayDrawable: Drawable? = null

    /**
     * 高度使用DecorView的高度, 否则使用View的高度
     */
    private var isFullOverlayDrawable = false

    /**
     * 触发滑动的时候, 是否隐藏键盘
     */
    var hideSoftInputOnSwipe = false

    /**返回按钮检查*/
    private val backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (isSwipeDrag) {
                //如果正在拖拽, 则不允许执行[Activity]默认的操作
            } else if (isInDebugLayout) {
                closeDebugLayout()
            }
        }
    }

    val fragmentManager: FragmentManager?
        get() {
            return if (context is FragmentActivity) {
                (context as FragmentActivity).supportFragmentManager
            } else {
                null
            }
        }

    /**
     * 获取有效Fragment的数量
     */
    val fragmentsCount: Int
        get() = fragmentManager?.getAllValidityFragment()?.size ?: 0

    init {
        backPressedCallback.isEnabled = false
        if (context is OnBackPressedDispatcherOwner) {
            context.onBackPressedDispatcher.addCallback(backPressedCallback)
        }
    }

    /**
     * 滑动返回处理
     */
    override fun canTryCaptureView(child: View): Boolean {
        if (fragmentManager == null) {
            return false
        }
        val lastFragment = findBeforeFragment()
        if (isBackPress || lastFragment == null || viewDragState != ViewDragHelper.STATE_IDLE) {
            return false
        }
        if (screenOrientation != Configuration.ORIENTATION_PORTRAIT) { //非竖屏, 禁用滑动返回
            return false
        }
        if (lastFragment is IFragment) {
            if (!(lastFragment as IFragment).canSwipeBack()) {
                return false
            }
        }
        if (fragmentsCount <= 0) {
            return false
        }
        if (fragmentsCount > 1) {
            return if (lastFragment.view === child) {
                if (hideSoftInputOnSwipe) {
                    hideSoftInput()
                }
                true
            } else {
                false
            }
        } else if (enableRootSwipe) {
            if (hideSoftInputOnSwipe) {
                hideSoftInput()
            }
            return true
        }
        return false
    }

    private fun findBeforeFragment(anchor: Fragment? = null): Fragment? {
        return fragmentManager?.findBeforeFragment(anchor)
    }

    private fun findFragment(view: View): Fragment? {
        return fragmentManager?.findFragmentByView(view)
    }

    /**
     * 为了确保任务都行执行完了, 延迟打印堆栈信息
     */
    private fun printLog() {
        postDelayed({ logLayoutInfo() }, 16)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mInsets[0] = insets.systemWindowInsetLeft
            mInsets[1] = insets.systemWindowInsetTop
            mInsets[2] = insets.systemWindowInsetRight
            mInsets[3] = insets.systemWindowInsetBottom
            super.onApplyWindowInsets(
                insets.replaceSystemWindowInsets(
                    insets.systemWindowInsetLeft,
                    0,
                    insets.systemWindowInsetRight,
                    if (lockHeight) 0 else insets.systemWindowInsetBottom
                )
            )
        } else {
            super.onApplyWindowInsets(insets)
        }
    }

    private val debugWidthSize: Int
        get() = measuredWidth - 2 * hSpace

    private val debugHeightSize: Int
        get() = measuredHeight - 4 * vSpace

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*if (isDebug()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }*/

        //of java
        //int widthSize = MeasureSpec.getSize(widthMeasureSpec)
        //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        //int heightSize = MeasureSpec.getSize(heightMeasureSpec)
        //int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //of kotlin
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        //val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        //val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val count = childCount
        if (isInDebugLayout) {
            //int hCount = count > 9 ? 4 : (count > 6 ? 3 : 2);//横向放3个
            //int vCount = (int) Math.max(2, Math.ceil(count * 1f / hCount));//竖向至少2行
            //int wSize = (getMeasuredWidth() - (hCount + 1) * hSpace) / hCount;
            //int hSize = (getMeasuredHeight() - (vCount + 1) * vSpace) / vCount;
            for (i in 0 until count) {
                val childAt = getChildAt(i)
                val visibility = childAt.visibility
                if (visibility != View.VISIBLE) {
                    childAt.setTag(R.id.lib_tag_old_view_visible, visibility)
                    childAt.visibility = View.VISIBLE
                }
                childAt.measure(
                    exactlyMeasure(widthSize - paddingLeft - paddingRight),
                    exactlyMeasure(heightSize - paddingTop - paddingBottom)
                )
            }
            setMeasuredDimension(widthSize, heightSize)
        } else {
            if (showDebugInfo) {
                LTime.tick()
                L.v("${hash()} ↓开始测量,Child共:$fragmentsCount")
            }
            //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            //只测量最后2个View
            for (i in childCount - 2 until childCount) {
                getChildOrNull(i)?.apply {
                    measure(widthMeasureSpec, heightMeasureSpec)
                }
            }
            setMeasuredDimension(widthSize, heightSize)
            if (showDebugInfo) {
                L.v("${hash()} ↑测量结束:${LTime.time()} ${widthSize}x$heightSize")
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        //L.e("debug layout 1 " + isInDebugLayout + " " + getScrollX() + " " + getScrollY());
        /*if (isDebug()) {
            super.onLayout(changed, left, top, right, bottom)
            return
        }*/

        if (isInDebugLayout) {
            val count = childCount
            //int l = hSpace;
            //int t = vSpace;
            val l = paddingLeft
            var t = -vSpace + paddingTop
            val wSize =
                measuredWidth - paddingLeft - paddingRight //getDebugWidthSize();
            val hSize =
                measuredHeight - paddingTop - paddingBottom //getDebugHeightSize();
            for (i in 0 until count) {
                val childAt = getChildAt(i)
                childAt.translationX = 0f
                childAt.translationY = 0f
                childAt.layout(l, t, l + wSize, t + hSize)
                t += debugHeightSize + vSpace
                //                t += hSize + vSpace;
            }
            //            viewMaxHeight = t;
            viewMaxHeight = t + 2 * vSpace
            return
        }
        //在滑动返回的过程中, 保持手势不释放. 顶层的View布局控制
        var swipeViewLeft = -1
        if (isSwipeDrag) {
            swipeViewLeft = targetView?.left ?: swipeViewLeft
        }
        //super.onLayout(changed, left, top, right, bottom)
        for (i in childCount - 2 until childCount) {
            getChildOrNull(i)?.apply {
                layout(0, 0, measuredWidth, measuredHeight)
            }
        }

        if (isSwipeDrag) {
            targetView?.apply {
                layout(swipeViewLeft, top, swipeViewLeft + measuredWidth, measuredHeight)
            }
        }
    }

    private fun hideSoftInput() {
        if (isSoftKeyboardShow) {
            val manager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    /**
     * 判断键盘是否显示
     */
    private val isSoftKeyboardShow: Boolean
        get() {
            val screenHeight = resources.displayMetrics.heightPixels
            val keyboardHeight = softKeyboardHeight
            return screenHeight != keyboardHeight && keyboardHeight > 100
        }

    /**
     * 获取键盘的高度
     */
    private val softKeyboardHeight: Int
        get() {
            val screenHeight = resources.displayMetrics.heightPixels
            val rect = Rect()
            getWindowVisibleDisplayFrame(rect)
            val visibleBottom = rect.bottom
            return screenHeight - visibleBottom
        }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (interceptTouchEvent) {
            return true
        }

        val actionMasked = ev.actionMasked
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            val lastFragment = findBeforeFragment()
            if (lastFragment != null) {
                var view: View? = null
                var targetViewGroup: ViewGroup? = null
                val lastFragmentView = lastFragment.view
                if (lastFragmentView is ViewGroup) {
                    targetViewGroup = lastFragmentView
                }
                if (targetViewGroup != null) {
                    view = targetViewGroup.findView(ev.rawX, ev.rawY)
                    if (L.debug) {
                        val builder = StringBuilder("\nTouchOn->")
                        if (view == null) {
                            builder.append("null")
                        } else {
                            view.getGlobalVisibleRect(viewVisibleRectTemp)
                            builder.append(viewVisibleRectTemp)
                            builder.append("#")
                            if (view is TextView) {
                                builder.append(view.text)
                                builder.append("#")
                            } else if (view is RecyclerView) {
                                builder.append(view.adapter)
                                builder.append("#")
                                builder.append(view.layoutManager)
                                builder.append("#")
                            }
                            if (view.hasOnClickListeners()) {
                                builder.append("$")
                            }
                            builder.append(view)
                        }
                        d(builder.toString())
                    }
                }
                if (lastFragment is IFragment) {
                    if (lastFragment.hideSoftInputOnTouchDown(view)) {
                        hideSoftInput()
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        //val actionMasked = ev.actionMasked
        if (handleDebugLayout(ev)) {
            return true
        }
        if (isInDebugLayout) {
            return true
        }
        return if (needInterceptTouchEvent()) {
            true
        } else super.onInterceptTouchEvent(ev)
    }

    /**
     * 返回是否需要拦截Touch事件
     */
    open fun needInterceptTouchEvent(): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        handleDebugLayout(event)
        if (isInDebugLayout) {
            orientationGestureDetector.onTouchEvent(event)
        } else {
            super.onTouchEvent(event)
        }
        return true
    }

    /**
     * 多点按下, 是否处理
     */
    private fun handleDebugLayout(ev: MotionEvent): Boolean {
        val actionMasked = ev.actionMasked
        val downTime = ev.downTime
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            firstDownTime = downTime
        }
        if (L.debug &&
            showDebugLayout &&
            actionMasked == MotionEvent.ACTION_POINTER_DOWN &&
            ev.pointerCount == DEBUG_LAYOUT_POINTER
        ) {
            if (ev.eventTime - firstDownTime < 500) { //快速三指按下才受理操作
                //debug模式下, 三指按下
                if (isInDebugLayout) {
                    closeDebugLayout()
                } else {
                    startDebugLayout()
                }
                return true
            }
        }
        return false
    }

    fun finishActivity() {
        if (context is Activity) {
            (context as Activity).dslAHelper {
                finish()
            }
        }
    }

    /**
     * 滚动到关闭状态
     */
    override fun onRequestClose() {
        super.onRequestClose()
        isSwipeDrag = false
        translation(0f)
        if (enableRootSwipe && fragmentsCount == 1) {
            finishActivity()
        } else {
            val lastFragment = findBeforeFragment()
            if (lastFragment != null) {
                if (lastFragment.view != null) {
                    lastFragment.view?.alpha = 0f
                    lastFragment.view?.visibility = View.GONE
                }
                swipeBackFragment(lastFragment)
            }
        }
    }

    /**
     * 默认状态
     */
    override fun onRequestOpened() {
        super.onRequestOpened()
        isSwipeDrag = false
        translation(-100f, 0f)
        resetViewVisible(findBeforeFragment(findBeforeFragment()))
        printLog()
    }

    override fun onSlideChange(percent: Float) {
        super.onSlideChange(percent)
        isSwipeDrag = true
        translation(percent)
    }

    override fun onStateIdle() {
        super.onStateIdle()
        isWantSwipeBack = false
        backPressedCallback.isEnabled = false
    }

    /**
     * 滑动中
     */
    override fun onStateDragging() {
        super.onStateDragging()
        backPressedCallback.isEnabled = true
        isWantSwipeBack = true
        isSwipeDrag = true
        //开始偏移时, 偏移的距离
        translation(-100f, (measuredWidth * 0.3f).also { translationOffsetX = it })
    }

    private fun translation(
        percent: Float /*如果为0, 表示滑动关闭了*/,
        translationX: Float = 0f /*强制偏移, percent=-100生效*/
    ) {
        val preFragment = findBeforeFragment(findBeforeFragment())
        if (preFragment != null) {
            val preFragmentView = preFragment.view
            if (preFragmentView != null) {
                val tx: Float = if (percent.toInt() == -100) {
                    translationX
                } else {
                    -translationOffsetX * percent
                }
                val visibility = preFragmentView.visibility
                if (visibility == View.GONE) {
                    preFragmentView.setTag(R.id.lib_tag_old_view_visible, visibility)
                    preFragmentView.visibility = View.VISIBLE
                }
                if (preFragmentView.translationX != tx) {
                    preFragmentView.translationX = tx
                }
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        try {
            super.dispatchDraw(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 打印堆栈信息
     */
    fun logLayoutInfo(): String {
        return fragmentManager?.log() ?: "fragmentManager is null. "
    }

    /**
     * 滑动返回的形式, 关闭一个Fragment
     */
    fun swipeBackFragment(fragment: Fragment?) {
        if (fragment == null) {
            return
        }

        fragmentManager?.dslFHelper {
            noAnim()
            findBeforeFragment(fragment)?.run {
                show(this)
            }
        }
    }

    private fun initDebugLayout() {
        if (debugPaint == null) {
            debugPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        if (measureLogBuilder == null) {
            measureLogBuilder = StringBuilder()
        }
    }

    fun startDebugLayout() {
        backPressedCallback.isEnabled = true
        if (!isInDebugLayout) {
            initDebugLayout()
            isInDebugLayout = true
            overScroller.abortAnimation()
            requestLayout()
            for (i in 0 until childCount) {
                val childAt = getChildAt(i)
                childAt.scaleAnimator(
                    toX = debugWidthSize * 1f / measuredWidth,
                    toY = debugHeightSize * 1f / measuredHeight
                )
            }
            postDelayed({
                scrollTo(0, Int.MAX_VALUE) //滚动到最后一个IView
            }, 16)
        }
    }

    fun closeDebugLayout() {
        backPressedCallback.isEnabled = false
        if (isInDebugLayout) {
            isInDebugLayout = false
            overScroller.abortAnimation()
            scrollTo(0, 0) //恢复滚动坐标
            resetViewVisible()
            requestLayout()
            for (i in 0 until childCount) {
                val childAt = getChildAt(i)
                childAt.scaleAnimator()
            }
            printLog()
        }
    }

    fun resetViewVisible() {
        if (fragmentManager != null) {
            val fragments =
                fragmentManager!!.fragments
            for (fragment in fragments) {
                resetViewVisible(fragment)
            }
        }
    }

    fun resetViewVisible(fragment: Fragment?) {
        if (fragment != null) {
            val view = fragment.view
            if (view != null) {
                val tag = view.getTag(R.id.lib_tag_old_view_visible)
                if (tag != null) {
                    view.visibility = (tag as Int)
                }
            }
        }
    }

    override fun drawSwipeLine(canvas: Canvas) {
        if (!isInDebugLayout) {
            super.drawSwipeLine(canvas)
        }
    }

    override fun drawDimStatusBar(canvas: Canvas) {
        if (!isInDebugLayout) {
            super.drawDimStatusBar(canvas)
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        var ty = y
        val maxScrollY = viewMaxHeight - measuredHeight
        if (ty > maxScrollY) {
            ty = maxScrollY
        }
        if (ty < 0) {
            ty = 0
        }
        super.scrollTo(x, ty)
    }

    override fun onFlingChange(orientation: ORIENTATION, velocityX: Float, velocityY: Float) {
        super.onFlingChange(orientation, velocityX, velocityY)
        if (isInDebugLayout && isVertical(orientation)) {
            if (velocityY > 1000) { //快速向下滑动
                startFlingY((-velocityY).toInt(), scrollY)
            } else if (velocityY < -1000) { //快速向上滑动
                startFlingY((-velocityY).toInt(), viewMaxHeight)
            }
        }
        //L.i("$orientation $velocityX $touchDownX $isSwipeDrag")
        if (!isSwipeDrag &&
            orientation == ORIENTATION.RIGHT &&
            velocityX > 1000 &&
            touchDownX <= measuredWidth / 3 /*在左边视图1/3的区域, 激活fling*/
        ) {
            //快速向右滑动, 触发fling关闭功能
            val lastFragment = findBeforeFragment()
            if (lastFragment is IFragment) {
                if (lastFragment.canFlingBack()) {
                    lastFragment.back()
                }
            } else {
                lastFragment?.back()
            }

            L.i("fling to back touchDownX:$touchDownX velocityX:$velocityX")
        }
    }

    private fun initDebugPaint() {
        debugPaint!!.strokeJoin = Paint.Join.ROUND
        debugPaint!!.style = Paint.Style.FILL_AND_STROKE
        debugPaint!!.strokeCap = Paint.Cap.ROUND
        debugPaint!!.textSize = 12 * resources.displayMetrics.density
        debugPaint!!.color = Color.WHITE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        /*调试模式绘制*/
        if (isInDebugLayout) {
            initDebugPaint()
            val childCount = childCount
            val l = hSpace
            var t = vSpace
            val wSize = debugWidthSize
            val hSize = debugHeightSize
            for (i in 0 until childCount) {
                val childAt = getChildAt(i)
                val fragmentByView = findFragment(childAt)
                val textHeight: Float = debugPaint.textHeight()
                val dp2: Float = 1 * dp
                debugPaint!!.setShadowLayer(dp2, dp2, dp2, Color.BLACK)
                measureLogBuilder?.run {
                    delete(0, length)

                    //单独绘制name, 一行显示不下
                    val name =
                        (fragmentByView?.className()
                            ?: "null") + " " + (canvas.javaClass.simpleName)
                    val nameHeight = textHeight
                    canvas.drawText(name, 2 * dp, t + nameHeight, debugPaint!!)

                    fragmentByView?.log(this)
                    append(" ha:")
                    append(canvas.isHardwareAccelerated)
                    canvas.drawText(
                        measureLogBuilder.toString(),
                        2 * dp,
                        t + textHeight + nameHeight,
                        debugPaint!!
                    )
                }
                t += hSize + vSpace
            }
        }
        /*全屏覆盖绘制Drawable*/
        if (overlayDrawable != null) {
            val context = context
            var screenHeight = measuredHeight
            if (isFullOverlayDrawable) {
                if (context is Activity) {
                    screenHeight =
                        context.window.decorView.measuredHeight
                }
            }
            overlayDrawable!!.setBounds(0, 0, measuredWidth, screenHeight)
            overlayDrawable!!.draw(canvas)
        }
    }

    override fun onScrollChange(
        orientation: ORIENTATION,
        distance: Float
    ) {
        super.onScrollChange(orientation, distance)
        if (isInDebugLayout && isVertical(orientation)) {
            scrollBy(0, distance.toInt())
        }
    }
}