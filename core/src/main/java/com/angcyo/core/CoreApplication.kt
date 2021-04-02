package com.angcyo.core

import android.content.Context
import androidx.collection.ArrayMap
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.HttpConfigDialog
import com.angcyo.core.component.interceptor.LogFileInterceptor
import com.angcyo.http.DslHttp
import com.angcyo.http.addInterceptorEx
import com.angcyo.http.rx.Rx
import com.angcyo.library.L
import com.angcyo.library.LibApplication
import com.angcyo.library.ex.getAppSignatureMD5
import com.angcyo.library.ex.getAppSignatureSHA1
import com.angcyo.library.getAppString
import me.jessyan.progressmanager.ProgressManager
import me.weishu.reflection.Reflection

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/06
 */

open class CoreApplication : LibApplication(), ViewModelStoreOwner {

    override fun onCreate() {
        super.onCreate()

        Rx.init()

        DslHttp.config {
            onGetBaseUrl = {
                //优先使用主动配置的, 其次使用默认配置的
                val getBaseUrl = HttpConfigDialog.customBaseUrl ?: getHostBaseUrl()
                if (getBaseUrl.endsWith("/")) {
                    getBaseUrl
                } else {
                    "${getBaseUrl}/"
                }
            }

            val logFileInterceptor = LogFileInterceptor()
            configHttpBuilder {
                //进度拦截器
                it.addInterceptorEx(ProgressManager.getInstance().interceptor, 0)
                it.addInterceptorEx(logFileInterceptor)
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

    /**服务器地址
     * resValue "string", "base_api", '"https://rj.appraise.wayto.com.cn/appraiseApi"'
     * */
    open fun getHostBaseUrl() = getAppString("base_api") ?: "http://api.angcyo.com"

    /**
     * 服务器配置
     * resValue "string", "custom_urls", '"测试服务器 https://rj.appraise.wayto.com.cn/appraiseApi;正式服务器 https://ruijie.appraise.wayto.com.cn:8043/appraiseApi"'
     * */
    open fun getHostUrls() = getAppString("custom_urls")

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