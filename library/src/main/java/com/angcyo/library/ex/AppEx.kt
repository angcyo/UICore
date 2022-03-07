package com.angcyo.library.ex

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.angcyo.library.app
import com.angcyo.library.utils.Device
import java.io.File
import kotlin.system.exitProcess

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**获取应用md5签名*/
fun Context.getAppSignatureMD5() =
    getAppSignature(this, this.packageName)?.getOrNull(0)?.toByteArray()?.encrypt("MD5")
        ?.toHexString()?.beautifyHex()

/**获取应用sha1签名*/
fun Context.getAppSignatureSHA1() =
    getAppSignature(this, this.packageName)?.getOrNull(0)?.toByteArray()?.encrypt("SHA1")
        ?.toHexString()?.beautifyHex()

/**获取应用签名*/
fun getAppSignature(
    context: Context,
    packageName: String
): Array<Signature>? {
    return try {
        val pm = context.packageManager
        val pi = pm.getPackageInfo(
            packageName, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                PackageManager.GET_SIGNATURES
            }
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pi?.signingInfo?.apkContentsSigners
        } else {
            pi?.signatures
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}

/**
 * 安装apk
 * 安卓7.0 需要androidx.core.content.FileProvider
 * 安卓8.0 需要请求安装权限 Manifest.permission.REQUEST_INSTALL_PACKAGES
 *
 * https://github.com/AnyLifeZLB/DownloadInstaller
 * https://github.com/hgncxzy/InstallApk
 * */
fun installApk(context: Context, file: File?) {
    if (file == null || !file.canRead()) return
    //兼容8.0
    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val hasInstallPermission: Boolean =
            context.packageManager.canRequestPackageInstalls()
        if (!hasInstallPermission) {
            //注意这个是8.0新API
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }
    }*/
    val intent = Intent(Intent.ACTION_VIEW)
    val type: String? = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        "application/vnd.android.package-archive"
    } else {
        file.absolutePath.mimeType()
    }
    val uri: Uri = fileUri(context, file)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.setDataAndType(uri, type)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

/**
 * INTERNAL method that kills the current process.
 * It is used after restarting or killing the app.
 */
fun killCurrentProcess() {
    Process.killProcess(Process.myPid())
    exitProcess(909)
}

/**取消注册的广播*/
fun BroadcastReceiver.unregisterReceiver(context: Context = app()) {
    try {
        context.unregisterReceiver(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**返回不可用原因, 负数表示可用*/
fun getCanUsedState(): Int {
    var reason = when {
        isRoot() -> 1
        isVpnUsed() -> 2
        isProxyUsed() -> 3
        isRootUI() -> 4
        isXposedExistByThrow() -> 5
        isRunningInEmulator() -> 6
        Device.androidId.isBlank() -> 8
        else -> -1
    }

    if (isRelease()) {
        if (isAppDebug()) {
            reason = 7
        }
    }

    return reason
}