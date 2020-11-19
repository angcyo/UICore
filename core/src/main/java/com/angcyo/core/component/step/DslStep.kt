package com.angcyo.core.component.step

import android.content.Context
import android.content.Intent
import com.angcyo.library.app

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/19
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object DslStep {

    /**启动计步器, 数据可以通过
     * [com.angcyo.core.component.step.StepModel.getStepCountData] 监听到*/
    fun start(context: Context = app()) {
        val intent = Intent(context, StepService::class.java)
        context.startService(intent)
    }

    /**停止计步器*/
    fun stop(context: Context = app()) {
        val intent = Intent(context, StepService::class.java)
        context.stopService(intent)
    }
}