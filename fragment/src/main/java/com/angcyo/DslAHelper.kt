package com.angcyo

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslAHelper(val context: Context) {

    /**需要启动的[Intent]*/
    val startIntent = mutableListOf<Intent>()

    //<editor-fold desc="start操作">

    fun start(intent: Intent, action: Intent.() -> Unit = {}) {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.action()
        startIntent.add(intent)
    }

    fun start(aClass: Class<*>, action: Intent.() -> Unit = {}) {
        val intent = Intent(context, aClass)
        start(intent, action)
    }

    //</editor-fold desc="start操作">

    fun doIt() {
        startIntent.forEach {
            try {
                context.startActivity(it)
            } catch (e: Exception) {
                L.e("启动Activity失败:$e")
            }
        }
    }

}