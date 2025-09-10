package com.angcyo.widget.layout

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsCompat
import com.angcyo.library.L
import com.angcyo.library.ex.anim
import com.angcyo.library.ex.append
import com.angcyo.library.ex.back
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.getStatusBarHeight
import com.angcyo.library.ex.hideSoftInput
import com.angcyo.library.ex.offsetTopTo
import com.angcyo.widget.R
import com.angcyo.widget.layout.DslSoftInputLayout.Companion.ACTION_HIDE_EMOJI
import com.angcyo.widget.layout.DslSoftInputLayout.Companion.ACTION_HIDE_SOFT_INPUT
import com.angcyo.widget.layout.DslSoftInputLayout.Companion.ACTION_SHOW_EMOJI
import com.angcyo.widget.layout.DslSoftInputLayout.Companion.ACTION_SHOW_SOFT_INPUT
import kotlin.math.min

/**
 * 只针对API 21以上处理软键盘, 支持在Activity中同时存在多个控件
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/15
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class DslSoftInputLayout(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    companion object {

        var DEFAULT_SOFT_INPUT_HEIGHT = 275 * dpi
        private val DEFAULT_CHILD_GRAVITY = Gravity.BOTTOM or Gravity.START

        const val ACTION_SHOW_SOFT_INPUT = 1
        const val ACTION_HIDE_SOFT_INPUT = 2
        const val ACTION_SHOW_EMOJI = 3
        const val ACTION_HIDE_EMOJI = 4

        /**模式: 同时改变内容和emoji布局的高度*/
        const val MODE_HEIGHT = 1

        /**模式: 同时偏移内容和emoji布局*/
        const val MODE_OFFSET = 2

        /**只改变内容布局的高度, emoji跟随布局*/
        const val MODE_CONTENT_HEIGHT = 3

        /**只改变emoji的高度, 内容顶上去*/
        const val MODE_EMOJI_HEIGHT = 4
    }

    //<editor-fold desc="属性配置">

    /**是否激活组件*/
    var enableLayout = true

    /**激活显示表情/键盘时的动画*/
    var enableShowAnimator = true

    /**激活隐藏表情/键盘时的动画*/
    var enableHideAnimator = true

    /**动画时长*/
    var animatorDuration = 160L

    /**键盘弹出时, 布局处理模式*/
    var handlerMode = MODE_OFFSET

    /**insert过程中, 动画调整的padding大小, [MODE_HEIGHT]模式下生效*/
    var softInputPaddingTop = 0
        set(value) {
            field = value
            _softInputPaddingTop = value
        }

    /**监听器*/
    val softInputListener = mutableListOf<OnSoftInputListener>()

    /**隐藏键盘时, 如果上一次是显示表情, 则恢复表情布局*/
    var keepEmojiState: Boolean = false

    /**隐藏/显示键盘时, 将emoji视图布局不可见*/
    var hideEmojiViewOnSoftInput: Boolean = false

    /**当键盘/emoji显示时, 内容布局的高度, 是否要排除状态栏的大小,
     * 此属性最好配合[softInputPaddingTop]一起使用*/
    var fixStatusBar: Boolean = false

    /**延迟处理[onApplyWindowInsets]
     * 当从密码键盘切换到非密码键盘时, 可以通过此属性避免跳动*/
    var delayWindowInsets: Int = 64

    //</editor-fold desc="属性配置">

    //<editor-fold desc="私有属性辅助计算">

    //需要额外追加的paddingTop, 只影响内容布局的测量
    var _softInputPaddingTop = 0

    //底部目标的高度
    var bottomInsertHeight = 0

    //底部需要计算的高度(有可能是emoji布局显示或者键盘显示)
    var _bottomInsertHeight = 0

    //[MODE_OFFSET]时, top偏移量
    //var offsetTop = 0
    var _offsetTop = 0

    //操作列表, 用于在隐藏键盘之后, 判断是否需要再次显示emoji视图
    val _actionList = mutableListOf<Int>()

    //记录当前操作行为
    var _action = 0

    //底部窗口是否有inset, 如果有通常是键盘显示了
    var _isBottomWindowInset: Boolean = false

    var _delayHandleRunnable: DelayHandleRunnable? = null

    //</editor-fold desc="私有属性辅助计算">

    init {
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.DslSoftInputLayout)

        enableLayout = typedArray.getBoolean(
            R.styleable.DslSoftInputLayout_r_enable_layout,
            enableLayout
        )
        enableShowAnimator = typedArray.getBoolean(
            R.styleable.DslSoftInputLayout_r_enable_show_animator,
            enableShowAnimator
        )
        enableHideAnimator = typedArray.getBoolean(
            R.styleable.DslSoftInputLayout_r_enable_hide_animator,
            enableHideAnimator
        )

        if (typedArray.hasValue(R.styleable.DslSoftInputLayout_r_enable_animator)) {
            val animator =
                typedArray.getBoolean(R.styleable.DslSoftInputLayout_r_enable_animator, true)
            enableAnimator(animator)
        }

        animatorDuration =
            typedArray.getInt(
                R.styleable.DslSoftInputLayout_r_animator_duration,
                animatorDuration.toInt()
            ).toLong()

        handlerMode = typedArray.getInt(
            R.styleable.DslSoftInputLayout_r_handler_mode,
            handlerMode
        )

        softInputPaddingTop = typedArray.getDimensionPixelOffset(
            R.styleable.DslSoftInputLayout_r_soft_input_padding_top,
            softInputPaddingTop
        )

        keepEmojiState =
            typedArray.getBoolean(R.styleable.DslSoftInputLayout_r_keep_emoji_state, keepEmojiState)

        hideEmojiViewOnSoftInput = typedArray.getBoolean(
            R.styleable.DslSoftInputLayout_r_hide_emoji_view_on_soft_input,
            hideEmojiViewOnSoftInput
        )

        fixStatusBar =
            typedArray.getBoolean(R.styleable.DslSoftInputLayout_r_fix_status_bar, fixStatusBar)

        delayWindowInsets = typedArray.getInt(
            R.styleable.DslSoftInputLayout_r_delay_window_insets,
            delayWindowInsets
        )

        typedArray.recycle()

        if (isInEditMode) {
            enableShowAnimator = false
            enableHideAnimator = false
        }
    }

    //<editor-fold desc="基础方法">

    fun removeDelayHandle() {
        removeCallbacks(_delayHandleRunnable)
        _delayHandleRunnable = null
    }

    fun delayHandle(action: Int, height: Int) {
        if (delayWindowInsets > 0) {
            _delayHandleRunnable = DelayHandleRunnable(action, height)
            postDelayed(_delayHandleRunnable, delayWindowInsets.toLong())
        } else {
            handleSoftInput(action, height)
            removeDelayHandle()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        try {
            super.dispatchDraw(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        _initView()
    }

    /**是否要激活功能*/
    fun enableSoftInputApply(enable: Boolean = true) {
        isEnabled = enable
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
            val navigationBarInsets =
                insetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars())
            val imeInsets = insetsCompat.getInsets(WindowInsetsCompat.Type.ime())
            val systemWindowInsetBottom = insets.systemWindowInsetBottom
            _isBottomWindowInset = systemWindowInsetBottom > 0
            if (systemWindowInsetBottom > 0) {
                //需要显示键盘
                if (enableLayout && isEnabled) { //显示键盘的时候判断, 阻止显示流程
                    removeDelayHandle()
                    delayHandle(ACTION_SHOW_SOFT_INPUT, systemWindowInsetBottom)
                }
            } else if (systemWindowInsetBottom == 0) {
                //可能是隐藏键盘, 也可能是显示表情布局
                if (_action != ACTION_SHOW_EMOJI) {
                    removeDelayHandle()
                    delayHandle(ACTION_HIDE_SOFT_INPUT, systemWindowInsetBottom)
                }
            }
        }
        return super.onApplyWindowInsets(insets)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var maxHeight = 0
        var maxWidth = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            if (child.visibility != View.GONE) {
                val lp = child.layoutParams as MarginLayoutParams

                when (child) {
                    _contentView -> {
                        //内容布局测量大小
                        when (handlerMode) {
                            //需要改变内容布局的高度
                            MODE_HEIGHT, MODE_CONTENT_HEIGHT -> measureChildWithMargins(
                                child,
                                widthMeasureSpec,
                                0,
                                MeasureSpec.makeMeasureSpec(
                                    heightSize - _bottomInsertHeight,
                                    heightMode
                                ),
                                _softInputPaddingTop
                            )

                            else -> measureChildWithMargins(
                                child,
                                widthMeasureSpec,
                                0,
                                heightMeasureSpec,
                                _softInputPaddingTop
                            )
                        }
                    }

                    _emojiView -> {
                        //emoji布局测量大小
                        when (handlerMode) {
                            MODE_HEIGHT, MODE_EMOJI_HEIGHT -> {
                                //需要改变emoji布局的高度
                                val childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(
                                    widthMeasureSpec,
                                    paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin,
                                    lp.width
                                )
                                val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                                    _bottomInsertHeight,
                                    MeasureSpec.EXACTLY
                                )
                                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                            }

                            else -> {
                                val childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(
                                    widthMeasureSpec,
                                    paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin,
                                    lp.width
                                )
                                val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                                    bottomInsertHeight, MeasureSpec.EXACTLY
                                )
                                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                            }
                        }
                    }

                    else -> {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                    }
                }

                maxWidth =
                    maxWidth.coerceAtLeast(child.measuredWidth + lp.leftMargin + lp.rightMargin)

                if (child != _emojiView) {
                    //emoji布局的高度, 不参与parent的测量
                    maxHeight =
                        maxHeight.coerceAtLeast(child.measuredHeight + lp.topMargin + lp.bottomMargin)
                }
            }
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = maxHeight + _bottomInsertHeight
//            if (_action == ACTION_SHOW_EMOJI || _action == ACTION_SHOW_SOFT_INPUT) {
//                heightSize = maxHeight + _bottomInsertHeight
//            } else {
//                heightSize = maxHeight
//            }
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = maxWidth
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutChildren(left, top, right, bottom, false)
        _offsetContent(_offsetTop)
    }

    //copy from FrameLayout
    fun layoutChildren(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        forceLeftGravity: Boolean
    ) {
        val count = childCount
        val parentLeft: Int = paddingLeft
        val parentRight: Int = right - left - paddingRight
        val parentTop: Int = paddingTop
        val parentBottom: Int = bottom - top - paddingBottom

        val emojiHeight = _emojiView?.measuredHeight ?: 0
        val emojiViewTop: Int =
            if (handlerMode == MODE_HEIGHT || handlerMode == MODE_EMOJI_HEIGHT) {
                parentBottom - emojiHeight
            } else if (handlerMode == MODE_OFFSET) {
                parentBottom
            } else {
                parentBottom - _bottomInsertHeight
            }

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {

                val lp = child.layoutParams as LayoutParams
                val width = child.measuredWidth
                val height = child.measuredHeight
                var childLeft: Int
                var childTop: Int
                var gravity = lp.gravity
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY
                }
                val layoutDirection =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        layoutDirection
                    } else {
                        0
                    }
                val absoluteGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection)
                val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK

                childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                    Gravity.CENTER_HORIZONTAL -> parentLeft + (parentRight - parentLeft - width) / 2 +
                            lp.leftMargin - lp.rightMargin

                    Gravity.RIGHT -> {
                        if (!forceLeftGravity) {
                            parentRight - width - lp.rightMargin
                        } else {
                            parentLeft + lp.leftMargin
                        }
                    }

                    Gravity.LEFT -> parentLeft + lp.leftMargin
                    else -> parentLeft + lp.leftMargin
                }

                if (child == _emojiView) {
                    //emoji布局的top
                    childTop = emojiViewTop
                } else if (child == _contentView) {
                    //内容布局的top
                    childTop = when (verticalGravity) {
                        Gravity.TOP -> min(
                            parentTop + lp.topMargin + _softInputPaddingTop,
                            emojiViewTop - lp.bottomMargin - height - lp.topMargin
                        )

                        Gravity.CENTER_VERTICAL -> parentTop + (emojiViewTop - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin

                        Gravity.BOTTOM -> min(
                            parentBottom - height - lp.bottomMargin,
                            emojiViewTop - lp.bottomMargin - height - lp.topMargin
                        )

                        else -> emojiViewTop - lp.bottomMargin - height - lp.topMargin
                    }
                } else {
                    //其他布局
                    childTop = when (verticalGravity) {
                        Gravity.TOP -> parentTop + lp.topMargin
                        Gravity.CENTER_VERTICAL -> parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin

                        Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
                        else -> parentTop + lp.topMargin
                    }
                }
                if (child == _contentView) {
                    _contentLayoutTop = childTop
                } else if (child == _emojiView) {
                    _emojiLayoutTop = childTop
                }

                if (child == _emojiView &&
                    hideEmojiViewOnSoftInput &&
                    (_action == ACTION_HIDE_SOFT_INPUT || _action == ACTION_SHOW_SOFT_INPUT)
                ) {
                    //隐藏键盘时, 将emoji视图布局不可见
                    child.layout(childLeft, parentBottom, childLeft, parentBottom)
                } else {
                    child.layout(childLeft, childTop, childLeft + width, childTop + height)
                }
            }
        }
    }

    //</editor-fold desc="基础方法">

    //<editor-fold desc="辅助方法">

    //内容视图, 强制GRAVITY为BOTTOM
    var _contentView: View? = null

    //emoji视图
    var _emojiView: View? = null

    fun _initView() {
        if (childCount > 0) {
            _contentView = getChildAt(0)
        }
        if (childCount > 1) {
            _emojiView = getChildAt(1)
        }

        //预览表情布局的高度
        if (isInEditMode) {
            if (_emojiView != null) {
                val height = _emojiView?.layoutParams?.height ?: 0
                if (height > 0) {
                    showEmojiLayout(height)
                } else {
                    showEmojiLayout(DEFAULT_SOFT_INPUT_HEIGHT)
                }
            }
        }
    }

    //内容布局原本布局的top值
    var _contentLayoutTop = 0
    var _emojiLayoutTop = 0

    var _animator: ValueAnimator? = null

    //触发offset的成本, 比触发requestLayout的成本低
    fun _offsetContent(offset: Int) {
        _offsetTop = offset
        _contentView?.offsetTopTo(_contentLayoutTop + offset)
        _emojiView?.offsetTopTo(_emojiLayoutTop + offset)

        //L.i("offset:$offset top:${_emojiLayoutTop + offset}")
        //L.i("${_emojiView?.top} ${_emojiView?.bottom} ${_emojiView?.measuredHeight}")
    }

    fun Int.orHeight(): Int {
        return if (this <= 0) {
            DEFAULT_SOFT_INPUT_HEIGHT
        } else {
            this
        }
    }

    //保存最后一次软键盘的高度
    var _lastSoftInputHeight = 0

    //</editor-fold desc="辅助方法">

    //<editor-fold desc="操作方法">

    /**处理键盘插入*/
    fun handleSoftInput(action: Int, height: Int) {
        if (action == ACTION_SHOW_SOFT_INPUT) {
            _lastSoftInputHeight = height
        }
        if (action == ACTION_HIDE_SOFT_INPUT && action == _action) {
            //L.w("no op!")
            return
        }
        if (keepEmojiState &&
            action == ACTION_HIDE_SOFT_INPUT &&
            _actionList.getOrNull(_actionList.size - 2) == ACTION_SHOW_EMOJI
        ) {
            showEmojiLayout()
        } else {
            insertBottom(action, height)
        }
    }

    fun showEmojiLayout(height: Int = _lastSoftInputHeight) {
        handleEmojiLayout(ACTION_SHOW_EMOJI, height.orHeight())
    }

    /**处理隐藏键盘/表情布局的高度,
     * 并不会调用隐藏键盘的方法*/
    fun hideEmojiLayout() {
        if (_action == ACTION_SHOW_SOFT_INPUT) {
            handleSoftInput(ACTION_HIDE_SOFT_INPUT, 0)
        } else if (_action == ACTION_SHOW_EMOJI) {
            handleEmojiLayout(ACTION_HIDE_EMOJI, 0)
        } else {
            L.w("no op")
        }
    }

    /**处理表情布局展示*/
    fun handleEmojiLayout(action: Int, height: Int = _lastSoftInputHeight) {
        if (!isInEditMode) {
            hideSoftInput()
        }
        insertBottom(action, height)
    }

    fun insertBottom(action: Int, height: Int) {
        val fromHeight = _bottomInsertHeight
        val toHeight = height

        _animator?.cancel()
        _animator = null

        _action = action
        _actionList.append(_action)

        if (fromHeight == toHeight) {
            //无变化
            return
        }

        val anim = (action.isShowAction() && enableShowAnimator) ||
                (action.isHideAction() && enableHideAnimator)

        val fromPaddingTop = _softInputPaddingTop
        val toPaddingTop = if (action.isShowAction()) {
            if (fixStatusBar) context.getStatusBarHeight() else 0
        } else softInputPaddingTop

        val fromOffset = _offsetTop
        val toOffset = -height

        if (action.isShowAction()) {
            bottomInsertHeight = height
        }

        _notifyListenerStart(action, toHeight, fromHeight)

        val end: (animator: ValueAnimator?) -> Unit = {
            _bottomInsertHeight = height
            _notifyListenerEnd(action, height, fromHeight)
        }

        if (handlerMode == MODE_OFFSET) {
            if (action.isShowAction()) {
                _bottomInsertHeight = height
            }
            if (anim) {
                _animator = anim(0f, 1f) {
                    onAnimatorConfig = { animator ->
                        animator.duration = animatorDuration
                    }
                    onAnimatorUpdateValue = { _, fraction ->
                        val offset = fromOffset + (toOffset - fromOffset) * fraction
                        _offsetContent(offset.toInt())

                        _notifyListenerChange(action, _bottomInsertHeight, fromHeight, fraction)
                    }
                    onAnimatorEnd = end
                }
            } else {
                _bottomInsertHeight = height
                _offsetContent(-height)
                end(null)
            }
        } else {
            //恢复offset
            _offsetContent(0)

            if (anim) {
                _animator = anim(0f, 1f) {
                    onAnimatorConfig = { animator ->
                        animator.duration = animatorDuration
                    }
                    onAnimatorUpdateValue = { _, fraction ->
                        _softInputPaddingTop =
                            (fromPaddingTop + (toPaddingTop - fromPaddingTop) * fraction).toInt()
                        _bottomInsertHeight =
                            (fromHeight + (toHeight - fromHeight) * fraction).toInt()
                        requestLayout()

                        _notifyListenerChange(action, _bottomInsertHeight, fromHeight, fraction)
                    }
                    onAnimatorEnd = end
                }
            } else {
                _softInputPaddingTop = toPaddingTop
                _bottomInsertHeight = toHeight
                end(null)
            }
        }
    }

    fun enableAnimator(animator: Boolean) {
        enableShowAnimator = animator
        enableHideAnimator = animator
    }

    fun _notifyListenerStart(action: Int, height: Int, oldHeight: Int) {
        softInputListener.forEach {
            it.onSoftInputChangeStart(action, height, oldHeight)
        }
    }

    fun _notifyListenerChange(action: Int, height: Int, oldHeight: Int, fraction: Float) {
        softInputListener.forEach {
            it.onSoftInputChange(action, height, oldHeight, fraction)
        }
    }

    fun _notifyListenerEnd(action: Int, height: Int, oldHeight: Int) {
        if (!isInEditMode) {
            L.d(action.softAction(), " $oldHeight -> ", height)
        }
        softInputListener.forEach {
            it.onSoftInputChangeEnd(action, height, oldHeight)
        }
    }

    fun addSoftInputListener(listener: OnSoftInputListener) {
        if (!softInputListener.contains(listener)) {
            softInputListener.add(listener)
        }
    }

    fun removeSoftInputListener(listener: OnSoftInputListener) {
        if (softInputListener.contains(listener)) {
            softInputListener.remove(listener)
        }
    }

    /**返回true, 表示不需要拦截处理[back]操作*/
    fun onBackPress(): Boolean {
        if (_action.isShowAction()) {
            hideEmojiLayout()
            return false
        }
        return true
    }

    /**正常布局状态, 无键盘显示, 表情显示*/
    fun isNormal(): Boolean = _bottomInsertHeight == 0

    /**键盘是否显示了*/
    fun isSoftInputShow(): Boolean = _isBottomWindowInset

    /**表情布局是否显示*/
    fun isEmojiLayoutShow(): Boolean = !isSoftInputShow() && _bottomInsertHeight > 0

    //</editor-fold desc="操作方法">

    inner class DelayHandleRunnable(val action: Int, val height: Int) : Runnable {
        override fun run() {
            handleSoftInput(action, height)
            removeDelayHandle()
        }
    }
}

//显示键盘or表情
fun Int.isShowAction() = this == ACTION_SHOW_SOFT_INPUT || this == ACTION_SHOW_EMOJI

fun Int.isHideAction() = this == ACTION_HIDE_SOFT_INPUT || this == ACTION_HIDE_EMOJI

//显示键盘的操作
fun Int.isSoftInputShowAction() = this == ACTION_SHOW_SOFT_INPUT

//显示表情的操作
fun Int.isEmojiShowAction() = this == ACTION_SHOW_EMOJI

fun Int.softAction(): String = when (this) {
    ACTION_SHOW_SOFT_INPUT -> "SHOW_SOFT_INPUT"
    ACTION_HIDE_SOFT_INPUT -> "HIDE_SOFT_INPUT"
    ACTION_SHOW_EMOJI -> "SHOW_EMOJI"
    ACTION_HIDE_EMOJI -> "HIDE_EMOJI"
    else -> "Unknown"
}

interface OnSoftInputListener {

    /**处理之前, 通过[action]判断是隐藏/还是显示键盘*/
    fun onSoftInputChangeStart(action: Int, height: Int, oldHeight: Int) {}

    /**处理之后, 通过[action]判断是隐藏/还是显示键盘*/
    fun onSoftInputChangeEnd(action: Int, height: Int, oldHeight: Int) {}

    /**动画处理中, 只有开启动画才会回调*/
    fun onSoftInputChange(action: Int, height: Int, oldHeight: Int, fraction: Float) {}
}

/**键盘/表情, 显示完成之后*/
fun DslSoftInputLayout.onSoftInputChangeStart(action: (action: Int, height: Int, oldHeight: Int) -> Unit) {
    addSoftInputListener(object : OnSoftInputListener {
        override fun onSoftInputChangeStart(action: Int, height: Int, oldHeight: Int) {
            action(action, height, oldHeight)
        }
    })
}

/**键盘/表情, 显示完成之后*/
fun DslSoftInputLayout.onSoftInputChangeEnd(action: (action: Int, height: Int, oldHeight: Int) -> Unit) {
    addSoftInputListener(object : OnSoftInputListener {
        override fun onSoftInputChangeEnd(action: Int, height: Int, oldHeight: Int) {
            action(action, height, oldHeight)
        }
    })
}