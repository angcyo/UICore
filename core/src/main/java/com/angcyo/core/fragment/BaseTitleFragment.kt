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

    //<editor-fold desc="操作属性">

    var viewResConfig: ViewResConfig = ViewResConfig()

    /**标题*/
    var fragmentTitle: CharSequence? = null
        set(value) {
            field = value
            baseViewHolder.tv(R.id.lib_title_text_view)?.text = value
        }

    //</editor-fold desc="操作属性">

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getUiFragment().initFragment(this)
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        initTitleFragment()
    }

    //<editor-fold desc="操作方法">

    /**Fragment根布局*/
    override fun getFragmentLayoutId(): Int = R.layout.lib_title_fragment_layout

    /**自定义内容布局*/
    open fun getContentLayoutId(): Int = -1

    /**自定义标题栏布局*/
    open fun getTitleLayoutId(): Int = -1

    open fun initTitleFragment() {
        if (getContentLayoutId() > 0) {
            baseViewHolder.group(R.id.lib_content_wrap_layout)?.inflate(getContentLayoutId())
        }
        if (getTitleLayoutId() > 0) {
            baseViewHolder.group(R.id.lib_title_wrap_layout)?.inflate(getTitleLayoutId())
        }

        titleControl()?.apply {
            setBackground(viewResConfig.titleBarBackgroundDrawable)
            selector(R.id.lib_title_text_view)
            setTextSize(viewResConfig.titleTextSize)
            setTextColor(viewResConfig.titleTextColor)
        }

        leftControl()?.apply {
            setDrawableColor(viewResConfig.titleItemIconColor)
            setTextColor(viewResConfig.titleItemTextColor)
        }

        rightControl()?.apply {
            setDrawableColor(viewResConfig.titleItemIconColor)
            setTextColor(viewResConfig.titleItemTextColor)
        }

        rootControl().setBackground(viewResConfig.fragmentBackgroundDrawable)

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