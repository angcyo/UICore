package com.angcyo.library.ex

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.model.MediaBean
import java.io.File
import java.io.FileDescriptor
import java.io.InputStream

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/03
 */

fun fileUri(context: Context, file: String?): Uri {
    return fileUri(context, File(file))
}

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
    return scheme?.startsWith("http") == true
}

fun String?.isHttpScheme(): Boolean {
    if (this.isNullOrBlank()) {
        return false
    }
    val case = this.toLowerCase()
    return case.startsWith("http://") || case.startsWith("https://")
}

/**是否是可直接读写file的uri资源*/
fun Uri?.isFileScheme(): Boolean {
    if (this == null || path.isNullOrBlank()) {
        return false
    }
    return path.isFileExist()
}

fun Uri.query(context: Context = app()): MediaBean? {
    /**
     * 全部媒体数据 - PROJECTION
     * 需要返回的数据库字段
     */
    val ALL_PROJECTION = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.SIZE
    )

    return context.contentResolver.query(
        this,
        ALL_PROJECTION,
        null,
        null,
        MediaStore.Images.Media._ID
    )?.use {
        val count = it.count
        if (count <= 0) {
            null
        } else {
            it.moveToFirst()
            val data = it
            val mediaBean = MediaBean()
            mediaBean.id = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[0]))
            mediaBean.localPath = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[1]))
            mediaBean.displayName = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[2]))
            mediaBean.addTime = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[3]))
            mediaBean.mimeType = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[4]))
            mediaBean.fileSize = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[5]))
            L.d("$this->$mediaBean")
            mediaBean
        }
    }
}