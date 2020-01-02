package com.angcyo.core

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build

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