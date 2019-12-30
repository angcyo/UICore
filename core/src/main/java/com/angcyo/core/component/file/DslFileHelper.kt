package com.angcyo.core.component.file

import android.content.Context
import android.os.Environment
import com.angcyo.http.base.jsonString
import com.angcyo.library.L
import java.io.File

/**
 * 文件操作
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslFileHelper {
    fun init(context: Context) {

        val externalStorageState = Environment.getExternalStorageState()
        val externalStorageDirectory = Environment.getExternalStorageDirectory()

        val mkdirs = File(externalStorageDirectory, "_test").mkdirs()


        //此目录下的所有文件, 卸载就没了.

        ///storage/emulated/0/Android/data/com.angcyo.uicore.demo/files
        val externalFilesDir = context.getExternalFilesDir("")
        ///storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/folder
        val externalFilesDir1 = context.getExternalFilesDir("folder")
        ///storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/Documents
        val externalFilesDir2 = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)


        Environment.getExternalStorageState()

//        try {
//            File(externalStorageDirectory, "_testfile").writeText("test")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

        val listFiles = externalFilesDir?.listFiles()

        File(externalFilesDir, "_testfile").writeText("test1")
        File(externalFilesDir1, "_testfile").writeText("test2")
        File(externalFilesDir2, "_testfile").writeText("test3")

        L.i("this...$mkdirs")

        L.i(this)

        L.i(jsonString {
            add("a", 1)
            add("a2", 1)
            add("a3", 1)
            add("a4", 1)
        })
    }
}