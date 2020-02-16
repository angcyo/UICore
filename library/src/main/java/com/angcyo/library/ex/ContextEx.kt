package com.angcyo.library.ex

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.Window
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import com.angcyo.library.L
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */

/**
 * ContentView 的高度, 包含 DecorView的高度-状态栏-导航栏
 * 当状态栏是透明时, 那么状态栏的高度会是0
 */
fun Context.getContentViewHeight(): Int {
    if (this is Activity) {
        val window = this.window
        return window.findViewById<View>(Window.ID_ANDROID_CONTENT)
            .measuredHeight
    }
    return 0
}

/**是否具有指定的权限*/
fun Context.havePermissions(vararg permissions: String): Boolean {
    return permissions.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.havePermission(permissionList: List<String>): Boolean {
    return permissionList.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/**相机拍摄新照片，并将照片的条目添加到媒体存储。*/
fun Context.scanFile(file: File) {
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

    // Implicit broadcasts will be ignored for devices running API level >= 24
    // so if you only target API level 24+ you can remove this statement
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        if (mimeType.isImageMimeType()) {
            sendBroadcast(Intent(Camera.ACTION_NEW_PICTURE, Uri.fromFile(file)))
        } else if (mimeType.isImageMimeType()) {
            sendBroadcast(Intent(Camera.ACTION_NEW_VIDEO, Uri.fromFile(file)))
        }
    }

    // If the folder selected is an external media directory, this is unnecessary
    // but otherwise other apps will not be able to access our images unless we
    // scan them using [MediaScannerConnection]
    MediaScannerConnection.scanFile(
        this,
        arrayOf(file.absolutePath),
        arrayOf(mimeType)
    ) { path, uri ->
        //uri 为空, 表示失败.
        L.i("$path $uri")
    }
}