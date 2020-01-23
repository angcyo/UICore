package com.angcyo.core

import android.app.Application
import android.content.Context
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.interceptor.LogFileInterceptor
import com.angcyo.http.DslHttp
import com.angcyo.library.L
import com.angcyo.library.Library
import com.angcyo.library.ex.isDebug
import com.angcyo.library.getAppString
import me.weishu.reflection.Reflection

/**
 * Created by angcyo on 2019/12/23.
 * Copyright (c) 2016, angcyo@126.com All Rights Reserved.
 * *                                                   #
 * #                                                   #
 * #                       _oo0oo_                     #
 * #                      o8888888o                    #
 * #                      88" . "88                    #
 * #                      (| -_- |)                    #
 * #                      0\  =  /0                    #
 * #                    ___/`---'\___                  #
 * #                  .' \\|     |# '.                 #
 * #                 / \\|||  :  |||# \                #
 * #                / _||||| -:- |||||- \              #
 * #               |   | \\\  -  #/ |   |              #
 * #               | \_|  ''\---/''  |_/ |             #
 * #               \  .-\__  '-'  ___/-. /             #
 * #             ___'. .'  /--.--\  `. .'___           #
 * #          ."" '<  `.___\_<|>_/___.' >' "".         #
 * #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 * #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 * #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 * #                       `=---='                     #
 * #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 * #                                                   #
 * #               佛祖保佑         永无BUG              #
 * #                                                   #
 */

open class CoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //必须第一个初始化
        Library.init(this, isDebug())
        DslFileHelper.init(this)
        DslCrashHandler.init(this)
        L.init(getAppString("app_name") ?: "Log", isDebug())

        DslHttp.config {
            onConfigOkHttpClient.add {
                it.addInterceptor(LogFileInterceptor())
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }
}