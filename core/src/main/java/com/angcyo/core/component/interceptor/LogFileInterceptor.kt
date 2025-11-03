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

    companion object {
        /**[printRequestLog]*/
        val printRequestLogActionList = mutableListOf<PrintLogAction>()

        /**[printResponseLog]*/
        val printResponseLogActionList = mutableListOf<PrintLogAction>()
    }

    /**是否需要控制台输出日志*/
    var printLog = false

    init {
        enable = true
    }

    override fun printRequestLog(builder: StringBuilder) {
        if (printLog) {
            super.printRequestLog(builder)
        }
        val log = builder.toString()
        DslFileHelper.http(data = log)
        printRequestLogActionList.forEach {
            it.invoke(log)
        }
    }

    override fun printResponseLog(builder: StringBuilder) {
        if (printLog) {
            super.printResponseLog(builder)
        }
        val log = builder.toString()
        DslFileHelper.http(data = log)
        printResponseLogActionList.forEach {
            it.invoke(log)
        }
    }
}

typealias PrintLogAction = (log: String) -> Unit