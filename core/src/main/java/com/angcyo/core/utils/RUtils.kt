package com.angcyo.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.angcyo.library.toast

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/02
 */

object RUtils {
    /** qq咨询 */
    fun chatQQ(context: Context, qq: String = "664738095"): Boolean {
        try {
            if (context.checkApkExist("com.tencent.mobileqq")) {
                val url = "mqqwpa://im/chat?chat_type=wpa&uin=$qq"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } else {
                toast("您没有安装腾讯QQ")
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            toast("您没有安装腾讯QQ")
        }
        return false
    }

    /** 快速加群 */
    fun joinQQGroup(context: Context, key: String = "TO1ybOZnKQHSLcUlwsVfOt6KQMGLmuAW"): Boolean {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (e: java.lang.Exception) { // 未安装手Q或安装的版本不支持
            toast("您没有安装腾讯QQ")
            false
        }
    }
}

/** 检查APK是否安装 */
fun Context.checkApkExist(packageName: String?): Boolean {
    return if (packageName.isNullOrBlank()) {
        false
    } else try {
        val packageManager = packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.MATCH_UNINSTALLED_PACKAGES
            )
        } else {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_UNINSTALLED_PACKAGES
            )
        }
        true
    } catch (e: Exception) {
        false
    }
}