package com.angcyo.library.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.angcyo.library.app
import com.angcyo.library.ex.baseConfig
import com.angcyo.library.ex.mimeType
import com.angcyo.library.ex.uriConfig


/**
 * https://developer.android.google.cn/training/sharing
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/13
 */
class DslIntent {

    companion object {
        /** 跳至拨号界面 @param phoneNumber 电话号码 */
        fun callTo(context: Context, phoneNumber: String) {
            val intent =
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            intent.baseConfig(context)
            context.startActivity(intent)
        }

        /** 拨打电话 需添加权限 `<uses-permission android:name="android.permission.CALL_PHONE"/>`
         * @param phoneNumber 电话号码 */
        @SuppressLint("MissingPermission")
        fun call(context: Context, phoneNumber: String) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            intent.baseConfig(context)
            context.startActivity(intent)
        }

        /**发送短信*/
        fun sendSMS(context: Activity, message: String?, phoneNumber: String) {
            val intent =
                Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
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

        /**打开通知设置界面*/
        fun toNotifySetting(context: Context = app()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = Intent()
                intent.action =
                    "android.settings.APP_NOTIFICATION_SETTINGS"//Settings.ACTION_APP_NOTIFICATION_SETTINGS
                //intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                intent.data = Uri.fromParts("package", context.packageName, null)
                intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
                intent.putExtra("android.provider.extra.CHANNEL_ID", context.applicationInfo.uid)
                intent.putExtra("app_package", context.packageName)
                intent.putExtra("app_uid", context.applicationInfo.uid)
                intent.baseConfig(context)
                context.startActivity(intent)
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:" + context.packageName)
                intent.baseConfig(context)
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.baseConfig(context)
                context.startActivity(intent)
            }
        }

        /**打开APP详情界面*/
        fun toAppDetail(context: Context = app()) {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= 9) {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", context.packageName, null)
            } else if (Build.VERSION.SDK_INT <= 8) {
                intent.action = Intent.ACTION_VIEW
                intent.setClassName(
                    "com.android.settings",
                    "com.android.settings.InstalledAppDetails"
                )
                intent.putExtra("com.android.settings.ApplicationPkgName", context.packageName)
            }
            intent.baseConfig(context)
            context.startActivity(intent)
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
    fun doShare(context: Context) {
        Intent().apply {
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

    fun doIt() {

    }
}

fun dslIntent(context: Context, action: DslIntent.() -> Unit) {
    val dslIntent = DslIntent()
    dslIntent.action()
    dslIntent.doIt()
}

fun dslIntentShare(context: Context, action: DslIntent.() -> Unit) {
    val dslIntent = DslIntent()
    dslIntent.action()
    dslIntent.doShare(context)
}