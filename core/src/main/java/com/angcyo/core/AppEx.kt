package com.angcyo.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.angcyo.library.ex.mimeType
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

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
 * */
fun installApk(context: Context, file: File?) {
    if (file == null || !file.canRead()) return
    val intent = Intent(Intent.ACTION_VIEW);
    val type: String? = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        "application/vnd.android.package-archive"
    } else {
        file.absolutePath.mimeType()
    }
    val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context, context.packageName, file)
    } else {
        Uri.fromFile(file)
    }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.setDataAndType(uri, type)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}