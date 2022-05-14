package com.angcyo.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.core.view.doOnPreDraw
import androidx.core.widget.PopupWindowCompat
import com.angcyo.dsladapter.getViewRect
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.*
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.*
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/15
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**window中的点击回调事件*/
typealias WindowClickAction = (window: TargetWindow, view: View) -> Unit

/**指定类型是以下类型:
 * [Window]
 * [PopupWindow]
 * */
typealias TargetWindow = Any

open class PopupConfig {
    /**
     * 标准需要显示的属性, 使用[showAsDropDown]显示
     *
     * Gravity.LEFT  左下角对齐
     * Gravity.RIGHT 右下角对齐
     * Gravity.TOP 和 Gravity.BOTTOM 不起作用
     *
     * https://www.jianshu.com/p/0115713cca49
     * */
    var anchor: View? = null

    /**
     * 优先级高, [anchor] [parent] 至少需要配置一个
     * 使用此属性, 将会使用 showAtLocation(View parent, int gravity, int x, int y) 显示window
     *
     * 相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
     * */
    var parent: View? = null

    var xoff: Int = 0
    var yoff: Int = 0

    /**左右最小边界距离*/
    var minHorizontalOffset = 20 * dpi //最小边距

    /**上下最小边界距离*/
    var minVerticalOffset = 0 //最小边距

    //此属性 似乎只在 showAtLocation 有效, 在showAsDropDown中, anchor完全在屏幕底部, 系统会控制在TOP显示, 手动控制无效
    //此属性在showAtLocation中配合parent效果才会达到预期
    //如果是anchor,那么效果有点差异.
    var gravity: Int = Gravity.NO_GRAVITY//Gravity.TOP or Gravity.START or Gravity.LEFT

    /**自动调整偏移到
     * [anchor]的 TOP_CENTER or BOTTOM_CENTER
     * 此值不会覆盖 [xoff] [yoff] 而是追加*/
    var autoOffset: Boolean = false
        set(value) {
            field = value
            gravity = Gravity.LEFT // 默认左下角对齐
        }

    /**自动设置offset, 到达屏幕横向居中的状态,
     * 否则就是目标横向居中*/
    var autoOffsetCenterInScreen: Boolean = false

    /**自动设置offset, 到达锚点横向居中的状态*/
    var autoOffsetCenterInAnchor: Boolean = true

    /** 标准属性 */
    var contentView: View? = null

    /** 指定布局id */
    var layoutId: Int = -1

    //<editor-fold desc="Popup属性">

    var height: Int = WindowManager.LayoutParams.WRAP_CONTENT
    var width: Int = WindowManager.LayoutParams.WRAP_CONTENT
    var focusable = true//是否需要焦点
    var touchable = true//是否需要按压事件
    var outsideTouchable = true//是否需要窗口外的事件
    var background: Drawable? = null//背景

    //</editor-fold desc="Popup属性">

    /**将[height]设置为, 锚点距离屏幕*/
    var exactlyHeight = false

    /**
     * 动画样式, 0 表示没有动画, -1 表示 默认动画.
     * */
    var animationStyle = R.style.LibPopupAnimation

    /**[androidx.appcompat.R.attr.listPopupWindowStyle]
     * <style name="Widget.Material.ListPopupWindow">
     * <item name="dropDownSelector">?attr/listChoiceBackgroundIndicator</item>
     * <item name="popupBackground">@drawable/popup_background_material</item>
     * <item name="popupElevation">@dimen/floating_window_z</item>
     * <item name="popupAnimationStyle">@empty</item>
     * <item name="popupEnterTransition">@transition/popup_window_enter</item>
     * <item name="popupExitTransition">@transition/popup_window_exit</item>
     * <item name="dropDownVerticalOffset">0dip</item>
     * <item name="dropDownHorizontalOffset">0dip</item>
     * <item name="dropDownWidth">wrap_content</item>
     * </style>
     * */
    //R.style.LibPopupWindowStyle
    var popupStyleAttr: Int = undefined_res

    /**使用[Activity]当做布局载体, 而不是[PopupWindow]*/
    var showWithActivity: Boolean = false

    /**android.widget.PopupWindow.setWindowLayoutType*/
    var windowType: Int = undefined_int

    /**
     * 回调, 是否要拦截默认操作.[showWithActivity]时有效
     * */
    var onDismiss: (window: TargetWindow) -> Boolean = { false }

    /**
     * [window] :[PopupWindow] or [Window]
     * */
    var onInitLayout: (window: TargetWindow, viewHolder: DslViewHolder) -> Unit =
        { _, _ -> }

    /**[PopupWindow]or[Window]载体*/
    var _container: TargetWindow? = null

    /**显示, 根据条件, 选择使用[PopupWindow]or[Window]载体*/
    open fun show(context: Context): TargetWindow {
        return if (showWithActivity && context is Activity) {
            showWidthActivity(context).apply {
                _container = this
            }
        } else {
            showWithPopupWindow(context).apply {
                _container = this
            }
        }
    }

    /**显示[PopupWindow]*/
    open fun showWithPopupWindow(context: Context): PopupWindow {
        val window = if (popupStyleAttr != undefined_res) {
            try {
                PopupWindow(context, null, popupStyleAttr, popupStyleAttr)
            } catch (e: Exception) {
                PopupWindow(context, null, popupStyleAttr)
            }
        } else {
            PopupWindow(context)
        }

        window.apply {
            inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

            width = this@PopupConfig.width
            height = this@PopupConfig.height

            if (windowType != undefined_int) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    windowLayoutType = windowType
                }
            }

            anchor?.let {
                val viewRect = it.getViewRect()
                if (exactlyHeight) {
                    height = max(
                        it.context.getContentViewHeight(),
                        _screenHeight
                    ) - viewRect.bottom
                }

                if (viewRect.bottom >= _screenHeight) {
                    //接近屏幕底部
                    if (this@PopupConfig.gravity == Gravity.NO_GRAVITY) {
                        //手动控制无效
                        //gravity = Gravity.TOP

                        if (exactlyHeight) {
                            height = viewRect.top
                        }
                    }
                }
            }

            isFocusable = focusable
            isTouchable = touchable
            isOutsideTouchable = outsideTouchable
            setBackgroundDrawable(this@PopupConfig.background)

            animationStyle = this@PopupConfig.animationStyle

            setOnDismissListener {
                onDismiss(window)
            }

            //创建布局和初始化
            val view = createContentView(context)
            val popupViewHolder = DslViewHolder(view!!)
            initPopupWindow(window, popupViewHolder)
            initLayout(window, popupViewHolder)

            onInitLayout(window, popupViewHolder)

            contentView = view
        }

        if (parent != null) {
            window.showAtLocation(parent, gravity, xoff, yoff)
        } else if (anchor != null) {
            //当空间不够时, 系统会自动偏移到最佳位置, 此时xoff也会额外追加偏移计算中
            // Gravity.LEFT / Gravity.RIGHT 有效
            // Gravity.CENTER 属性无效, 还是已左上角为计算锚点
            PopupWindowCompat.showAsDropDown(window, anchor!!, xoff, yoff, gravity)
        } else {
            L.w("至少需要配置一项[parent]or[anchor]")
        }

        return window
    }

    open fun isAnchorInTopArea(anchor: View): Boolean {
        val rect = anchor.getViewRect()
        return rect.centerY() < _screenHeight / 4
    }

    open fun isAnchorInLeftArea(anchor: View): Boolean = !isAnchorInRightArea(anchor)

    open fun isAnchorInRightArea(anchor: View): Boolean {
        val rect = anchor.getViewRect()
        return rect.left >= _screenWidth / 2 || rect.centerX() > _screenWidth / 2
    }

    /**[autoOffset]后, 根布局所在的坐标矩形*/
    val rootViewRect = Rect()

    /**都会触发, 初始化View
     * [window] 有可能是[PopupWindow] 也有可能是*/
    open fun initLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        val anchorView = anchor
        if (anchorView != null && autoOffset) {

            val rootView = viewHolder.itemView
            rootView.measure(
                atMost(_screenWidth - minHorizontalOffset * 2),
                atMost(_screenHeight - minVerticalOffset * 2)
            )
            val rootViewWidth = rootView.mW()
            val rootViewHeight = rootView.mH()

            //固定宽度
            if (window is PopupWindow) {
                window.width = rootViewWidth
                window.height = rootViewHeight
            }

            val anchorViewRect = anchorView.getViewRect()
            if (isAnchorInTopArea(anchorView)) {
                //目标在屏幕的上半区
            } else {
                //目标在屏幕的下半区
                yoff = -(anchorViewRect.height() + rootViewHeight) - yoff
            }
            rootViewRect.top = anchorViewRect.bottom + yoff
            rootViewRect.bottom = rootViewRect.top + rootViewHeight

            //计算横向偏移
            if (autoOffsetCenterInScreen) {
                //优先显示在屏幕横向居中的位置
                rootViewRect.left = _screenWidth / 2 - rootViewWidth / 2 + xoff
                rootViewRect.right = rootViewRect.left + rootViewWidth

                if (isAnchorInLeftArea(anchorView)) {
                    gravity = Gravity.LEFT
                    xoff += _screenWidth / 2 - anchorViewRect.left - rootViewWidth / 2

                    _updateRootViewRectLeft(anchorViewRect.left + xoff, rootViewWidth)
                } else {
                    gravity = Gravity.RIGHT
                    xoff -= anchorViewRect.right - (_screenWidth / 2 + rootViewWidth / 2)

                    _updateRootViewRectRight(anchorViewRect.right + xoff, rootViewWidth)
                }
            } else if (autoOffsetCenterInAnchor) {
                //优先显示在锚点横向居中的位置
                val originOffset = xoff
                if (isAnchorInLeftArea(anchorView)) {
                    gravity = Gravity.LEFT

                    rootViewRect.left = anchorViewRect.left
                    rootViewRect.right = anchorViewRect.left + rootViewWidth

                    val dx = rootViewRect.centerX() - anchorViewRect.centerX()
                    val newLeft = rootViewRect.left - dx

                    xoff = if (newLeft >= minHorizontalOffset) {
                        //够空间
                        -dx
                    } else {
                        minHorizontalOffset - rootViewRect.left
                    } + originOffset

                    _updateRootViewRectLeft(anchorViewRect.left + xoff, rootViewWidth)
                } else {
                    gravity = Gravity.RIGHT
                    rootViewRect.right = anchorViewRect.right
                    rootViewRect.left = rootViewRect.right - rootViewWidth

                    val dx = anchorViewRect.centerX() - rootViewRect.centerX()
                    val newRight = rootViewRect.right + dx

                    xoff = if (newRight <= _screenWidth - minHorizontalOffset) {
                        //够空间
                        dx
                    } else {
                        (_screenWidth - minHorizontalOffset) - anchorViewRect.right
                    } + originOffset

                    _updateRootViewRectRight(anchorViewRect.right + xoff, rootViewWidth)
                }
            } else {
                //优先目标横向居右的位置
                if (isAnchorInLeftArea(anchorView)) {
                    gravity = Gravity.LEFT
                    /*if (rect.left > minOffset) {
                        xoff += rect.width() / 2 - rootViewWidth / 2
                    }*/
                    val originOffset = xoff
                    xoff += anchorViewRect.width() / 2 - rootViewWidth / 2
                    if (anchorViewRect.left + xoff < 0) {
                        //越界了
                        xoff = originOffset
                    }

                    _updateRootViewRectLeft(anchorViewRect.left + xoff, rootViewWidth)
                } else {
                    gravity = Gravity.RIGHT
                    /*if (rect.right + minOffset < _screenWidth) {
                        xoff += rect.width() / 2 - rootViewWidth / 2
                    }*/
                    val originOffset = xoff

                    xoff -= anchorViewRect.width() / 2 - rootViewWidth / 2
                    if (anchorViewRect.right + xoff > _screenWidth) {
                        //越界了, 不偏移
                        xoff = originOffset
                    }

                    _updateRootViewRectRight(anchorViewRect.right + xoff, rootViewWidth)
                }
            }
        }
    }

    fun _updateRootViewRectLeft(left: Int, width: Int) {
        rootViewRect.left = left
        rootViewRect.right = left + width
    }

    fun _updateRootViewRectRight(right: Int, width: Int) {
        rootViewRect.right = right
        rootViewRect.left = right - width
    }

    /**[showWithPopupWindow]*/
    open fun initPopupWindow(popupWindow: PopupWindow, popupViewHolder: DslViewHolder) {

    }

    /**[showWidthActivity]*/
    open fun initPopupActivity(activity: Activity, popupViewHolder: DslViewHolder) {

    }

    open fun createContentView(context: Context): View? {
        if (layoutId != -1) {
            contentView = LayoutInflater.from(context)
                .inflate(layoutId, FrameLayout(context), false)
        }
        if (showWithActivity) {
            val rootLayout = FrameLayout(context)
            rootLayout.addView(contentView)
            contentView = rootLayout
        }
        return contentView
    }

    var _onBackPressedCallback: OnBackPressedCallback? = null

    /**使用[Activity]当做载体*/
    open fun showWidthActivity(activity: Activity): Window {

        //拦截掉[Activity]的[BackPress], 需要[BaseAppCompatActivity]的支持
        _onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                onRemoveRootLayout(activity)
            }
        }

        if (activity is OnBackPressedDispatcherOwner) {
            activity.onBackPressedDispatcher.addCallback(_onBackPressedCallback!!)
        }

        val window = activity.window

        val windowLayout = window.findViewById<FrameLayout>(Window.ID_ANDROID_CONTENT)

        //创建内容布局, 和初始化
        val contentLayout = createContentView(activity)
        val viewHolder = DslViewHolder(contentLayout!!)
        initPopupActivity(activity, viewHolder)
        initLayout(window, viewHolder)

        //全屏覆盖层
        val rootLayout = FrameLayout(activity)
        rootLayout.layoutParams = FrameLayout.LayoutParams(-1, -1)
        rootLayout.setBgDrawable(background)

        //模拟点击外隐藏
        rootLayout.setOnClickListener {
            if (outsideTouchable) {
                _onBackPressedCallback?.handleOnBackPressed()
            }
        }

        //进行锚点位置纠正偏移
        val anchorRect = Rect(0, 0, 0, 0)
        anchor?.let {
            val viewRect = it.getViewRect()
            anchorRect.set(viewRect)
            if (exactlyHeight) {
                height = max(
                    it.context.getContentViewHeight(),
                    _screenHeight
                ) - viewRect.bottom
            }

            if (viewRect.bottom >= _screenHeight) {
                //接近屏幕底部
                if (this@PopupConfig.gravity == Gravity.NO_GRAVITY) {
                    gravity = Gravity.TOP

                    if (exactlyHeight) {
                        height = viewRect.top
                    }
                }
            }
        }

        //addView
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(width, height, gravity).apply {
            leftMargin = xoff

            if (width != -1) {
                leftMargin += anchorRect.left
            }

            topMargin = yoff + anchorRect.bottom
        })

        contentView = rootLayout

        windowLayout.addView(contentView)

        //回调
        onAddRootLayout(activity, viewHolder)

        onInitLayout(window, viewHolder)

        return window
    }

    /**
     * 透明颜色变暗透明度, [PopupWindow]不支持此属性
     * */
    var amount: Float = 0.8f
    var animatorDuration = 300L

    open fun onAddRootLayout(activity: Activity, viewHolder: DslViewHolder) {
        val backgroundLayout = (contentView as? ViewGroup)?.getChildOrNull(0)
        val contentWrapLayout = (backgroundLayout as? ViewGroup)?.getChildOrNull(0)

        //背景动画
        backgroundLayout?.bgColorAnimator(
            Color.parseColor("#00000000"),
            Color.argb((255 * amount).toInt(), 0, 0, 0),
            duration = animatorDuration
        )

        fun View.doAnimate() {
            if (measuredHeight <= 0) {
                doOnPreDraw {
                    doAnimate()
                }
            } else {
                translationY = (-measuredHeight).toFloat()
                animate().translationY(0f).setDuration(animatorDuration).start()
            }
        }

        //内容动画
        contentWrapLayout?.doAnimate()
    }

    open fun onRemoveRootLayout(activity: Activity) {
        _onBackPressedCallback?.isEnabled = false
        contentView?.run {
            val window = activity.window
            if (!onDismiss(window)) {
                val windowLayout = window.findViewById<FrameLayout>(Window.ID_ANDROID_CONTENT)
                val backgroundLayout = (contentView as? ViewGroup)?.getChildOrNull(0)
                val contentWrapLayout = (backgroundLayout as? ViewGroup)?.getChildOrNull(0)

                //背景动画
                backgroundLayout?.bgColorAnimator(
                    Color.argb((255 * amount).toInt(), 0, 0, 0),
                    Color.parseColor("#00000000"),
                    duration = animatorDuration
                )

                //内容动画
                contentWrapLayout?.run {
                    doOnPreDraw {
                        animate().translationY((-it.measuredHeight).toFloat())
                            .setDuration(animatorDuration)
                            .withEndAction { windowLayout.removeView(contentView) }
                            .start()
                    }
                } ?: windowLayout.removeView(this)
            }
        }
    }

    /**移除[Activity]模式的界面*/
    fun hide() {
        _onBackPressedCallback?.handleOnBackPressed()
    }
}