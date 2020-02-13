package com.angcyo.library.ex

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileDescriptor
import java.io.InputStream

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/03
 */

fun fileUri(context: Context, file: File): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //content://com.angcyo.uicore.demo/sdcard/Android/data/com.angcyo.uicore.demo/files/demo/camera/2020-02-13_15-02-38-993.jpeg
        //scheme:content
        FileProvider.getUriForFile(context, context.packageName, file).run {
            context.grantUriPermission(
                context.packageName, this,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            this
        }
    } else {
        //file:///storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/demo/camera/2020-02-13_15-03-59-694.jpeg
        //scheme:file
        Uri.fromFile(file)
    }

    //https://www.baidu.com
    //Uri.parse("https://www.baidu.com") scheme:https

    //content://media/external/file/101295
    //scheme:content
}

fun Uri.inputStream(context: Context): InputStream? {
    return context.contentResolver.openInputStream(this)
}

fun <R> Uri.use(context: Context, block: (InputStream) -> R): R? {
    return inputStream(context)?.use(block)
}

fun Uri.fd(context: Context?): FileDescriptor? {
    val resolver = context?.contentResolver
    val parcelFileDescriptor = resolver?.openFileDescriptor(this, "r")
    return parcelFileDescriptor?.fileDescriptor
}

/**是否是http的uri资源*/
fun Uri?.isHttpScheme(): Boolean {
    if (this == null || scheme == null) {
        return false
    }
    return scheme?.toLowerCase()?.startsWith("http") == true
}

/**是否是可直接读写file的uri资源*/
fun Uri?.isFileScheme(): Boolean {
    if (this == null || path.isNullOrBlank()) {
        return false
    }
    return path.isFileExist()
}