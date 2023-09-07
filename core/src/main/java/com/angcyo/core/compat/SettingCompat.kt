package com.angcyo.core.compat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.angcyo.library.ex.baseConfig
import ezy.assist.compat.SettingsCompat

/**
 * 设置兼容
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/26
 */
object SettingCompat {

    /**是否有浮窗权限*/
    fun haveDrawOverlays(context: Context): Boolean {
        return try {
            SettingsCompat.canDrawOverlays(context)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**打开悬浮窗设置页面
     * [context] 需要是Activity*/
    fun openOverlaysActivity(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.baseConfig(context)
            context.startActivity(intent)
        } else {
            SettingsCompat.manageDrawOverlays(context)
        }
    }


}