package com.angcyo.core.component.model

import androidx.lifecycle.ViewModel
import com.angcyo.viewmodel.vmDataOnce
import java.io.File

/**
 * App内数据共享模型
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DataShareModel : ViewModel() {

    /**共享数据通知, [Any]类型*/
    val shareOnceData = vmDataOnce<Any?>()

    /**共享文本通知*/
    val shareTextOnceData = vmDataOnce<CharSequence?>()

    /**共享服务地址通知, http://地址*/
    val shareServerAddressOnceData = vmDataOnce<String?>()

    /**共享文件通知*/
    val shareFileOnceData = vmDataOnce<File?>()

    /**共享状态通知*/
    val shareStateOnceData = vmDataOnce<Int?>()
}