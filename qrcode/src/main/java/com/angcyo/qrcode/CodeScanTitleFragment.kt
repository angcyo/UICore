package com.angcyo.qrcode

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.angcyo.base.back
import com.angcyo.core.appendTextItem
import com.angcyo.core.fragment.BaseUI
import com.angcyo.core.fragment.FragmentConfig
import com.angcyo.core.fragment.FragmentUI
import com.angcyo.library.ex.*
import com.angcyo.widget.DslGroupHelper
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.replace
import com.angcyo.widget.span.span
import com.angcyo.widget.text.DslTextView

/**
 * 二维码扫描, 带标题界面[BaseTitleFragment]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class CodeScanTitleFragment : CodeScanFragment() {

    /**自定义内容布局*/
    var contentLayoutId: Int = -1

    /**自定义标题栏布局*/
    var titleLayoutId: Int = -1

    /**标题*/
    open var fragmentTitle: CharSequence? = null
        set(value) {
            field = value
            if (isAdded && baseViewHolder != null) {
                _vh.tv(R.id.lib_title_text_view)?.text = value
            }
        }

    var baseViewHolder: DslViewHolder? = null

    /**别名*/
    val _vh: DslViewHolder
        get() = baseViewHolder!!

    var fragmentUI: FragmentUI? = null
        get() = field ?: BaseUI.fragmentUI

    var fragmentConfig: FragmentConfig = FragmentConfig()

    override fun getLayoutId(): Int {
        return R.layout.fragment_code_scan_title_layout
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        baseViewHolder = DslViewHolder(view!!)
        initBaseView(savedInstanceState)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**初始化布局, 此时的[View]还没有[attach]*/
    open fun initBaseView(savedInstanceState: Bundle?) {
        fragmentUI?.apply {
            if (backIconDrawableId > 0) {
                leftControl()?.inflate(R.layout.lib_text_layout, true) {
                    find<TextView>(R.id.lib_text_view)?.apply {
                        id = R.id.lib_title_back_view
                        setTextColor(fragmentConfig.titleItemTextColor)
                        text = span {
                            drawable {
                                backgroundDrawable =
                                    loadDrawable(backIconDrawableId).colorFilter(fragmentConfig.titleItemIconColor)
                            }
                            if (showBackText) {
                                drawable("返回") {
                                    textSize = backTextSize
                                    marginLeft = -8 * dpi
                                    marginTop = 1 * dpi
                                    textGravity = Gravity.CENTER
                                }
                            }
                        }
                        clickIt {
                            back()
                        }
                    }
                }
            }
        }
        onInitFragment()
    }

    fun _inflateTo(wrapId: Int, layoutId: Int) {
        if (layoutId > 0) {
            _vh.visible(wrapId)
            _vh.group(wrapId)?.replace(layoutId)
        } else {
            _vh.gone(wrapId, _vh.group(wrapId)?.childCount ?: 0 <= 0)
        }
    }

    /**初始化样式*/
    open fun onInitFragment() {
        _vh.itemView.isClickable = fragmentConfig.interceptRootTouchEvent

        //内容包裹
        _inflateTo(R.id.lib_content_wrap_layout, contentLayoutId)
        //标题包裹
        _inflateTo(R.id.lib_title_wrap_layout, titleLayoutId)

        titleControl()?.apply {
            setBackground(fragmentConfig.titleBarBackgroundDrawable)
            selector(R.id.lib_title_text_view)
            setTextSize(fragmentConfig.titleTextSize)
            setTextStyle(fragmentConfig.titleTextType)
            setTextColor(fragmentConfig.titleTextColor)
        }

        leftControl()?.apply {
            setDrawableColor(fragmentConfig.titleItemIconColor)
            setTextColor(fragmentConfig.titleItemTextColor)
        }

        rightControl()?.apply {
            setDrawableColor(fragmentConfig.titleItemIconColor)
            setTextColor(fragmentConfig.titleItemTextColor)
        }

        rootControl().setBackground(fragmentConfig.fragmentBackgroundDrawable)

        fragmentTitle = fragmentTitle
    }

    /**常用控制助手*/
    open fun titleControl(): DslGroupHelper? =
        _vh.view(com.angcyo.core.R.id.lib_title_wrap_layout)?.run { DslGroupHelper(this) }

    open fun leftControl(): DslGroupHelper? =
        _vh.view(com.angcyo.core.R.id.lib_left_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rightControl(): DslGroupHelper? =
        _vh.view(com.angcyo.core.R.id.lib_right_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rootControl(): DslGroupHelper = DslGroupHelper(_vh.itemView)

    open fun contentControl(): DslGroupHelper? =
        _vh.view(com.angcyo.core.R.id.lib_content_wrap_layout)?.run { DslGroupHelper(this) }

    //<editor-fold desc="扩展方法">

    fun DslGroupHelper.appendItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        appendTextItem {
            gravity = Gravity.CENTER
            setTextColor(fragmentConfig.titleItemTextColor)
            this.text = span {

                if (ico != undefined_res) {
                    drawable {
                        backgroundDrawable =
                            loadDrawable(ico).colorFilter(fragmentConfig.titleItemIconColor)
                        textGravity = Gravity.CENTER
                    }
                }

                if (text != null) {
                    drawable(text) {
                        textGravity = Gravity.CENTER
                    }
                }
            }
            clickIt(onClick)
            this.action()
        }
    }

    fun appendLeftItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        leftControl()?.appendItem(text, ico, action, onClick)
    }

    fun appendRightItem(
        text: CharSequence? = null,
        @DrawableRes ico: Int = undefined_res,
        action: DslTextView.() -> Unit = {},
        onClick: (View) -> Unit
    ) {
        rightControl()?.appendItem(text, ico, action, onClick)
    }

    //</editor-fold desc="扩展方法">
}