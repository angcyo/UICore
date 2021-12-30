package com.angcyo.core.component

import android.util.SparseArray
import com.angcyo.core.lifecycle.LifecycleViewModel
import com.angcyo.library.component.OnBackgroundObserver
import com.angcyo.library.component.RBackground
import com.angcyo.viewmodel.vmDataNull

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class BackgroundModel : LifecycleViewModel() {

    /**应用是否在后台运行*/
    val backgroundData = vmDataNull(false)

    init {
        RBackground.registerObserver(object : OnBackgroundObserver {
            override fun onActivityChanged(stack: SparseArray<String>, background: Boolean) {
                backgroundData.postValue(background)
            }
        }, true)
    }
}