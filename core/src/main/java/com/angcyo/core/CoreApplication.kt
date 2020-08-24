package com.angcyo.core

import android.content.Context
import androidx.collection.ArrayMap
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.interceptor.LogFileInterceptor
import com.angcyo.http.DslHttp
import com.angcyo.library.L
import com.angcyo.library.LibApplication
import com.angcyo.library.ex.getAppSignatureMD5
import com.angcyo.library.ex.getAppSignatureSHA1
import me.weishu.reflection.Reflection

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/06
 */

open class CoreApplication : LibApplication(), ViewModelStoreOwner {

    override fun onCreate() {
        super.onCreate()

        DslHttp.config {
            val logFileInterceptor = LogFileInterceptor()
            onConfigOkHttpClient.add {
                if (!it.interceptors().contains(logFileInterceptor)) {
                    it.addInterceptor(logFileInterceptor)
                }
            }
        }
    }

    override fun onCreateMain() {
        super.onCreateMain()
        DslCrashHandler.init(this)

        L.d("MD5->", getAppSignatureMD5())
        L.d("SHA1->", getAppSignatureSHA1())
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }

    //<editor-fold desc="hold">

    /**[Application]的单例模式*/

    val objHold = ArrayMap<Class<*>, Any>()

    /**存储对象*/
    fun hold(obj: Any) {
        objHold[obj::class.java] = obj
    }

    /**获取对象*/
    @Throws(NullPointerException::class)
    fun <Obj> holdGet(obj: Class<*>): Obj {
        return objHold[obj::class.java] as Obj
    }

    //</editor-fold desc="hold">

    //<editor-fold desc="ViewModelStore">

    /**[ViewModel]单例*/
    val modelStore: ViewModelStore = ViewModelStore()

    override fun getViewModelStore(): ViewModelStore = modelStore

    //</editor-fold desc="ViewModelStore">

}