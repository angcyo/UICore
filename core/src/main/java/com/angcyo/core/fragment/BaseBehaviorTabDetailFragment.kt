package com.angcyo.core.fragment

import android.graphics.Color
import android.os.Bundle
import com.angcyo.core.R
import com.angcyo.library._screenHeight
import com.angcyo.library.ex.copyDrawable
import com.angcyo.library.ex.dpi
import com.angcyo.tablayout.DslTabIndicator
import com.angcyo.widget.base.find
import com.angcyo.widget.base.replace
import com.angcyo.widget.base.setHeight
import com.angcyo.widget.tab

/**
 * 复杂的Behavior详情页, 底部是ViewPager. Pager数量等于2时, 界面显示最佳.其他情况需要额外适配.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021-6-24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseBehaviorTabDetailFragment : BasePagerFragment() {

    /**自定义背景布局*/
    var backgroundScaleLayoutId: Int = -1

    var headerLayoutId: Int = -1

    /**多少个item, 启动tab 等宽样式*/
    var equWidthTabItemCount = 3

    override var fragmentTitle: CharSequence? = null
        set(value) {
            field = value
            if (isAdded) {
                _vh.tv(R.id.lib_title_text_view)?.text = value
                _vh.tv(R.id.lib_header_title_view)?.text = value
            }
        }

    init {
        fragmentLayoutId = R.layout.lib_behavior_tab_detail_layout
        contentLayoutId = -1
        tabItemLayoutId = R.layout.lib_behavior_detail_tab_item_layout
        pages
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onInitFragment() {
        super.onInitFragment()
        initHeaderLayout()
    }

    open fun backgroundHeight(): Int {
        return _screenHeight / 3
    }

    /**初始化头部布局*/
    open fun initHeaderLayout() {
        fragmentConfig.titleItemIconColor = Color.WHITE

        //背景层
        _vh.group(R.id.lib_background_wrap_layout)?.replace(backgroundScaleLayoutId)
        //头部层
        _vh.group(R.id.lib_header_wrap_layout)?.replace(headerLayoutId)

        val backgroundHeight = backgroundHeight()
        val headerIndent = 20 * dpi
        _vh.view(R.id.lib_background_wrap_layout)?.setHeight(backgroundHeight)
        _vh.view(R.id.lib_header_wrap_layout)?.setHeight(backgroundHeight - headerIndent)

        //内容背景使用和fragment一样的
        _vh.view(R.id.lib_content_wrap_layout)?.background =
            fragmentConfig.fragmentBackgroundDrawable?.copyDrawable()

        //tab
        _vh.tab(R.id.lib_tab_layout)?.apply {
            configTabLayoutConfig {
                onGetTextStyleView = { itemView, _ ->
                    itemView.find(R.id.lib_tab_text_view)
                }
                onGetIcoStyleView = { itemView, _ ->
                    itemView.find(R.id.lib_tab_image_view)
                }
            }

            if (getPageCount() > equWidthTabItemCount) {
                //滚动样式
                tabIndicator.indicatorStyle = DslTabIndicator.INDICATOR_STYLE_BOTTOM
                itemIsEquWidth = false
            } else {
                //等宽样式
                tabLayoutConfig?.tabEnableGradientScale = false
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return super.getPageTitle(position)
    }
}