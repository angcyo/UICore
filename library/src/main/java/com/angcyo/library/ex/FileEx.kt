package com.angcyo.library.ex

import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

/**文件是否存在*/
fun String.isFileExist(): Boolean {
    return try {
        val file = File(this)
        file.exists() && file.canRead()
    } catch (e: Exception) {
        false
    }
}