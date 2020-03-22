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
import com.angcyo.library._screenHeight
import com.angcyo.library.ex.getContentViewHeight
import com.angcyo.library.ex.undefined_int
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.bgColorAnimator
import com.angcyo.widget.base.getChildOrNull
import com.angcyo.widget.base.setBgDrawable
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/15
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class PopupConfig {
    /**
     * 标准需要显示的属性
     * */
    var anchor: View? = null

    /**
     * 使用此属性, 将会使用 showAtLocation(View parent, int gravity, int x, int y) 显示window
     *
     * 相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
     * */
    var parent: View? = null

    var xoff: Int = 0
    var yoff: Int = 0

    //此属性 似乎只在 showAtLocation 有效, 在showAsDropDown中, anchor完全在屏幕底部, 系统会控制在TOP显示, 手动控制无效
    var gravity = Gravity.NO_GRAVITY//Gravity.TOP or Gravity.START or Gravity.LEFT

    /** 标准属性 */
    var contentView: View? = null

    /** 指定布局id */
    var layoutId: Int = -1

    var height = WindowManager.LayoutParams.WRAP_CONTENT
    var width = WindowManager.LayoutParams.WRAP_CONTENT
    var focusable = true
    var touchable = true
    var outsideTouchable = true
    var background: Drawable? = null

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
    var onDismiss: (window: Any) -> Boolean = { false }

    var onInitLayout: (window: Any, viewHolder: DslViewHolder) -> Unit =
        { _, _ -> }

    /**显示, 根据条件, 选择使用[PopupWindow]or[Activity]载体*/
    open fun show(context: Context): Any {
        return if (showWithActivity && context is Activity) {
            showWidthActivity(context)
        } else {
            showWithPopupWindow(context)
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

            val view = createContentView(context)

            val popupViewHolder = DslViewHolder(view!!)
            initPopupWindow(window, popupViewHolder)
            onInitLayout(window, popupViewHolder)

            contentView = view
        }

        if (parent != null) {
            window.showAtLocation(parent, gravity, xoff, yoff)
        } else {
            PopupWindowCompat.showAsDropDown(window, anchor!!, xoff, yoff, gravity)
        }

        return window
    }

    open fun initPopupWindow(popupWindow: PopupWindow, popupViewHolder: DslViewHolder) {

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
        //创建内容布局
        val contentLayout = createContentView(activity)

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
        val viewHolder = DslViewHolder(rootLayout)
        onAddRootLayout(activity, viewHolder)

        onInitLayout(window, viewHolder)

        return window
    }

    /**透明颜色变暗透明度, [PopupWindow]不支持此属性*/
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