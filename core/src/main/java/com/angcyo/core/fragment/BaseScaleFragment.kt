package com.angcyo.core.fragment

import android.os.Bundle
import com.angcyo.core.R
import com.angcyo.library.ex.replace

/**
 * 简单的背景放大界面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/18
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseScaleFragment : BaseDslFragment() {

    /**自定义背景布局,
     * 也可以使用默认的[lib_background_scale_image]图片显示背景
     * */
    var backgroundScaleLayoutId: Int = -1

    init {
        fragmentLayoutId = R.layout.lib_sacle_fragment
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        initScaleLayout()
    }

    fun initScaleLayout() {
        //背景层
        _vh.group(R.id.lib_background_wrap_layout)?.replace(backgroundScaleLayoutId)
    }

}