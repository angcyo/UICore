package com.angcyo.core

import android.content.Context
import android.os.Build
import androidx.collection.ArrayMap
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.angcyo.activity.ActivityDebugInfo
import com.angcyo.core.component.ComplianceCheck
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.HttpConfigDialog
import com.angcyo.core.component.StateModel
import com.angcyo.core.component.file.writeErrorLog
import com.angcyo.core.component.interceptor.LogFileInterceptor
import com.angcyo.core.component.model.LanguageModel
import com.angcyo.core.component.model.NightModel
import com.angcyo.core.dslitem.DslLastDeviceInfoItem
import com.angcyo.coroutine.CoroutineErrorHandler
import com.angcyo.dialog.hideLoading
import com.angcyo.http.DslHttp
import com.angcyo.http.addInterceptorEx
import com.angcyo.http.interceptor.LogInterceptor
import com.angcyo.http.removeInterceptor
import com.angcyo.http.rx.Rx
import com.angcyo.http.rx.doBack
import com.angcyo.library.L
import com.angcyo.library.LibApplication
import com.angcyo.library.annotation.CallComplianceAfter
import com.angcyo.library.ex._string
import com.angcyo.library.ex.connectUrl
import com.angcyo.library.ex.getAppSignatureMD5
import com.angcyo.library.ex.getAppSignatureSHA1
import com.angcyo.library.ex.isAppDebug
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.isShowDebug
import com.angcyo.library.ex.toStr
import com.angcyo.library.ex.wrapLog
import com.angcyo.library.getAppString
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.LogFile
import com.angcyo.library.utils.toLogFilePath
import com.angcyo.library.utils.writeTo
import com.angcyo.widget.edit.BaseEditDelegate
import me.jessyan.progressmanager.ProgressManager
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/06
 */

open class CoreApplication : LibApplication(), ViewModelStoreOwner {

    companion object {

        /**写入文件log的级别*/
        var LOG_FILE_LEVEl = L.INFO

        /**默认L.log的文件路径*/
        var DEFAULT_FILE_PRINT_PATH: String? = ""

        /**L.log写入文件, 并且输出到控制台*/
        val DEFAULT_FILE_PRINT: (tag: String, level: Int, msg: String) -> Unit =
            { tag, level, msg ->
                if (isAppDebug()) {
                    //控制台输出
                    L.DEFAULT_LOG_PRINT.invoke(tag, level, msg)
                }
                //文件输出
                if (level >= LOG_FILE_LEVEl) {
                    DEFAULT_FILE_PRINT_PATH?.let { path ->
                        when (level) {
                            L.VERBOSE -> "[VERBOSE]${msg}"
                            L.DEBUG -> "[DEBUG]${msg}"
                            L.INFO -> "[INFO]${msg}"
                            L.WARN -> "[WARN]${msg}"
                            L.ERROR -> "[ERROR]${msg}"
                            else -> "[UNKNOWN]${msg}"
                        }.wrapLog().apply {
                            doBack(true) {
                                writeTo(path)
                            }
                        }
                    }
                }
            }

        /**打开url*/
        var onOpenUrlAction: ((url: String?) -> Unit)? = null
    }

    override fun onCreate() {
        super.onCreate()

        //初始化
        initCoreApplication()
    }

    /**[onCreate]*/
    override fun initLibApplication() {
        //日志输出到文件
        DEFAULT_FILE_PRINT_PATH = LogFile.l.toLogFilePath()
        L.logPrint = this::writeLogToFile
        L.init(getAppString("app_name") ?: "Log", isDebug()) //debug打开日志存储

        super.initLibApplication()
    }

    /**[onCreate]
     * [initLibApplication]*/
    open fun initCoreApplication() {
        if (isShowDebug()) {
            ActivityDebugInfo.install(this)
        }

        //暗色模式
        vmApp<NightModel>().onCreate(this)
        //语言
        vmApp<LanguageModel>().onCreate(this)

        //rx error
        Rx.init { exception ->
            "Rx内异常:${exception.toStr()}".writeErrorLog(L.NONE)
            onThreadException(exception)
        }

        //coroutine error
        CoroutineErrorHandler.globalCoroutineExceptionHandler = { _, exception ->
            "协程内异常:${exception.toStr()}".writeErrorLog(L.NONE)
            onThreadException(exception)
        }

        DslHttp.config {
            onGetBaseUrl = {
                //优先使用主动配置的, 其次使用默认配置的
                val baseUrl = HttpConfigDialog.customBaseUrl ?: getHostBaseUrl()
                if (baseUrl.endsWith("/")) {
                    baseUrl
                } else {
                    "${baseUrl}/"
                }
            }

            val logFileInterceptor = LogFileInterceptor()
            logFileInterceptor.printLog = L.debug
            configHttpBuilder {
                it.removeInterceptor { it is LogInterceptor }
                //进度拦截器
                it.addInterceptorEx(ProgressManager.getInstance().interceptor, 0)
                it.addInterceptorEx(logFileInterceptor)
            }
        }

        //debug
        BaseEditDelegate.textChangedActionList.add(Debug::onDebugTextChanged)

        //Compliance 合规后的初始化
        vmApp<StateModel>().waitState(
            ComplianceCheck.TYPE_COMPLIANCE_STATE,
            false
        ) { data, throwable ->
            if (throwable == null && data.value == true) {
                //合规后
                onComplianceAfter()
            }
        }
    }

    /**合规后的初始化
     * ```
     * ComplianceCheck.check {
     *   //同意合规, 在此弹出合规对话框
     *   ComplianceCheck.agree()//用户同意之后, 调用此方法
     * }
     * ```
     * */
    @CallComplianceAfter
    open fun onComplianceAfter() {
        //device, 需要合规
        DslLastDeviceInfoItem.saveDeviceInfo(this, true) {
            L.i("MD5->", getAppSignatureMD5()?.apply {
                append("MD5->")
                appendLine(this)
            })
            L.i("SHA1->", getAppSignatureSHA1()?.apply {
                append("SHA1->")
                appendLine(this)
            })
        }
        DslCrashHandler.init(this)
        vmApp<StateModel>().updateState(ComplianceCheck.TYPE_COMPLIANCE_INIT_AFTER, true)
    }

    override fun onCreateMain() {
        super.onCreateMain()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguageModel.attachBaseContext(base))
        //Reflection.unseal(base)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P /*&& Build.VERSION.SDK_INT < Build.VERSION_CODES.S*/) {
            //HiddenApiBypass.addHiddenApiExemptions("L")
            HiddenApiBypass.addHiddenApiExemptions("")
        }
    }

    /**写入log到文件*/
    open fun writeLogToFile(tag: String, level: Int, msg: String) {
        DEFAULT_FILE_PRINT(tag, level, msg)
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

    /**将相对路径或者绝对路径, 转换成绝对路径*/
    open fun toUrl(path: String?): String {
        if (path.isNullOrEmpty()) {
            return ""
        }
        return HttpConfigDialog.appBaseUrl.connectUrl(path)
    }

    /**线程/协程内发生了异常*/
    open fun onThreadException(exception: Throwable) {
        val tip = if (isDebug()) {
            exception.message
        } else {
            _string(R.string.core_thread_error_tip)
        }
        toastQQ(tip)
        hideLoading(tip)
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

    override val viewModelStore get() = modelStore

    //</editor-fold desc="ViewModelStore">

}