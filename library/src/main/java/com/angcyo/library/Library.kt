package com.angcyo.library

import android.app.Application
import com.angcyo.library.ex.isDebug
import com.orhanobut.hawk.Hawk

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object Library {

    lateinit var application: Application

    fun init(context: Application, debug: Boolean = isDebug()) {
        application = context

        /*sp持久化库*/
        Hawk.init(context)
            .build()
    }
}

fun app(): Application = Library.application