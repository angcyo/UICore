package com.angcyo.core.component.step

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.angcyo.viewmodel.vmData

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/19
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 *
 * 计步器启动的步数收集
 */

class StepModel : ViewModel() {
    val stepCountData: MutableLiveData<Long> = vmData(0)
}