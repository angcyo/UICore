package com.angcyo.core.component.file

import android.content.Context
import com.angcyo.library.L
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/30
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object FileUtils {
    fun write(
        context: Context,
        type: String = "",
        name: String,
        data: String,
        append: Boolean = true
    ) {
        // /storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/$type
        val externalFilesDir = context.getExternalFilesDir(type)
        externalFilesDir?.also {
            try {
                File(it, name).apply {
                    if (append) {
                        appendText(data)
                    } else {
                        writeText(data)
                    }
                }
            } catch (e: Exception) {
                L.e("写入文件失败:$e")
            }
        }
    }
}