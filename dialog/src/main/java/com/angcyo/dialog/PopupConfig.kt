package com.angcyo.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.view.doOnPreDraw
import androidx.core.widget.PopupWindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.angcyo.dsladapter.getViewRect
import com.angcyo.library.IActivityProvider
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.bgColorAnimator
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.getChildOrNull
import com.angcyo.library.ex.getContentViewHeight
import com.angcyo.library.ex.have
import com.angcyo.library.ex.mH
import com.angcyo.library.ex.mW
import com.angcyo.library.ex.setBgDrawable
import com.angcyo.library.ex.undefined_int
import com.angcyo.library.ex.undefined_res
import com.angcyo.lifecycle.onDestroy
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.atMost
import com.angcyo.widget.base.setDslViewHolder
import com.angcyo.widget.base.tagDslViewHolder
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
 * [Dialog]
 * [Activity]
 * */
typealias TargetWindow = Any

/**销毁[TargetWindow]*/
fun TargetWindow.dismissWindow() {
    when (this) {
        is PopupWindow -> dismiss()
        is Dialog -> dismiss()
        is Window -> Unit
        is Activity -> finish()
        is DslDialogConfig -> _dialog?.dismissWindow()
    }
}

open class PopupConfig : ActivityResultCaller, LifecycleOwner, IActivityProvider {
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

    /**设置给[PopupWindow]的属性, 此属性会自动赋值
     * 更好的自定义设置属性[offsetX] [offsetY]*/
    var xoff: Int = 0
    var yoff: Int = 0

    /**额外的偏移量,
     * 为了兼容 [updatePopup]*/
    var offsetX: Int = 0

    /**额外的偏移量, 会根据锚点在屏幕的上下区域, 自动取反*/
    var offsetY: Int = 0

    /**左右最小边界距离*/
    var minHorizontalOffset = 20 * dpi //最小边距

    /**上下最小边界距离*/
    var minVerticalOffset = 0 //最小边距

    //此属性 似乎只在 showAtLocation 有效, 在showAsDropDown中, anchor完全在屏幕底部, 系统会控制在TOP显示, 手动控制无效
    //此属性在showAtLocation中配合parent效果才会达到预期
    //如果是anchor,那么效果有点差异.
    var gravity: Int = Gravity.NO_GRAVITY//Gravity.TOP or Gravity.START or Gravity.LEFT

    /**当前[PopupWindow]是否是在[Dialog]中显示*/
    var isShowInDialogWindows: Boolean = false
    val inDialogOffsetY
        get() = if (isShowInDialogWindows && gravity.have(Gravity.TOP)) {
            -(rootViewRect.height() + anchorViewRect.height())
        } else {
            0
        }

    /**自动调整偏移到
     * [anchor]的 TOP_CENTER or BOTTOM_CENTER
     * 此值不会覆盖 [xoff] [yoff] 而是追加*/
    var autoOffset: Boolean = false
        set(value) {
            field = value
            if (value) {
                gravity = Gravity.LEFT // 默认在锚点目标的左下角对齐显示
            }
        }

    /**自动设置offset, 到达屏幕横向居中的状态,
     * 否则就是目标横向居中*/
    var autoOffsetCenterInScreen: Boolean = false

    /**自动设置offset, 到达锚点横向居中的状态*/
    var autoOffsetCenterInAnchor: Boolean = true

    /**是否根据锚点的中心位置, 自动设置[gravity]*/
    var autoAdjustGravity: Boolean = true

    /** 标准属性
     * [com.angcyo.dialog.PopupConfig.createContentView]*/
    var contentView: View? = null

    /** 指定布局id */
    @LayoutRes
    var popupLayoutId: Int = -1

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
     * 动画样式, 0:表示没有动画, -1:表示默认动画.
     * [android.widget.PopupWindow.setAnimationStyle]
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
     * 回调, 是否要拦截默认操作.
     * [showWithPopupWindow]时有效
     * [showWithActivity]时是在[com.angcyo.dialog.PopupConfig.onRemoveRootLayout]中回调
     *
     * - [isDestroyed]
     * - [onDismiss]
     * */
    var onDismiss: (window: TargetWindow) -> Boolean = { false }


    /**
     * [window] :[PopupWindow] or [Window]
     * */
    var onInitLayout: (window: TargetWindow, viewHolder: DslViewHolder) -> Unit =
        { _, _ -> }

    /**[DslViewHolder]*/
    var _popupViewHolder: DslViewHolder? = null

    /**[PopupWindow]or[Window]载体*/
    var _container: TargetWindow? = null

    //---

    /**显示, 根据条件, 选择使用[PopupWindow]or[Window]载体*/
    @CallPoint
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

    /**显示在[view]的底部*/
    fun showOnViewBottom(view: View) {
        animationStyle = R.style.LibPopupBottomAnimation
        parent = view
        gravity = Gravity.BOTTOM
        width = WindowManager.LayoutParams.MATCH_PARENT
    }

    //<editor-fold desc="PopupWindow">

    /**显示[PopupWindow]
     *
     * - [updatePopup] 更新位置时调用
     * */
    @CallPoint
    open fun showWithPopupWindow(context: Context): PopupWindow {
        onPopupInit()

        val window = if (popupStyleAttr != undefined_res) {
            try {
                PopupWindow(context, null, popupStyleAttr, popupStyleAttr)
            } catch (e: Exception) {
                PopupWindow(context, null, popupStyleAttr)
            }
        } else {
            PopupWindow(context)
        }

        onPopupCreate(window)

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

            //dismiss
            setOnDismissListener {
                dismissPopupWindow(window)
            }

            //创建布局和初始化
            val view = createContentView(context)
            val popupViewHolder = DslViewHolder(view!!)
            _popupViewHolder = popupViewHolder
            view.setDslViewHolder(popupViewHolder)
            initPopupWindow(window, popupViewHolder)

            onInitLayout(window, popupViewHolder)
            initLayout(window, popupViewHolder)//init 配置位置

            contentView = view

            onPopupShow(window, popupViewHolder)
        }

        if (parent != null) {
            window.showAtLocation(parent, gravity, xoff + offsetX, yoff + offsetY + inDialogOffsetY)
        } else if (anchor != null) {
            //当空间不够时, 系统会自动偏移到最佳位置, 此时xoff也会额外追加偏移计算中
            // Gravity.LEFT / Gravity.RIGHT 有效
            // Gravity.CENTER 属性无效, 还是已左上角为计算锚点
            PopupWindowCompat.showAsDropDown(
                window,
                anchor!!,
                xoff + offsetX,
                yoff + offsetY + inDialogOffsetY,
                gravity
            )
        } else {
            L.w("至少需要配置一项[parent]or[anchor]")
        }

        return window
    }

    /**[showWithPopupWindow]*/
    open fun initPopupWindow(popupWindow: PopupWindow, popupViewHolder: DslViewHolder) {

    }

    /** 更新[PopupWindow]的坐标
     * 通过[xoff] [yoff] 更新位置*/
    open fun updatePopup(updateLayout: Boolean = true) {
        val window = _container
        if (window != null) {
            if (window is PopupWindow) {
                window.update(
                    anchor ?: parent,
                    xoff + offsetX,
                    yoff + offsetY + inDialogOffsetY,
                    width, height
                )
            }
            contentView?.tagDslViewHolder()?.let {
                onInitLayout(window, it)
            }
        }
    }

    /**销毁[PopupWindow]时触发的回调*/
    open fun dismissPopupWindow(window: TargetWindow): Boolean {
        onPopupDestroy(window, _popupViewHolder)
        return onDismiss(window)
    }

    //</editor-fold desc="PopupWindow">

    //<editor-fold desc="Core">

    open fun isAnchorInTopArea(anchor: View): Boolean {
        val rect = anchor.getViewRect()
        val s2 = _screenHeight / 2
        val s4 = _screenHeight / 4
        if (rect.bottom < s2) {
            return true
        }
        return rect.centerY() < s4
    }

    open fun isAnchorInLeftArea(anchor: View): Boolean = !isAnchorInRightArea(anchor)

    open fun isAnchorInRightArea(anchor: View): Boolean {
        val rect = anchor.getViewRect()
        return rect.left >= _screenWidth / 2 || rect.centerX() > _screenWidth / 2
    }

    open fun createContentView(context: Context): View? {
        if (popupLayoutId != -1) {
            contentView = LayoutInflater.from(context)
                .inflate(popupLayoutId, FrameLayout(context), false)
        }
        if (showWithActivity) {
            val rootLayout = FrameLayout(context)
            rootLayout.addView(contentView)
            contentView = rootLayout
        }
        return contentView
    }

    /**[autoOffset]后, 根布局所在的坐标矩形*/
    val rootViewRect = Rect()

    /**[anchor]在屏幕中的坐标*/
    val anchorViewRect = Rect()

    /**都会触发, 初始化View
     * [window] 有可能是[PopupWindow] 也有可能是*/
    open fun initLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        val anchorView = anchor
        if (anchorView != null && autoOffset) {

            val rootView = viewHolder.itemView
            val lp = rootView.layoutParams

            val leftMargin: Int = if (lp is ViewGroup.MarginLayoutParams) {
                max(lp.leftMargin, minHorizontalOffset)
            } else {
                minHorizontalOffset
            }
            val rightMargin: Int = if (lp is ViewGroup.MarginLayoutParams) {
                max(lp.rightMargin, minHorizontalOffset)
            } else {
                minHorizontalOffset
            }

            val topMargin: Int = if (lp is ViewGroup.MarginLayoutParams) {
                max(lp.topMargin, minVerticalOffset)
            } else {
                minVerticalOffset
            }
            val bottomMargin: Int = if (lp is ViewGroup.MarginLayoutParams) {
                max(lp.bottomMargin, minVerticalOffset)
            } else {
                minVerticalOffset
            }

            //宽高计算
            val _width = if (width == WindowManager.LayoutParams.WRAP_CONTENT ||
                width == WindowManager.LayoutParams.MATCH_PARENT
            ) {
                _screenWidth
            } else {
                width
            }
            val _height = if (height == WindowManager.LayoutParams.WRAP_CONTENT ||
                height == WindowManager.LayoutParams.MATCH_PARENT
            ) {
                _screenHeight
            } else {
                width
            }
            rootView.measure(
                atMost(_width - (leftMargin + rightMargin)),
                atMost(_height - (topMargin + bottomMargin))
            )
            val rootViewWidth = rootView.mW()
            val rootViewHeight = rootView.mH()

            //固定宽度
            if (window is PopupWindow) {
                window.width = rootViewWidth
                window.height = rootViewHeight
            }

            val anchorViewRect = anchorView.getViewRect(anchorViewRect)
            if (isAnchorInTopArea(anchorView)) {
                //目标在屏幕的上半区
            } else {
                //目标在屏幕的下半区
                yoff = -(anchorViewRect.height() + rootViewHeight) - yoff
                offsetY = -offsetY
            }
            rootViewRect.top = anchorViewRect.bottom + yoff
            rootViewRect.bottom = rootViewRect.top + rootViewHeight
            _updateRootViewRectLeft(xoff, rootViewWidth)

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
            } else if (autoAdjustGravity) {
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
            } else {
                //no op
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

    //</editor-fold desc="Core">

    //<editor-fold desc="PopupActivity">

    /**使用[Activity]当做载体*/
    open fun showWidthActivity(activity: Activity): Window {
        onPopupInit()

        val window = activity.window

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

        onPopupCreate(window)

        val windowLayout = window.findViewById<FrameLayout>(Window.ID_ANDROID_CONTENT)

        //创建内容布局, 和初始化
        val contentLayout = createContentView(activity)
        val viewHolder = DslViewHolder(contentLayout!!)
        _popupViewHolder = viewHolder
        initPopupActivity(activity, viewHolder)
        onInitLayout(window, viewHolder)
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

        onPopupShow(window, viewHolder)
        return window
    }

    /**[showWidthActivity]*/
    open fun initPopupActivity(activity: Activity, popupViewHolder: DslViewHolder) {

    }

    var _onBackPressedCallback: OnBackPressedCallback? = null

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
            if (!dismissPopupWindow(activity)) {
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

    //</editor-fold desc="PopupActivity">

    override fun getActivityContext(): Context? =
        contentView?.context ?: anchor?.context ?: parent?.context ?: lastContext

    /**移除[Activity]模式的界面
     * 需要[OnBackPressedDispatcherOwner]支持*/
    fun hide() {
        _onBackPressedCallback?.handleOnBackPressed()
        _container?.let {
            if (it is PopupWindow) {
                it.dismiss()
            }
        }
    }

    override fun <I : Any?, O : Any?> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        error("未实现:registerForActivityResult")
    }

    override fun <I : Any?, O : Any?> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
        registry: ActivityResultRegistry,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        error("未实现:registerForActivityResult")
    }

    //<editor-fold desc="Lifecycle支持">

    /**监听声明周期*/
    var _lifecycleObserver: LifecycleEventObserver? = null

    val lifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    /**当前窗口是否被销毁
     * - [isDestroyed]
     * - [onDismiss]
     * */
    val isDestroyed: Boolean
        get() = lifecycle.currentState == Lifecycle.State.DESTROYED

    @CallSuper
    open fun onPopupInit() {
        //防止activity销毁时, dialog泄漏
        val activityContext = getActivityContext()
        if (activityContext is LifecycleOwner) {
            val observer: LifecycleEventObserver = (activityContext as LifecycleOwner).onDestroy {
                _container?.dismissWindow()
                true
            }
            _lifecycleObserver = observer
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    @CallSuper
    open fun onPopupCreate(window: TargetWindow) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    @CallSuper
    open fun onPopupShow(window: TargetWindow, viewHolder: DslViewHolder) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    /**[android.content.DialogInterface.OnDismissListener]*/
    @CallSuper
    open fun onPopupDestroy(window: TargetWindow, viewHolder: DslViewHolder?) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _lifecycleObserver?.let { observer ->
            val activityContext = getActivityContext()
            if (activityContext is LifecycleOwner) {
                activityContext.lifecycle.removeObserver(observer)
            }
        }
        viewHolder?.clear()
    }

    //</editor-fold desc="Lifecycle支持">
}