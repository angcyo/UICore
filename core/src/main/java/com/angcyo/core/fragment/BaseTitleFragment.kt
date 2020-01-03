package com.angcyo.core.fragment

import android.os.Bundle
import com.angcyo.core.R
import com.angcyo.widget.DslGroupHelper
import com.angcyo.widget.base.inflate

/**
 * Email:angcyo@126.com
 * 统一标题管理的Fragment
 *
 * @author angcyo
 * @date 2018/12/07
 */
abstract class BaseTitleFragment : BaseFragment() {

    init {
        /**Fragment根布局*/
        fragmentLayoutId = R.layout.lib_title_fragment_layout
    }

    /**自定义内容布局*/
    var contentLayoutId: Int = -1

    /**自定义标题栏布局*/
    var titleLayoutId: Int = R.layout.lib_title_bar_layout

    //<editor-fold desc="操作属性">

    var fragmentConfig: FragmentConfig = FragmentConfig()

    /**标题*/
    var fragmentTitle: CharSequence? = null
        set(value) {
            field = value
            baseViewHolder.tv(R.id.lib_title_text_view)?.text = value
        }

    //</editor-fold desc="操作属性">

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseUI.onFragmentCreateAfter(this, fragmentConfig)
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        initTitleFragment()

        BaseUI.onFragmentInitBaseViewAfter(this)
    }

    //<editor-fold desc="操作方法">

    open fun initTitleFragment() {
        baseViewHolder.itemView.isClickable = fragmentConfig.interceptRootTouchEvent

        if (contentLayoutId > 0) {
            baseViewHolder.group(R.id.lib_content_wrap_layout)?.inflate(contentLayoutId)
        }
        if (titleLayoutId > 0) {
            baseViewHolder.group(R.id.lib_title_wrap_layout)?.inflate(titleLayoutId)
        }

        titleControl()?.apply {
            setBackground(fragmentConfig.titleBarBackgroundDrawable)
            selector(R.id.lib_title_text_view)
            setTextSize(fragmentConfig.titleTextSize)
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

        fragmentTitle = this.javaClass.simpleName
    }

    /**常用控制助手*/
    open fun titleControl(): DslGroupHelper? =
        baseViewHolder.view(R.id.lib_title_wrap_layout)?.run { DslGroupHelper(this) }

    open fun leftControl(): DslGroupHelper? =
        baseViewHolder.view(R.id.lib_left_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rightControl(): DslGroupHelper? =
        baseViewHolder.view(R.id.lib_right_wrap_layout)?.run { DslGroupHelper(this) }

    open fun rootControl(): DslGroupHelper = DslGroupHelper(baseViewHolder.itemView)

    open fun contentControl(): DslGroupHelper? =
        baseViewHolder.view(R.id.lib_content_wrap_layout)?.run { DslGroupHelper(this) }

    //</editor-fold desc="操作方法">
}