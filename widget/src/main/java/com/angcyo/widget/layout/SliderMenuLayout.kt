package com.angcyo.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.dp
import com.angcyo.tablayout.exactlyMeasure
import com.angcyo.widget.R
import com.angcyo.widget.base.isTouchDown
import com.angcyo.widget.base.isTouchFinish
import com.angcyo.widget.layout.touch.TouchLayout
import kotlin.math.max

/**
 * 侧滑菜单布局, 布局数量小于2时, 无效果. 默认第一个是menu布局
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2018/04/03 15:19
 */
class SliderMenuLayout(context: Context, attributeSet: AttributeSet? = null) :
    TouchLayout(context, attributeSet) {

    companion object {
        /*菜单在左边*/
        const val SLIDER_GRAVITY_LEFT = 1

        /*菜单在右边*/
        const val SLIDER_GRAVITY_RIGHT = 2
    }

    private var menuMaxWidthRatio = 0.8f

    /**回调接口*/
    var sliderCallback: SliderCallback? = null

    /**第几个view是菜单*/
    private var menuViewIndex = 0

    /**当开启了enableContentLinkage时, 内容滚动view的index*/
    private var contentViewIndex = 1

    //菜单覆盖在内容之上
    private val isOverLayoutMode: Boolean get() = menuViewIndex > 0

    /**激活内容联动, 激活此操作, menuViewIndex最好为1, 否则有界面层级有问题*/
    var enableContentLinkage = true

    /**激活内容变暗*/
    var enableContentDim = true

    /**激活菜单视差*/
    var enableMenuParallax = true

    /**菜单打开的方向*/
    var menuSliderGravity = SLIDER_GRAVITY_LEFT

    var menuClosePreview = false

    /**激活通过滚动打开菜单*/
    var enableScrollOpenMenu = true

    /**激活通过快速滑动打开菜单*/
    var enableFlingOpenMenu = true

    var flingThreshold = 2000

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SliderMenuLayout)
        menuMaxWidthRatio =
            typedArray.getFloat(R.styleable.SliderMenuLayout_r_menu_max_width, menuMaxWidthRatio)
        menuViewIndex =
            typedArray.getInt(R.styleable.SliderMenuLayout_r_menu_view_index, menuViewIndex)
        menuSliderGravity =
            typedArray.getInt(R.styleable.SliderMenuLayout_r_menu_slider_gravity, menuSliderGravity)
        flingThreshold =
            typedArray.getInt(R.styleable.SliderMenuLayout_r_fling_threshold, flingThreshold)
        enableContentLinkage = typedArray.getBoolean(
            R.styleable.SliderMenuLayout_r_enable_content_linkage,
            enableContentLinkage
        )
        enableContentDim = typedArray.getBoolean(
            R.styleable.SliderMenuLayout_r_enable_content_dim,
            enableContentDim
        )
        enableMenuParallax = typedArray.getBoolean(
            R.styleable.SliderMenuLayout_r_enable_menu_parallax,
            enableMenuParallax
        )
        menuClosePreview = typedArray.getBoolean(
            R.styleable.SliderMenuLayout_r_menu_close_preview,
            menuClosePreview
        )
        enableScrollOpenMenu = typedArray.getBoolean(
            R.styleable.SliderMenuLayout_r_enable_scroll_open_menu,
            enableScrollOpenMenu
        )
        enableFlingOpenMenu = typedArray.getBoolean(
            R.styleable.SliderMenuLayout_r_enable_fling_open_menu,
            enableFlingOpenMenu
        )
        typedArray.recycle()

        contentViewIndex = if (menuViewIndex == 0) {
            1
        } else {
            0
        }

        setWillNotDraw(false)
    }

    private var needInterceptTouchEvent = false
    private var isTouchDown = false
    private var isTouchDownInContentWithMenuOpen = false //菜单打开的状态下, 点击在内容区域
    var isOldMenuOpen = false //事件触发之前,菜单的打开状态

    /*是否激活滑动菜单*/
    private fun canSlider(event: MotionEvent): Boolean {
        if (sliderCallback == null || isOldMenuOpen) {
            return true
        }
        return sliderCallback!!.canSlider(this, event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val dispatch = super.dispatchTouchEvent(ev)
        /*if (!enableScrollOpenMenu && enableFlingOpenMenu) {
            orientationGestureDetector.onTouchEvent(ev)
        }*/
        return dispatch
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val intercept = needInterceptTouchEvent
        super.onInterceptTouchEvent(ev)
        if (scrollHorizontalDistance.abs() in 1 until maxMenuWidth) {
            return enableScrollOpenMenu
        }
        return if (canSlider(ev)) {
            intercept && enableScrollOpenMenu
        } else {
            false
        }
    }

    override fun handleCommonTouchEvent(event: MotionEvent) {
        super.handleCommonTouchEvent(event)
        if (event.isTouchDown()) {
            isOldMenuOpen = isMenuOpen()
        }
        if (scrollHorizontalDistance == 0 && !canSlider(event)) {
            return
        }

        if (needInterceptTouchEvent) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        if (event.isTouchDown()) {
            isTouchDown = true
            isTouchDownInContentWithMenuOpen = false
            touchDownX = event.x
            touchDownY = event.y
            //overScroller.abortAnimation()

            if (isOldMenuOpen) {
                //打开已经打开
                if ((menuSliderGravity == SLIDER_GRAVITY_LEFT && event.x >= maxMenuWidth) ||
                    (menuSliderGravity == SLIDER_GRAVITY_RIGHT && event.x <= measuredWidth - maxMenuWidth)
                ) {
                    //点击在内容区域
                    isTouchDownInContentWithMenuOpen = true
                    needInterceptTouchEvent = true
                }
            } else {
                if (scrollHorizontalDistance.abs() in 1 until maxMenuWidth) {
                    //当菜单滑动到一半, 突然被终止, 又再次点击时
                    needInterceptTouchEvent = true
                }
            }
        } else if (event.isTouchFinish()) {
            isTouchDown = false
            parent.requestDisallowInterceptTouchEvent(false)

            if (needInterceptTouchEvent) {
                if (isTouchDownInContentWithMenuOpen &&
                    ((touchEventX - touchDownX).abs() <= scrollDistanceSlop.toFloat()) ||
                    (touchEventY - touchDownY).abs() >= (touchEventX - touchDownX).abs()
                ) {
                    if ((menuSliderGravity == SLIDER_GRAVITY_LEFT && event.x >= maxMenuWidth) ||
                        (menuSliderGravity == SLIDER_GRAVITY_RIGHT && event.x <= measuredWidth - maxMenuWidth)
                    ) {
                        //在菜单打开的情况下,点击了内容区域, 并且没有触发横向滚动
                        closeMenu()
                    } else {
                        resetLayout()
                    }
                } else {
                    resetLayout()
                }
                isTouchDownInContentWithMenuOpen = false
                needInterceptTouchEvent = false
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount < 2) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            var widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            var heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)

            if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                //测量菜单, 和内容的宽度
                for (i in 0 until childCount) {
                    val childAt = getChildAt(i)
                    when (i) {
                        menuViewIndex -> childAt.measure(
                            exactlyMeasure(menuMaxWidthRatio * widthSize),
                            heightMeasureSpec
                        )
                        contentViewIndex -> childAt.measure(
                            exactlyMeasure(widthSize),
                            heightMeasureSpec
                        )
                        else -> measureChildWithMargins(
                            childAt,
                            widthMeasureSpec,
                            0,
                            heightMeasureSpec,
                            0
                        )
                    }
                }
                setMeasuredDimension(widthSize, heightSize)
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        refreshContentLayout(
            if (isInEditMode && !menuClosePreview) maxMenuWidth else scrollHorizontalDistance,
            false
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sliderCallback?.onSizeChanged(this)
    }

    override fun onScrollChange(orientation: ORIENTATION, distance: Float /*瞬时值*/) {
        super.onScrollChange(orientation, distance)
        //L.e("call: onScrollChange -> $orientation $distance")
        //refreshMenuLayout(((secondMotionEvent?.x ?: 0f) - (firstMotionEvent?.x ?: 0f)).toInt())
        if (canSlider(firstMotionEvent!!)) {
            if (isHorizontal(orientation)) {
                if (!needInterceptTouchEvent) {
                    if (distance > 0) {
                        //左滑动
                        if (isMenuClose()) {
                            if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                                needInterceptTouchEvent = true
                            }
                        } else {
                            if (menuSliderGravity == SLIDER_GRAVITY_LEFT) {
                                needInterceptTouchEvent = true
                            }
                        }
                    } else {
                        //右滑动
                        if (isMenuOpen()) {
                            if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                                needInterceptTouchEvent = true
                            }
                        } else {
                            if (menuSliderGravity == SLIDER_GRAVITY_LEFT) {
                                needInterceptTouchEvent = true
                            }
                        }
                    }
                }

                if (needInterceptTouchEvent) {
                    if (isMenuClose() && !enableScrollOpenMenu) {
                        return
                    }
                    refreshLayout(distance.toInt())
                }
            }
        }
    }

    override fun onFlingChange(orientation: ORIENTATION, velocity: Float /*瞬时值*/) {
        super.onFlingChange(orientation, velocity)
        //L.e("call: onFlingChange -> $velocity")
        if (canSlider(firstMotionEvent!!)) {
            if (isHorizontal(orientation)) {
                if (velocity < -flingThreshold) {
                    //快速向左
                    if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                        if (enableFlingOpenMenu) {
                            openMenu()
                        }
                    } else {
                        closeMenu()
                    }
                } else if (velocity > flingThreshold) {
                    //快速向右
                    if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                        closeMenu()
                    } else {
                        if (enableFlingOpenMenu) {
                            openMenu()
                        }
                    }
                }
            }
        }
    }

    /**菜单是否完全打开*/
    fun isMenuOpen(): Boolean {
        return scrollHorizontalDistance.abs() >= maxMenuWidth
    }

    /**菜单完全关闭*/
    fun isMenuClose(): Boolean {
        return scrollHorizontalDistance <= 0
    }

    /**根据当前打开程度, 决定*/
    fun resetLayout() {
        if (isOldMenuOpen) {
            //菜单已经打开
            if (scrollHorizontalDistance <= maxMenuWidth * 2 / 3) {
                closeMenu()
            } else {
                openMenu()
            }
        } else {
            //菜单未打开
            if (scrollHorizontalDistance >= maxMenuWidth * 1 / 3) {
                openMenu()
            } else {
                closeMenu()
            }
        }
    }

    /**关闭菜单*/
    fun closeMenu() {
        if (scrollHorizontalDistance == 0) {
            if (isOldMenuOpen) {
                sliderCallback?.onMenuSlider(this, 0f, isTouchDown)
            } else {
            }
        } else {
            startScrollTo(scrollHorizontalDistance, 0)
        }
    }

    /**打开菜单*/
    fun openMenu() {
        if (scrollHorizontalDistance == maxMenuWidth) {
            if (isOldMenuOpen) {
            } else {
                sliderCallback?.onMenuSlider(this, 1f, isTouchDown)
            }
        } else {
            startScrollTo(scrollHorizontalDistance, maxMenuWidth)
        }
    }

    /**刷新布局位置*/
    private fun refreshLayout(distanceX: Int /*每次移动的距离*/) {
        //L.e("call: refreshMenuLayout -> $distanceX")
        refreshContentLayout(
            clampViewPositionHorizontal(
                if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                    scrollHorizontalDistance + distanceX
                } else {
                    scrollHorizontalDistance - distanceX
                }
            )
        )
    }

    private fun refreshContentLayout(left: Int, notify: Boolean = true) {
        if (childCount >= 2) {
            if (enableContentLinkage) {

                val contentLayout = if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                    -left
                } else {
                    left
                }

                getChildAt(contentViewIndex).apply {
                    layout(
                        contentLayout,
                        0,
                        contentLayout + this.measuredWidth,
                        this.measuredHeight
                    )
                }
            }
            scrollHorizontalDistance = left
            refreshMenuLayout()

            if (notify) {
                sliderCallback?.onMenuSlider(
                    this@SliderMenuLayout,
                    scrollHorizontalDistance.toFloat() / maxMenuWidth,
                    isTouchDown
                )
            }
        }
    }

    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            val currX = overScroller.currX
            if (scrollHorizontalDistance != currX) {
                refreshContentLayout(currX)
            }
            postInvalidate()
        }
    }

    //横向滚动了多少距离
    private var scrollHorizontalDistance = 0

    //当前内容布局的Left坐标
    private val contentLayoutLeft: Int
        get() {
            return if (childCount >= max(menuViewIndex, contentViewIndex)) {
                getChildAt(contentViewIndex).left
            } else {
                0
            }
        }

    //菜单允许展开的最大宽度
    private val maxMenuWidth: Int
        get() {
            return (menuMaxWidthRatio * measuredWidth).toInt()
        }

    //单独更新菜单,营造视差滚动
    private fun refreshMenuLayout() {
        if (enableContentLinkage) {
            if (isOverLayoutMode || !enableMenuParallax) {
                //菜单在内容的上面, 取消视差
                getChildAt(menuViewIndex).apply {
                    val left = if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                        this@SliderMenuLayout.measuredWidth - scrollHorizontalDistance
                    } else {
                        -this.measuredWidth + scrollHorizontalDistance
                    }
                    layout(left, 0, left + this.measuredWidth, this.measuredHeight)
                }
            } else {
                //计算出菜单展开的比例
                val fl = contentLayoutLeft.toFloat() / maxMenuWidth
                if (fl >= 0f && childCount > 0) {
                    getChildAt(menuViewIndex).apply {
                        //视差开始时的偏移值
                        val menuOffsetStart = -maxMenuWidth / 2
                        val left = menuOffsetStart + (menuOffsetStart.abs() * fl).toInt()
                        layout(left, 0, left + this.measuredWidth, this.measuredHeight)
                    }
                }
            }
        } else {
            if (childCount > menuViewIndex) {
                getChildAt(menuViewIndex).apply {
                    val left = if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                        this@SliderMenuLayout.measuredWidth - scrollHorizontalDistance
                    } else {
                        -this.measuredWidth + scrollHorizontalDistance
                    }
                    //L.e("call: menu layout -> left:$left right:${left + this.measuredWidth}")
                    layout(left, 0, left + this.measuredWidth, this.measuredHeight)
                }
            }
        }
    }

    /**约束内容允许滚动的范围*/
    private fun clampViewPositionHorizontal(value: Int): Int {
        val minValue = 0
        val maxValue = maxMenuWidth

        var result = value
        if (value < minValue) {
            result = minValue
        } else if (value > maxValue) {
            result = maxValue
        }
        return result
    }

    private val maskRect: Rect by lazy {
        Rect()
    }

    private val View.paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 1 * dp
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        //绘制内容区域的阴影遮盖
        if (!isMenuClose() && enableContentDim) {
            val left = if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                0
            } else {
                scrollHorizontalDistance
            }
            val right = if (menuSliderGravity == SLIDER_GRAVITY_RIGHT) {
                measuredWidth - scrollHorizontalDistance
            } else {
                measuredWidth
            }
            paint.color =
                Color.BLACK.alpha(255 * (scrollHorizontalDistance.toFloat() / maxMenuWidth) * 0.4f)
            paint.style = Paint.Style.FILL_AND_STROKE
            maskRect.set(left, 0, right, measuredHeight)
            canvas.drawRect(maskRect, paint)
        }
    }

    /**按下返回键, 自动关闭菜单*/
    fun requestBackPressed(): Boolean {
        if (isMenuOpen()) {
            closeMenu()
            return false
        }
        return true
    }

    fun toggle() {
        if (isMenuOpen()) {
            closeMenu()
        } else {
            openMenu()
        }
    }

    interface SliderCallback {

        /**当前是否可以操作*/
        fun canSlider(menuLayout: SliderMenuLayout, event: MotionEvent): Boolean

        fun onSizeChanged(menuLayout: SliderMenuLayout)

        /**
         * 菜单打开的完成度
         * @param ratio [0-1]
         * */
        fun onMenuSlider(
            menuLayout: SliderMenuLayout,
            ratio: Float,
            isTouchDown: Boolean /*手指是否还在触摸*/
        )
    }

    open class SimpleSliderCallback : SliderCallback {
        override fun canSlider(menuLayout: SliderMenuLayout, event: MotionEvent): Boolean {
            return true
        }

        override fun onSizeChanged(menuLayout: SliderMenuLayout) {
        }

        override fun onMenuSlider(
            menuLayout: SliderMenuLayout,
            ratio: Float,
            isTouchDown: Boolean
        ) {
        }
    }
}

