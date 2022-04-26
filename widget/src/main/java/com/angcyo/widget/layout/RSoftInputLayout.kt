package com.angcyo.widget.layout

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.GravityCompat
import com.angcyo.library.L.w
import com.angcyo.library.ex.getStatusBarHeight
import com.angcyo.library.ex.hideSoftInput
import com.angcyo.library.ex.toDpi
import com.angcyo.widget.R
import com.orhanobut.hawk.Hawk

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持透明状态栏, 支持Dialog, 支持动画
 *
 *
 * 重写于2019-8-19
 * 原理:
 * API < 21
 * 键盘弹出, 只会回调 onSizeChanged , 相差的高度就是键盘的高度
 *
 *
 * API >= 21
 * 如果未激活 View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN, 那么和 API < 21 的处理方式一样
 * 键盘弹出, 会回调 onApplyWindowInsets , insets.getSystemWindowInsetBottom, 就是键盘的高度
 * 此时onSizeChange方法不会执行, 应为系统是用 PaddingBottom的方式, 为键盘腾出空间
 *
 *
 *
 *
 * 使用方式:
 *
 *
 * 1. android:windowSoftInputMode="adjustResize"
 *
 *
 * 2.
 *
 * <pre>
 * &lt;RSoftInputLayout2&gt;
 * &lt;第一个必须是内容布局&gt;
 * &lt;第二个会被识别为emoji布局, 非必须&gt;
 * &lt;其他子布局&gt;
 * &lt;其他子布局&gt;
 * &lt;其他子布局&gt;
 * ...
 * &lt;/RSoftInputLayout2&gt;
</pre> *
 *
 *
 *
 *
 *
 *
 * 创建人员：Robi
 * 创建时间：2016/12/21 9:01
 * 修改人员：Robi
 * 修改时间：2019-8-19
 * 修改备注：
 * Version: 1.0.0
 */
class RSoftInputLayout : FrameLayout {
    /**
     * 动画执行回调, 可以修改动画执行的值
     */
    var animatorCallback: AnimatorCallback? = null

    /**
     * 当键盘未显示过时, 默认的键盘高度
     */
    var defaultKeyboardHeight = -1

    /**
     * 由于延迟操作带来的意图延迟, 此变量不考虑无延迟
     */
    var wantIntentAction = INTENT_NONE

    /**
     * 当前用户操作的意图
     */
    var intentAction = INTENT_NONE
        set(value) {
            if (value == INTENT_NONE || field != value) {
                if (field != INTENT_NONE) {
                    lastIntentAction = field
                }
            }
            field = value
            wantIntentAction = value
        }

    /**
     * 最后一次有效的操作意图
     */
    var lastIntentAction = intentAction

    /**
     * 最后一次的意图, 用来实现表情布局状态恢复
     */
    var lastRestoreIntentAction = intentAction

    //2级缓存状态
    var lastRestoreIntentAction2 = intentAction
    var contentLayoutMaxHeight = 0

    //键盘/emoji 当前显示的高度
    var bottomCurrentShowHeight = 0

    //动画过程中的高度变量
    var bottomCurrentShowHeightAnim = 0

    //动画进度
    var animProgress = 0f
    var lastKeyboardHeight = 0
    var contentLayout: View? = null
    var emojiLayout: View? = null
    var mEmojiLayoutChangeListeners =
        HashSet<OnEmojiLayoutChangeListener>()

    /**
     * 是否激活控件
     */
    private var enableSoftInput = true
    private var enableSoftInputAnim = true

    /**
     * 隐藏和显示的动画 分开控制
     */
    private var enableSoftInputAnimShow = true

    //<editor-fold defaultState="collapsed" desc="核心方法">
    private var enableSoftInputAnimHide = true

    /**
     * 可以关闭此开关, 当键盘弹出时, 只有事件回调, 没有界面size处理. (API>=21)
     */
    private var enableSoftInputInset = true

    /**
     * 频繁切换键盘, 延迟检查时长.
     * 如果开启了手机的安全密码输入键盘, 可以适当的加大延迟时间. 消除抖动.
     */
    private var switchCheckDelay = 0

    //</editor-fold defaultstate="collapsed" desc="事件相关">

    //<editor-fold defaultstate="collapsed" desc="静态区">
    var animDuration: Long = 240
        private set

    /**
     * 在软键盘展示的过程中, 动态改变此paddingTop, 需要开启 [enableSoftInputAnim]
     * 大于0, 表示激活属性
     */
    private var animPaddingTop = -1

    /**
     * 键盘完全显示时, 依旧需要的padding大小
     */
    private var animPaddingMinTop = 0

    /**
     * 激活表情布局恢复, (如:显示键盘之前是表情布局, 那么隐藏键盘后就会显示表情布局)
     */
    private var enableEmojiRestore = false
    private var delaySizeChanged: Runnable? = null
    private var insetRunnable: Runnable? = null
    //</editor-fold defaultstate="collapsed" desc="核心方法">

    //<editor-fold defaultstate="collapsed" desc="辅助方法">
    private var checkSizeChanged: Runnable? = null
    private var mValueAnimator: ValueAnimator? = null

    constructor(context: Context) : super(context) {
        initLayout(context, null)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        initLayout(context, attrs)
    }

    private fun initLayout(
        context: Context,
        attrs: AttributeSet?
    ) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.RSoftInputLayout)
        defaultKeyboardHeight = array.getDimensionPixelOffset(
            R.styleable.RSoftInputLayout_r_default_soft_input_height,
            defaultKeyboardHeight
        )
        animPaddingTop = array.getDimensionPixelOffset(
            R.styleable.RSoftInputLayout_r_soft_input_anim_padding_top,
            animPaddingTop
        )
        animPaddingMinTop = array.getDimensionPixelOffset(
            R.styleable.RSoftInputLayout_r_soft_input_anim_padding_min_top,
            animPaddingMinTop
        )
        enableSoftInput =
            array.getBoolean(R.styleable.RSoftInputLayout_r_enable_soft_input, enableSoftInput)
        enableSoftInputAnim = array.getBoolean(
            R.styleable.RSoftInputLayout_r_enable_soft_input_anim,
            enableSoftInputAnim
        )
        setEnableSoftInputAnim(enableSoftInputAnim)
        enableSoftInputAnimShow = array.getBoolean(
            R.styleable.RSoftInputLayout_r_enable_soft_input_anim_show,
            enableSoftInputAnimShow
        )
        enableSoftInputAnimHide = array.getBoolean(
            R.styleable.RSoftInputLayout_r_enable_soft_input_anim_hide,
            enableSoftInputAnimHide
        )
        enableEmojiRestore = array.getBoolean(
            R.styleable.RSoftInputLayout_r_enable_emoji_restore,
            enableEmojiRestore
        )
        enableSoftInputInset = array.getBoolean(
            R.styleable.RSoftInputLayout_r_enable_soft_input_inset,
            enableSoftInputInset
        )
        switchCheckDelay =
            array.getInt(R.styleable.RSoftInputLayout_r_switch_check_delay, switchCheckDelay)
        animDuration =
            array.getInt(R.styleable.RSoftInputLayout_r_anim_duration, animDuration.toInt())
                .toLong()
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val maxWidth = widthSize - paddingLeft - paddingRight
        val animPaddingTop = calcAnimPaddingTop()
        val maxHeight = heightSize - paddingTop - paddingBottom - animPaddingTop
        val layoutFullScreen =
            isLayoutFullScreen(context)
        val softKeyboardShow = isSoftKeyboardShow
        val emojiLayoutShow = isEmojiLayoutShow
        var bottomHeight = bottomCurrentShowHeight
        if (isInEditMode) {
            if (emojiLayout == null) {
                defaultKeyboardHeight = 0
            }
            bottomHeight = defaultKeyboardHeight
        }
        if (isAnimStart) {
            bottomHeight = bottomCurrentShowHeightAnim
        }
        if (contentLayout != null) {
            val layoutParams =
                contentLayout!!.layoutParams as LayoutParams
            contentLayoutMaxHeight = if (layoutFullScreen) {
                maxHeight - bottomHeight - layoutParams.topMargin - layoutParams.bottomMargin
            } else {
                if (softKeyboardShow) { //这里加动画, 体验不好.
//                    if (isAnimStart()) {
//                        contentLayoutMaxHeight = (int) (maxHeight - layoutParams.topMargin - layoutParams.bottomMargin
//                                + bottomCurrentShowHeight * (1 - animProgress));
//                    } else {
//                        contentLayoutMaxHeight = maxHeight - layoutParams.topMargin - layoutParams.bottomMargin;
//                    }
                    maxHeight - layoutParams.topMargin - layoutParams.bottomMargin
                } else {
                    maxHeight - bottomHeight - layoutParams.topMargin - layoutParams.bottomMargin
                }
            }
            val contentLayoutMaxWidth =
                maxWidth - layoutParams.leftMargin - layoutParams.rightMargin
            if (layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                measureChildWithMargins(
                    contentLayout, widthMeasureSpec, 0,
                    heightMeasureSpec, 0
                )
            }
            if (contentLayout!!.measuredHeight > contentLayoutMaxHeight
                || layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT
            ) {
                contentLayout!!.measure(
                    MeasureSpec.makeMeasureSpec(contentLayoutMaxWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(contentLayoutMaxHeight, MeasureSpec.EXACTLY)
                )
            }
            //            L.i("内容高度:" + contentLayout.getMeasuredHeight() +
//                    " max: " + maxHeight + ":" + contentLayoutMaxHeight + " anim:" + isAnimStart()
//                    + " top:" + animPaddingTop + " wa:" + wantIntentAction + " la:" + lastIntentAction);
        }
        if (emojiLayout != null) {
            emojiLayout!!.measure(
                MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(validBottomHeight(), MeasureSpec.EXACTLY)
            )
        }
        measureOther(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        val l = paddingLeft
        var t = paddingTop + calcAnimPaddingTop()
        var r = 0
        var b = 0
        if (contentLayout != null) {
            val layoutParams =
                contentLayout!!.layoutParams as LayoutParams
            r = l + contentLayout!!.measuredWidth
            b = t + contentLayoutMaxHeight
            t = b - layoutParams.bottomMargin - contentLayout!!.measuredHeight
            contentLayout!!.layout(l, t, r, b)
            t = contentLayout!!.bottom
        }
        if (emojiLayout != null) {
            if (isSoftKeyboardShow) {
                t = measuredHeight
            } else if (!isEnableSoftInputAnim() && intentAction == INTENT_HIDE_KEYBOARD) {
                t = measuredHeight
            }
            r = l + emojiLayout!!.measuredWidth
            b = t + emojiLayout!!.measuredHeight
            emojiLayout!!.layout(l, t, r, b)
        }
        layoutOther()
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) { //        L.i("sizeChanged:" + oldw + "->" + w + " " + oldh + "->" + h +
//                " " + intentAction + " k:" + isSoftKeyboardShow() + " anim:" + isAnimStart());
//低版本适配
        var h = h
        var oldh = oldh
        val layoutFullScreen =
            isLayoutFullScreen(context)
        if (!layoutFullScreen) {
            if (intentAction == INTENT_SHOW_EMOJI && checkSizeChanged == null) {
                return
            }
            if (isFirstLayout(oldw, oldh)) {
                if (isSoftKeyboardShow) { //软件盘默认是显示状态
                    oldh = h + softKeyboardHeight
                }
            }
            if (handleSizeChange(w, h, oldw, oldh)) { //有可能是键盘弹出了
                val diffHeight = oldh - h
                val softKeyboardShow = isSoftKeyboardShow
                wantIntentAction = if (diffHeight > 0) {
                    if (softKeyboardShow) {
                        INTENT_SHOW_KEYBOARD
                    } else {
                        INTENT_SHOW_EMOJI
                    }
                } else {
                    if (lastIntentAction == INTENT_SHOW_EMOJI) {
                        INTENT_HIDE_EMOJI
                    } else {
                        INTENT_HIDE_KEYBOARD
                    }
                }
            }
        } else { //高版本, 默认显示键盘适配
            if (oldw == 0 && oldh == 0) {
                if (isSoftKeyboardShow) {
                    oldh = h
                    h = oldh - softKeyboardHeight
                }
            }
        }
        //用来解决, 快速切换 emoji布局和键盘或者普通键盘和密码键盘 时, 闪烁的不良体验.
        if (delaySizeChanged != null) {
            removeCallbacks(delaySizeChanged)
        }
        delaySizeChanged = DelaySizeChangeRunnable(w, h, oldw, oldh, wantIntentAction)
        if (switchCheckDelay > 0) {
            postDelayed(delaySizeChanged, switchCheckDelay.toLong())
        } else {
            delaySizeChanged?.run()
        }
    }

    private fun initDefaultKeyboardHeight() { //恢复上一次键盘的高度
        if (defaultKeyboardHeight < 0) {
            var lastKeyboardHeight = 0
            if (!isInEditMode) {
                lastKeyboardHeight = Hawk.get(KEY_KEYBOARD_HEIGHT, 0)
            }
            defaultKeyboardHeight = if (lastKeyboardHeight <= 0) {
                (275 * resources.displayMetrics.density).toInt()
            } else {
                lastKeyboardHeight
            }
        }
    }

    private fun isFirstLayout(oldw: Int, oldh: Int): Boolean {
        return oldw == 0 && oldh == 0 && intentAction == INTENT_NONE
    }

    private fun handleSizeChange(w: Int, h: Int, oldw: Int, oldh: Int): Boolean {
        var result = false
        val isFirstLayout = isFirstLayout(oldw, oldh)
        val diffHeight = oldh - h
        if (isFirstLayout) { //布局第一次显示在界面上, 需要排除默认键盘展示的情况
            result = diffHeight != 0
        } else {
            if (oldw != 0 && w != oldw) { //有可能屏幕旋转了
            } else {
                result = diffHeight != 0
            }
        }
        return result
    }

    private fun clearIntentAction() {
        intentAction = INTENT_NONE
    }

    private fun measureOther(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 2 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChildWithMargins(
                    child, widthMeasureSpec, 0,
                    heightMeasureSpec, 0
                )
            }
        }
    }

    private fun layoutOther() {
        val count = childCount
        val parentLeft = paddingLeft
        val parentRight = measuredWidth - paddingRight
        val parentTop = paddingTop
        val parentBottom = measuredHeight - paddingBottom
        val forceLeftGravity = false
        for (i in 2 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp =
                    child.layoutParams as LayoutParams
                val width = child.measuredWidth
                val height = child.measuredHeight
                var childLeft = 0
                var childTop: Int
                var gravity = lp.gravity
                if (gravity == -1) {
                    gravity = Gravity.TOP or Gravity.LEFT
                }
                val layoutDirection =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        layoutDirection
                    } else {
                        0
                    }

                val absoluteGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection)
                val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
                when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                    Gravity.CENTER_HORIZONTAL -> childLeft =
                        parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin
                    Gravity.RIGHT -> if (!forceLeftGravity) {
                        childLeft = parentRight - width - lp.rightMargin
                    }
                    Gravity.LEFT -> childLeft = parentLeft + lp.leftMargin
                    else -> childLeft = parentLeft + lp.leftMargin
                }
                childTop = when (verticalGravity) {
                    Gravity.TOP -> parentTop + lp.topMargin
                    Gravity.CENTER_VERTICAL -> parentTop + (parentBottom - parentTop - height) / 2 +
                            lp.topMargin - lp.bottomMargin
                    Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
                    else -> parentTop + lp.topMargin
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height)
            }
        }
    }

    override fun addView(
        child: View,
        index: Int,
        params: ViewGroup.LayoutParams
    ) {
        super.addView(child, index, params)
        val childCount = childCount
        /*请按顺序布局*/if (childCount > 0) {
            contentLayout = getChildAt(0)
        }
        if (childCount > 1) {
            emojiLayout = getChildAt(1)
        }
        if (haveChildSoftInput(child)) {
            setEnableSoftInput(false)
        }
    }

    /**
     * 解决RSoftInputLayout嵌套RSoftInputLayout的问题
     */
    private fun haveChildSoftInput(child: View): Boolean {
        if (child is ViewGroup) {
            for (i in 0 until child.childCount) {
                return haveChildSoftInput(child.getChildAt(i))
            }
        }
        return child is RSoftInputLayout
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initDefaultKeyboardHeight()
        if (!isInEditMode && isEnabled && enableSoftInput) {
            setFitsSystemWindows()
            //setClipToPadding(false);//未知作用
        }
        //必须放在post里面调用, 才会生效
        post { adjustResize(context) }
    }

    override fun fitSystemWindows(insets: Rect): Boolean { //此方法会触发 dispatchApplyWindowInsets
        insets[0, 0, 0] = 0
        super.fitSystemWindows(insets)
        return isEnableSoftInput()
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets { //Fragment+Fragment中使用此控件支持.
        if (!isEnableSoftInput()) {
            return super.onApplyWindowInsets(insets)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            val insetBottom = insets.systemWindowInsetBottom
            //            L.i("onApplyWindowInsets:" + insetBottom + " " +
            //                    intentAction + " w:" + getMeasuredWidth() + " h:" + getMeasuredHeight());
            if (measuredWidth <= 0 && measuredHeight <= 0) {
                return super.onApplyWindowInsets(insets)
            }
            if (isSoftKeyboardShow && insetBottom <= 0) { //软件已经显示, 此时却要隐藏键盘. ViewPager中, 使用此控件支持.
                //当启动一个新的Activity时, 也会触发此场景.
                post {
                    if (!isSoftKeyboardShow) {
                        insetBottom(0)
                    }
                }
                return super.onApplyWindowInsets(insets)
            }
            val action: Int
            action = if (insetBottom > 0) {
                INTENT_SHOW_KEYBOARD
            } else {
                INTENT_HIDE_KEYBOARD
            }
            if (insetRunnable != null) {
                if (action == wantIntentAction) { //之前已有相同操作
                    return insets.replaceSystemWindowInsets(0, 0, 0, 0)
                }
                wantIntentAction = action
                removeCallbacks(insetRunnable)
            }
            insetRunnable = InsetRunnable(insetBottom)
            //键盘切换到键盘, 延迟检查. 防止是普通键盘切换到密码键盘
            if (lastIntentAction == INTENT_NONE || switchCheckDelay <= 0) { //第一次不检查
                insetRunnable?.run()
            } else {
                postDelayed(insetRunnable, switchCheckDelay.toLong())
            }
            //替换掉系统的默认处理方式(setPadding)
            //系统会使用setPadding的方式, 为键盘留出空间
            return insets.replaceSystemWindowInsets(0, 0, 0, 0)
        }
        return super.onApplyWindowInsets(insets)
    }

    //底部需要腾出距离
    private fun insetBottom(height: Int) {
        //        L.i("插入:" + height + ":" + getMeasuredHeight() +
        //                " isFirstLayout:" + isFirstLayout(getMeasuredWidth(), getMeasuredHeight()));
        val measuredWidth = measuredWidth
        val measuredHeight = measuredHeight
        if (height > 0) { //键盘弹出
            checkSizeChanged = KeyboardRunnable(
                measuredWidth, measuredHeight - height,
                measuredWidth, measuredHeight
            )
        } else { //键盘隐藏
            var hideHeight = bottomCurrentShowHeight
            if (bottomCurrentShowHeight <= 0) {
                hideHeight = lastKeyboardHeight
            }
            checkSizeChanged = KeyboardRunnable(
                measuredWidth, measuredHeight,
                measuredWidth, measuredHeight - hideHeight
            )
        }
        checkOnSizeChanged(false)
    }

    private fun checkOnSizeChanged(delay: Boolean) {
        if (checkSizeChanged == null) {
            return
        }
        removeCallbacks(checkSizeChanged)
        if (delay) {
            post(checkSizeChanged)
        } else {
            checkSizeChanged!!.run()
        }
    }
    //</editor-fold defaultstate="collapsed" desc="辅助方法">

    //<editor-fold defaultstate="collapsed" desc="属性操作">
    /**
     * 判断键盘是否显示
     */
    val isSoftKeyboardShow: Boolean
        get() {
            if (isInEditMode) {
                return false
            }
            val screenHeight = screenHeightPixels
            val keyboardHeight = softKeyboardHeight
            return screenHeight != keyboardHeight && keyboardHeight > 50.toDpi()
        }

    /**
     * 获取键盘的高度
     */
    val softKeyboardHeight: Int
        get() = getSoftKeyboardHeight(this)

    /**
     * 屏幕高度(不包含虚拟导航键盘的高度)
     */
    private val screenHeightPixels: Int
        private get() = resources.displayMetrics.heightPixels

    //</editor-fold defaultstate="collapsed" desc="属性操作">

    //<editor-fold defaultstate="collapsed" desc="方法控制">
    private fun startAnim(
        bottomHeightFrom: Int,
        bottomHeightTo: Int,
        duration: Long
    ) { //        L.i("动画:from:" + bottomHeightFrom + "->" + bottomHeightTo);
        var duration = duration
        cancelAnim()
        var from = bottomHeightFrom
        var to = bottomHeightTo
        if (animatorCallback != null) {
            val preStart = animatorCallback!!.onAnimatorPreStart(
                wantIntentAction,
                bottomHeightFrom,
                bottomHeightTo,
                duration
            )
            from = preStart[0]
            to = preStart[1]
            duration = preStart[2].toLong()
        }
        mValueAnimator = ObjectAnimator.ofInt(from, to)
        mValueAnimator?.setDuration(duration)
        if (animatorCallback == null) {
            mValueAnimator?.setInterpolator(DecelerateInterpolator())
        } else {
            mValueAnimator?.setInterpolator(animatorCallback!!.getInterpolator(wantIntentAction))
        }
        mValueAnimator?.addUpdateListener(AnimatorUpdateListener { animation ->
            val animatedFraction = animation.animatedFraction
            val animatedValue = animation.animatedValue as Int
            animProgress = animatedFraction
            bottomCurrentShowHeightAnim = if (animatorCallback == null) {
                animatedValue
            } else {
                animatorCallback!!.onUpdateAnimatorValue(
                    intentAction,
                    animatedFraction,
                    animatedValue
                )
            }
            requestLayout()
        })
        mValueAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                clearIntentAction()
                if (animatorCallback != null) {
                    animatorCallback!!.onAnimatorEnd(lastIntentAction, false)
                }
            }

            override fun onAnimationCancel(animation: Animator) { //clearIntentAction();
                if (animatorCallback != null) {
                    animatorCallback!!.onAnimatorEnd(wantIntentAction, true)
                }
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })
        mValueAnimator?.start()
        if (animatorCallback != null) {
            animatorCallback!!.onAnimatorStart(wantIntentAction)
        }
    }

    private fun cancelAnim() {
        mValueAnimator?.cancel()
        mValueAnimator = null
    }

    //动画是否执行中
    private val isAnimStart: Boolean
        private get() = mValueAnimator != null && mValueAnimator!!.isRunning

    //表情布局有效的高度
    private fun validBottomHeight(): Int { //没显示过键盘, 默认高度
        var bottomHeight = defaultKeyboardHeight
        if (lastKeyboardHeight > 0) { //显示过键盘, 有键盘的高度
            bottomHeight = lastKeyboardHeight
        }
        return bottomHeight
    }

    private fun calcAnimPaddingTop(): Int {
        if (animPaddingTop <= 0) {
            return 0
        }
        if (isInEditMode) {
            return animPaddingTop
        }
        var result = animPaddingTop
        val animStart = isAnimStart
        val statusBarHeight: Int = getStatusBarHeight()
        val layoutFullScreen =
            isLayoutFullScreen(context)
        if (isSoftKeyboardShow || isEmojiLayoutShow) {
            if (animStart && intentAction != lastIntentAction) {
                result = (animPaddingTop * (1 - animProgress)).toInt()
                result = Math.max(result, statusBarHeight + animPaddingMinTop)
            } else if (lastIntentAction == INTENT_NONE || lastIntentAction == INTENT_HIDE_EMOJI || lastIntentAction == INTENT_HIDE_KEYBOARD
            ) {
            } else if (layoutFullScreen) {
                result = statusBarHeight + animPaddingMinTop
            } else {
                result = animPaddingMinTop
            }
        } else {
            if (wantIntentAction == INTENT_HIDE_EMOJI ||
                wantIntentAction == INTENT_HIDE_KEYBOARD
            ) {
                if (animStart && wantIntentAction != lastIntentAction) {
                    result = (animPaddingTop * animProgress).toInt()
                    result = Math.max(result, statusBarHeight + animPaddingMinTop)
                } else if (layoutFullScreen) {
                    result = statusBarHeight + animPaddingMinTop
                } else {
                    result = animPaddingMinTop
                }
            }
        }
        return result
    }

    fun setFitsSystemWindows() {
        fitsSystemWindows = isEnabled && enableSoftInput
    }

    fun isEnableSoftInput(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            fitsSystemWindows && isEnabled && enableSoftInput
        } else {
            isEnabled && enableSoftInput
        }
    }

    fun setEnableSoftInput(enableSoftInput: Boolean) {
        if (this.enableSoftInput == enableSoftInput) {
            return
        }
        val keyboardShow = isSoftKeyboardShow
        this.enableSoftInput = enableSoftInput
        if (enableSoftInput) {
            isEnabled = true
            fitsSystemWindows = true
        } else {
            fitsSystemWindows = false
        }
        if (keyboardShow && !enableSoftInput) { //已经显示了软键盘, 这个时候禁用控件, 恢复默认布局
            intentAction = INTENT_HIDE_KEYBOARD
            insetBottom(0)
        } else {
            requestLayout()
        }
    }

    fun isEnableSoftInputAnim(): Boolean {
        return enableSoftInputAnimHide || enableSoftInputAnimShow
    }

    fun setEnableSoftInputAnim(enableSoftInputAnim: Boolean) {
        this.enableSoftInputAnim = enableSoftInputAnim
        enableSoftInputAnimHide = enableSoftInputAnim
        enableSoftInputAnimShow = enableSoftInputAnim
    }

    val isEmojiLayoutShow: Boolean
        get() {
            var result = false
            if (isSoftKeyboardShow) {
                return false
            }
            if (emojiLayout != null) {
                if (isAnimStart) {
                    result = bottomCurrentShowHeight > 0
                } else if (emojiLayout!!.measuredHeight > 0 && emojiLayout!!.top < measuredHeight - paddingBottom && emojiLayout!!.bottom >= measuredHeight - paddingBottom
                ) {
                    result = true
                }
            }
            return result
        }

    /**
     * 返回按键处理
     *
     * @return true 表示可以关闭界面
     */
    fun requestBackPressed(): Boolean {
        if (isSoftKeyboardShow) {
            if (intentAction == INTENT_HIDE_KEYBOARD) {
                return false
            }
            intentAction = INTENT_HIDE_KEYBOARD
            hideSoftInput()
            return false
        }
        if (isEmojiLayoutShow) {
            hideEmojiLayout()
            return false
        }
        return true
    }

    /**
     * 显示表情布局
     */
    fun showEmojiLayout(
        height: Int = validBottomHeight(),
        force: Boolean = false
    ) {
        if (force) {
            lastKeyboardHeight = height
        } else {
            if (isEmojiLayoutShow) {
                return
            }
            if (intentAction == INTENT_SHOW_EMOJI) {
                return
            }
        }
        if (isSoftKeyboardShow) {
            hideSoftInput()
        }

        intentAction = INTENT_SHOW_EMOJI

        insetBottom(height)
    }

    /**
     * 隐藏表情布局
     */
    fun hideEmojiLayout() {
        if (intentAction == INTENT_HIDE_EMOJI) {
            return
        }
        if (isEmojiLayoutShow) {
            intentAction = INTENT_HIDE_EMOJI
            insetBottom(0)
        }
    }

    //</editor-fold defaultstate="collapsed" desc="方法控制">

    //<editor-fold defaultstate="collapsed" desc="事件相关">
    fun setEnableSoftInputAnimShow(enableSoftInputAnimShow: Boolean) {
        this.enableSoftInputAnimShow = enableSoftInputAnimShow
    }

    fun setEnableSoftInputAnimHide(enableSoftInputAnimHide: Boolean) {
        this.enableSoftInputAnimHide = enableSoftInputAnimHide
    }

    fun setSwitchCheckDelay(switchCheckDelay: Int) {
        this.switchCheckDelay = switchCheckDelay
    }

    fun setAnimPaddingTop(animPaddingTop: Int) {
        this.animPaddingTop = animPaddingTop
        requestLayout()
    }

    fun setAnimPaddingMinTop(animPaddingMinTop: Int) {
        this.animPaddingMinTop = animPaddingMinTop
    }

    fun addOnEmojiLayoutChangeListener(listener: OnEmojiLayoutChangeListener) {
        mEmojiLayoutChangeListeners.add(listener)
        adjustResize(context)
    }

    fun removeOnEmojiLayoutChangeListener(listener: OnEmojiLayoutChangeListener?) {
        mEmojiLayoutChangeListeners.remove(listener)
    }

    private fun notifyEmojiLayoutChangeListener(
        isEmojiShow: Boolean,
        isKeyboardShow: Boolean,
        height: Int
    ) {
        w(hashCode().toString() + " 表情:" + isEmojiShow + " 键盘:" + isKeyboardShow + " 高度:" + height)
        if (isKeyboardShow && !isInEditMode) {
            Hawk.put(KEY_KEYBOARD_HEIGHT, height)
        }
        val iterator: Iterator<OnEmojiLayoutChangeListener> =
            mEmojiLayoutChangeListeners.iterator()
        while (iterator.hasNext()) {
            iterator.next().onEmojiLayoutChange(isEmojiShow, isKeyboardShow, height)
        }
    }

    interface OnEmojiLayoutChangeListener {
        /**
         * @param height         EmojiLayout弹出的高度 或者 键盘弹出的高度
         * @param isEmojiShow    表情布局是否显示了
         * @param isKeyboardShow 键盘是否显示了
         */
        fun onEmojiLayoutChange(
            isEmojiShow: Boolean,
            isKeyboardShow: Boolean,
            height: Int
        )
    }

    class AnimatorCallback {
        /**
         * 动画需要执行的值
         *
         * @return 按照入参顺序, 返回对应修改后的值
         */
        fun onAnimatorPreStart(
            intentAction: Int,
            bottomHeightFrom: Int,
            bottomHeightTo: Int,
            duration: Long
        ): IntArray {
            return intArrayOf(bottomHeightFrom, bottomHeightTo, duration.toInt())
        }

        /**
         * 动画执行过程中的值
         */
        fun onUpdateAnimatorValue(
            intentAction: Int,
            animatedFraction: Float,
            animatedValue: Int
        ): Int {
            return animatedValue
        }

        fun getInterpolator(intentAction: Int): TimeInterpolator {
            return DecelerateInterpolator()
        }

        fun onAnimatorStart(intentAction: Int) {}
        fun onAnimatorEnd(intentAction: Int, isCancel: Boolean) {}
    }

    //</editor-fold defaultstate="collapsed" desc="静态区">
    private inner class KeyboardRunnable(var w: Int, var h: Int, var oldw: Int, var oldh: Int) :
        Runnable {
        override fun run() {
            onSizeChanged(w, h, oldw, oldh)
            checkSizeChanged = null
        }

    }

    private inner class DelaySizeChangeRunnable(
        var w: Int,
        var h: Int,
        var oldw: Int,
        var oldh: Int,
        var delayIntentAction: Int
    ) : Runnable {
        override fun run() {
            val oldBottomCurrentShowHeight = bottomCurrentShowHeight
            //            L.i("doSizeChanged:" + oldw + "->" + w + " " + oldh + "->" + h + " " + oldBottomCurrentShowHeight + " " + intentAction);
            bottomCurrentShowHeight = 0
            var needAnim = isEnableSoftInputAnim()
            if (handleSizeChange(w, h, oldw, oldh)) { //有可能是键盘弹出了
                var diffHeight = oldh - h
                val softKeyboardShow = isSoftKeyboardShow
                var emojiLayoutShow = false
                if (softKeyboardShow) {
                    lastRestoreIntentAction2 = lastRestoreIntentAction
                } else {
                    lastRestoreIntentAction = intentAction
                }
                val layoutFullScreen =
                    isLayoutFullScreen(context)
                //低版本, 普通输入框和密码输入框切换适配
                if (!layoutFullScreen) {
                    if (softKeyboardShow) {
                        diffHeight = softKeyboardHeight
                    }
                }
                if (diffHeight > 0) { //当用代码调整了布局的height属性, 也会回调此方法.
                    if (enableSoftInputInset) {
                        bottomCurrentShowHeight = diffHeight
                    }
                    if (softKeyboardShow) {
                        lastKeyboardHeight = diffHeight
                        emojiLayoutShow = false
                    } else {
                        emojiLayoutShow =
                            intentAction == INTENT_SHOW_EMOJI
                        //有可能是表情布局显示
                    }
                    //键盘显示
                    if (!enableSoftInputAnimShow) {
                        needAnim = false
                    }
                    notifyEmojiLayoutChangeListener(emojiLayoutShow, softKeyboardShow, diffHeight)
                    if (needAnim && enableSoftInputInset) {
                        if (isAnimStart) {
                            startAnim(
                                Math.abs(bottomCurrentShowHeightAnim),
                                diffHeight,
                                animDuration
                            )
                        } else {
                            startAnim(oldBottomCurrentShowHeight, diffHeight, animDuration)
                        }
                    }
                } else {
                    if (lastRestoreIntentAction2 == INTENT_SHOW_EMOJI && enableEmojiRestore) {
                        emojiLayoutShow = true
                        diffHeight = -diffHeight
                        bottomCurrentShowHeight = diffHeight
                        lastRestoreIntentAction2 = INTENT_NONE
                    }
                    notifyEmojiLayoutChangeListener(emojiLayoutShow, false, diffHeight)
                    if (isFirstLayout(oldw, oldh)) {
                        needAnim = false
                    }
                    //键盘显示或者表情显示
                    if (!enableSoftInputAnimHide) {
                        needAnim = false
                    }
                    if (needAnim && !emojiLayoutShow) {
                        if (isAnimStart) {
                            startAnim(
                                Math.abs(bottomCurrentShowHeightAnim),
                                0,
                                animDuration
                            )
                        } else {
                            startAnim(Math.abs(diffHeight), 0, animDuration)
                        }
                    }
                }
                //低版本适配
                if (!layoutFullScreen) {
                    intentAction = delayIntentAction
                }
                requestLayout()
            }
            if (!needAnim) {
                clearIntentAction()
            }
            delaySizeChanged = null
        }

    }

    private inner class InsetRunnable(var insetBottom: Int) : Runnable {
        override fun run() {
            if (intentAction <= INTENT_HIDE_KEYBOARD) {
                if (insetBottom > 0) {
                    intentAction = INTENT_SHOW_KEYBOARD
                } else {
                    intentAction = INTENT_HIDE_KEYBOARD
                }
                cancelAnim()
                insetBottom(insetBottom)
            }
            insetRunnable = null
        }

    }

    companion object {
        const val INTENT_NONE = 0
        const val INTENT_SHOW_KEYBOARD = 1
        const val INTENT_HIDE_KEYBOARD = 2
        const val INTENT_SHOW_EMOJI = 3
        const val INTENT_HIDE_EMOJI = 4
        const val KEY_KEYBOARD_HEIGHT = "key_keyboard_height"

        fun isLayoutFullScreen(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            return if (context is Activity) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val window = context.window
                    val systemUiVisibility = window.decorView.systemUiVisibility
                    systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN == View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                } else {
                    false
                }
            } else {
                false
            }
        }

        fun getSoftKeyboardHeight(view: View): Int {
            var view = view
            val context = view.context
            var screenHeight = 0
            val isLollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            if (context is Activity && isLayoutFullScreen(context)) {
                val window = context.window
                view = window.decorView
                screenHeight =
                    window.findViewById<View>(Window.ID_ANDROID_CONTENT)
                        .measuredHeight
            } else {
                screenHeight = view.resources.displayMetrics.heightPixels
                if (isLollipop) {
                    screenHeight += view.getStatusBarHeight()
                }
            }
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val visibleBottom = rect.bottom
            return screenHeight - visibleBottom
        }

        fun adjustResize(context: Context?) { //resize 必备条件
            if (context is Activity) {
                val window = context.window
                val softInputMode = window.attributes.softInputMode
                if (softInputMode and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                ) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
            }
        }
    }
}