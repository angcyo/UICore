package com.angcyo.library.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/28
 */

object ShortcutUtil {

    private var AUTHORITY: String? = null

    /**获取快捷方式的数量*/
    fun getShortcutCount(context: Context?, title: String?): Int {
        var shortcutCount = 0
        if (null == context || TextUtils.isEmpty(title)) return shortcutCount
        if (TextUtils.isEmpty(AUTHORITY)) AUTHORITY = getAuthorityFromPermission(context)
        val cr = context.contentResolver
        if (!TextUtils.isEmpty(AUTHORITY)) {
            try {
                val uri = Uri.parse(AUTHORITY)
                val c =
                    cr.query(uri, arrayOf("title", "iconResource"), "title=?", arrayOf(title), null)
                // XXX表示应用名称。
                shortcutCount = c?.count ?: 0
                L.i("找到:$title ${shortcutCount}个.")
                if (null != c && !c.isClosed) c.close()
            } catch (e: Exception) {
                L.w("isShortcutExist ", e.message)
            }
        }
        return shortcutCount
    }

    private fun getCurrentLauncherPackageName(context: Context): String {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val res = context.packageManager
            .resolveActivity(intent, 0)
        if (res?.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
            return ""
        }
        return if (res.activityInfo.packageName == "android") {
            ""
        } else {
            res.activityInfo.packageName
        }
    }

    private fun getAuthorityFromPermissionDefault(context: Context): String {
        return getThirdAuthorityFromPermission(
            context,
            "com.android.launcher.permission.READ_SETTINGS"
        )
    }

    private fun getThirdAuthorityFromPermission(
        context: Context,
        permission: String
    ): String {
        if (TextUtils.isEmpty(permission)) {
            return ""
        }
        try {
            val packs =
                context.packageManager.getInstalledPackages(PackageManager.GET_PROVIDERS)
                    ?: return ""
            for (pack in packs) {
                val providers = pack.providers
                if (providers != null) {
                    for (provider in providers) {
                        if (permission == provider.readPermission ||
                            permission == provider.writePermission
                        ) {
                            val authority = provider.authority
                            if (!authority.isBlank() &&
                                (authority.contains(".launcher.settings") ||
                                        authority.contains(".twlauncher.settings") ||
                                        authority.contains(".launcher2.settings"))
                            ) return authority
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getAuthorityFromPermission(context: Context): String {
        // 获取默认
        var authority = getAuthorityFromPermissionDefault(context)
        // 获取特殊第三方
        if (authority.trim { it <= ' ' } == "") {
            var packageName = getCurrentLauncherPackageName(context)
            packageName += ".permission.READ_SETTINGS"
            authority = getThirdAuthorityFromPermission(context, packageName)
        }
        // 还是获取不到，直接写死
        if (TextUtils.isEmpty(authority)) {
            val sdkInt = Build.VERSION.SDK_INT
            authority = if (sdkInt < 8) {
                // Android 2.1.x(API 7)以及以下的
                "com.android.launcher.settings"
            } else if (sdkInt < 19) { // Android 4.4以下
                "com.android.launcher2.settings"
            } else { // 4.4以及以上
                "com.android.launcher3.settings"
            }
        }
        authority = "content://$authority/favorites?notify=true"
        return authority
    }
}