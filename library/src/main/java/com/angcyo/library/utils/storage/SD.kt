package com.angcyo.library.utils.storage

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Environment
import com.angcyo.library.app
import com.angcyo.library.ex.havePermission
import java.io.File

/**
 *
 * SD卡, 存储管理
 *
 * [com.angcyo.base.Activity.requestSdCardPermission]
 *
 * [https://developer.android.com/about/versions/11/privacy/storage?hl=zh-cn]
 * [https://developer.android.com/training/data-storage/shared/media?hl=zh-cn]
 * [https://developer.android.com/training/data-storage/app-specific?hl=zh-cn#query-free-space]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/10/23
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
object SD {

    /*fun test() {
        val path = sdDocumentFolderPath("/angcyo/fonts")
        val list = path.file().listFiles()
        L.i(list)
    }*/
}

/**
 * 下载目录, Android11+不需要权限就可以访问
 * /storage/emulated/0/Download
 * */
fun sdDownloads(): File {
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    return if (directory.exists()) {
        directory
    } else {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
    }
}

/**
 * 下载目录, Android11+不需要权限就可以访问
 * /storage/emulated/0/Documents
 * */
fun sdDocuments(): File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    if (directory.exists()) {
        directory
    } else {
        sdDownloads()
    }
} else {
    sdDownloads()
}

/**
 * /storage/emulated/0/Documents 公共目录, Android11+不需要权限
 * */
fun sdDocumentFolderPath(folder: String = "", mk: Boolean = true): String {
    val directory = sdDocuments()
    val folderFile = File(directory.absolutePath, folder)
    if (mk) {
        folderFile.mkdirs()
    }
    return folderFile.absolutePath
}

//region ---SD卡---

/**SD卡的权限
 * android 11+ 使用 [android.os.Environment.isExternalStorageManager] 判断权限
 * [com.angcyo.base.ActivityExKt.requestSdCardPermission]
 * */
fun sdCardPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
} else {
    listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

/**判断是否有sd卡的写入权限
 *
 * [com.angcyo.base.ActivityExKt.requestSdCardPermission]
 * */
fun haveSdCardPermission(context: Context = app()) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        context.havePermission(sdCardPermission())
    }

/**
 * SD卡路径,
 * /storage/emulated/0
 * /storage/emulated/0/test
 * [Environment.getExternalStorageState]
 * [Environment.MEDIA_MOUNTED]
 *
 * [Manifest.permission.WRITE_EXTERNAL_STORAGE]
 *
 * [Environment.getExternalStorageDirectory]
 * */
fun sdFolderPath(folder: String = "", mk: Boolean = true): String {
    val folderFile = File(Environment.getExternalStorageDirectory().absolutePath, folder)
    if (mk) {
        folderFile.mkdirs()
    }
    return folderFile.absolutePath
}

//endregion ---SD卡---