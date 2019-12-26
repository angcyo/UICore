package com.angcyo.core.component

import android.os.Environment
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
    fun init() {

        val externalStorageState = Environment.getExternalStorageState()
        val externalStorageDirectory = Environment.getExternalStorageDirectory()

        val mkdirs = File(externalStorageDirectory, "_test").mkdirs()


        //File(externalStorageDirectory, "_testfile").writeText("test")

        L.i("this...$mkdirs")
    }
}