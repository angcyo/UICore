package com.angcyo.library.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.ex.baseConfig
import com.angcyo.library.ex.fileUri
import com.angcyo.library.ex.getAppOpenIntentByPackageName
import com.angcyo.library.ex.mimeType
import com.angcyo.library.ex.uriConfig
import com.angcyo.library.model.AppBean
import java.io.File


/**
 * https://developer.android.google.cn/training/sharing
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/13
 */
class DslIntent {

    companion object {
        const val QUERY_TYPE_ACTIVITY = 1
        const val QUERY_TYPE_SERVICE = 2
        const val QUERY_TYPE_PROVIDER = 3

        /** 跳至拨号界面 [phoneNumber] 电话号码 */
        fun callTo(context: Context, phoneNumber: String?) {
            if (phoneNumber.isNullOrEmpty()) {
                return
            }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            intent.baseConfig(context)
            context.startActivity(intent)
        }

        /** 拨打电话 需添加权限 `<uses-permission android:name="android.permission.CALL_PHONE"/>`
         * [phoneNumber] 电话号码 */
        @SuppressLint("MissingPermission")
        fun call(context: Context, phoneNumber: String?) {
            if (phoneNumber.isNullOrEmpty()) {
                return
            }
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            intent.baseConfig(context)
            context.startActivity(intent)
        }

        /**发送短信*/
        fun sendSMS(context: Activity, message: String?, phoneNumber: String) {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
            intent.putExtra("sms_body", message)
            context.startActivity(intent)
        }

        /**打开文件*/
        fun openFile(context: Context, uri: Uri?) {
            if (uri == null) {
                return
            }
            val intent = Intent()
            intent.uriConfig(context, uri)
            //设置intent的Action属性
            intent.action = Intent.ACTION_VIEW
            //获取文件file的MIME类型
            val type: String = uri.toString().mimeType() ?: "*/*"
            //设置intent的data和Type属性。
            intent.setDataAndType(uri, type)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //跳转
            try {
                //这里最好try一下，有可能会报错。
                // 比如说你的MIME类型是打开邮箱，但是你手机里面没装邮箱客户端，就会报错。
                context.startActivity(intent)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        /**打开链接*/
        fun openUrl(context: Context, url: String?) {
            if (url == null) {
                return
            }
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.baseConfig(context)
            intent.data = Uri.parse(url)
            //跳转
            try {
                context.startActivity(intent)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        /**打开通知设置界面
         * https://zhuanlan.zhihu.com/p/407705157*/
        fun toNotifySetting(context: Context = app()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val intent = Intent()
                    intent.action =
                        Settings.ACTION_APP_NOTIFICATION_SETTINGS//"android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    //intent.data = Uri.fromParts("package", context.packageName, null)
                    //intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
                    //intent.putExtra("android.provider.extra.CHANNEL_ID", context.applicationInfo.uid)
                    //intent.putExtra("app_package", context.packageName)
                    //intent.putExtra("app_uid", context.applicationInfo.uid)
                    intent.baseConfig(context)
                    context.startActivity(intent)
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                    toAppDetail(context)
                } else {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    intent.baseConfig(context)
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                toAppDetail(context)
            }
        }

        /**打开APP详情界面*/
        fun toAppDetail(context: Context = app(), packageName: String = app().packageName) {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= 9) {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", packageName, null)
            } else if (Build.VERSION.SDK_INT <= 8) {
                intent.action = Intent.ACTION_VIEW
                intent.setClassName(
                    "com.android.settings",
                    "com.android.settings.InstalledAppDetails"
                )
                intent.putExtra("com.android.settings.ApplicationPkgName", packageName)
            }
            intent.baseConfig(context)
            context.startActivity(intent)
        }

        /**
         * 获取安装App(支持6.0)的意图
         * 安卓8.0 需要请求安装权限 Manifest.permission.REQUEST_INSTALL_PACKAGES
         * @param file 文件
         * @return intent
         */
        fun getInstallAppIntent(file: File?): Intent? {
            if (file == null) return null
            val intent = Intent(Intent.ACTION_VIEW)
            val type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                "application/vnd.android.package-archive"
            } else {
                file.name.mimeType()
            }
            val uri = fileUri(app(), file)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, type)
            return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        /**
         * 获取卸载App的意图
         *
         * @param packageName 包名
         * @return intent
         */
        fun getUninstallAppIntent(packageName: String): Intent {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        /**打开应用的Intent*/
        fun openAppIntent(packageName: String): Intent? {
            return app().getAppOpenIntentByPackageName(packageName)
        }

        /**打开系统设置界面
         * https://www.jianshu.com/p/7145c2544ef4*/
        fun openSettingIntent(content: Context = app()) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            content.startActivity(intent)
        }

        /**打开网络设置界面
         * https://blog.csdn.net/qq_15527669/article/details/80264884*/
        fun openWirelessIntent(content: Context = app()) {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            content.startActivity(intent)
        }
    }

    //<editor-fold desc="分享相关配置">

    var createChooser: Boolean = true

    /**[Intent.ACTION_SENDTO] [Intent.ACTION_SEND_MULTIPLE]*/
    var shareAction: String = Intent.ACTION_SEND

    /**Android 10 分享预览窗口的标题*/
    var shareTitle: CharSequence? = null

    /**分享的文本信息, 也是邮件正文*/
    var shareText: CharSequence? = null

    /**分享的文件(流), 注意要修改mimeType*/
    var shareUri: Uri? = null

    /**多文件分享, 注意要修改mimeType, 多个不同类型的文件使用[*\*] */
    var shareUris: List<Uri>? = null
        set(value) {
            field = value
            if (value != null) {
                shareAction = Intent.ACTION_SEND_MULTIPLE
            }
        }

    /**邮件有附件, 可以用[application/octet-stream], [*\*]*/
    var shareTextMimeType: String = "text/*"

    /**使用邮件分享, angcyo@126.com*/
    var shareEmail: String? = null
        set(value) {
            field = value
            if (value != null) {
                shareAction = Intent.ACTION_SENDTO
            }
        }

    /**多个收件人*/
    var shareEmails: List<String>? = null
        set(value) {
            field = value
            if (value != null) {
                shareAction = Intent.ACTION_SENDTO
            }
        }

    /**邮件主题*/
    var shareEmailSubject: String? = null

    /**
     * 开始分享
     * https://developer.android.google.cn/training/sharing/send.html
     */
    fun doShare(context: Context?) {
        if (context == null) {
            L.w("context is null!")
            return
        }
        Intent().apply {
            baseConfig(context)

            action = shareAction

            if (shareTitle != null) {
                putExtra(Intent.EXTRA_TITLE, shareTitle)
            }

            if (shareText != null) {
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            //收件人
            if (shareEmail != null) {
                //data = Uri.parse("mailto:$shareEmail")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(shareEmail))
            }
            if (shareEmails != null) {
                putExtra(Intent.EXTRA_EMAIL, shareEmails!!.toTypedArray())
            }

            //邮件主题
            if (shareEmailSubject != null) {
                putExtra(Intent.EXTRA_SUBJECT, shareEmailSubject)
            }

            //文件
            when {
                shareUris != null -> {
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(shareUris!!))
                    shareUris!!.forEach {
                        uriConfig(context, it)
                    }
//                    shareUris!!.firstOrNull()?.run {
//                        clipData = this
//                    }
                }

                shareUri != null -> {
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    uriConfig(context, shareUri!!)
                }

                else -> {
                    baseConfig(context)
                }
            }

            type = shareTextMimeType

            if (createChooser) {
                val shareIntent = Intent.createChooser(this, shareTitle)
                context.startActivity(shareIntent)
            } else {
                context.startActivity(this)
            }
        }
    }

    //</editor-fold desc="分享相关配置">

    //<editor-fold desc="查询Intent相关配置">

    var queryAction: String? = Intent.ACTION_VIEW //Intent.ACTION_MAIN
    var queryData: Uri? = null
    var queryCategory: List<String>? = null //android.content.Intent.CATEGORY_LAUNCHER
    var queryFlag: Int = 0
    var queryPackageName: String? = null

    //查询指定类名的信息
    var queryClassName: String? = null

    var queryType: Int = QUERY_TYPE_ACTIVITY

    fun doQuery(context: Context?): List<ResolveInfo> {
        if (context == null) {
            L.w("context is null!")
            return emptyList()
        }

        val queryIntent = Intent()
        queryAction?.run { queryIntent.setAction(this) }
        queryCategory?.forEach { queryIntent.addCategory(it) }
        queryData?.run { queryIntent.setData(this) }
        queryPackageName?.run { queryIntent.setPackage(this) }
        queryClassName?.run {
            queryIntent.setClassName(
                queryPackageName ?: context.packageName,
                this
            )
        }

        val packageManager = context.packageManager

        /*
        * https://stackoverflow.com/questions/52734920/what-is-the-difference-between-queryintentactivities-and-resolveactivity-whi
        * queryIntentActivities() returns a list of all activities that can handle the Intent.
        * resolveActivity() returns the "best" Activity that can handle the Intent
        * */
        val resultList: List<ResolveInfo> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && queryType == QUERY_TYPE_PROVIDER) {
                packageManager.queryIntentContentProviders(queryIntent, queryFlag)
            } else if (queryType == QUERY_TYPE_SERVICE) {
                packageManager.queryIntentServices(queryIntent, queryFlag)
            } else {
                packageManager.queryIntentActivities(queryIntent, queryFlag)
            }
        return resultList
    }

    //</editor-fold desc="查询Intent相关配置">

    fun doIt() {

    }
}

/**查询对应的信息*/
fun Intent.queryActivities(context: Context = app(), queryFlag: Int = 0): List<ResolveInfo> {
    val packageManager = context.packageManager
    return packageManager.queryIntentActivities(this, queryFlag)
}

fun dslIntent(context: Context? = app(), action: DslIntent.() -> Unit) {
    val dslIntent = DslIntent()
    dslIntent.action()
    dslIntent.doIt()
}

fun dslIntentShare(context: Context? = app(), action: DslIntent.() -> Unit) {
    val dslIntent = DslIntent()
    dslIntent.action()
    dslIntent.doShare(context)
}

fun dslIntentQuery(
    context: Context? = app(),
    action: DslIntent.() -> Unit = {}
): List<ResolveInfo> {
    val dslIntent = DslIntent()
    dslIntent.action()
    return dslIntent.doQuery(context)
}

fun Context.appBean(packageName: String = this.packageName): AppBean? {
    return packageName.appBean(this)
}

fun String.appBean(context: Context = app()): AppBean? {
    return try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(this, 0)

        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }

        AppBean(
            packageInfo.packageName,
            packageInfo?.versionName,
            code,
            packageInfo.applicationInfo?.loadIcon(packageManager),
            packageInfo.applicationInfo?.loadLabel(context.packageManager),
            packageInfo
        )
    } catch (e: Exception) {
        null
    }
}

/**是否安装了app*/
fun String.isInstallApp() = appBean() != null

/**跳转应用详情页面*/
fun String.toAppDetail(context: Context = app()) = DslIntent.toAppDetail(context, this)

/** 跳至拨号界面 [phoneNumber] 电话号码 */
fun Context.callTo(phoneNumber: String) {
    DslIntent.callTo(this, phoneNumber)
}

/** 拨打电话 需添加权限 `<uses-permission android:name="android.permission.CALL_PHONE"/>`
 * [phoneNumber] 电话号码 */
fun Context.call(phoneNumber: String) {
    DslIntent.call(this, phoneNumber)
}