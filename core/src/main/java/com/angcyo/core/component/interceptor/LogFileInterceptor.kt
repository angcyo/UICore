package com.angcyo.core.component.interceptor

import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.http.interceptor.LogInterceptor

/**
 * 网络请求日志文件保存拦截器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class LogFileInterceptor : LogInterceptor() {

    /**是否需要控制台输出日志*/
    var printLog = false

    init {
        enable = true
    }

    override fun printRequestLog(builder: StringBuilder) {
        if (printLog) {
            super.printRequestLog(builder)
        }
        DslFileHelper.http(data = builder.toString())
    }

    override fun printResponseLog(builder: StringBuilder) {
        if (printLog) {
            super.printResponseLog(builder)
        }
        DslFileHelper.http(data = builder.toString())
    }
}