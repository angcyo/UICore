package com.angcyo.core

import android.content.Context
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.interceptor.LogFileInterceptor
import com.angcyo.http.DslHttp
import com.angcyo.library.LibApplication
import me.weishu.reflection.Reflection

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/06
 */

open class CoreApplication : LibApplication() {
    override fun onCreate() {
        super.onCreate()
        DslFileHelper.init(this)
        DslCrashHandler.init(this)

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