package com.angcyo.library.ex

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import com.angcyo.library.L
import com.angcyo.library.app
import java.io.File
import java.io.InputStream

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

/**保存到DCIM*/
fun Context.saveToDCIM(file: File): Boolean {
    val filename = file.name

    return try {
        return saveToDCIM(file.inputStream(), filename)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun Context.saveToDCIM(input: InputStream, filename: String): Boolean {
    val values = ContentValues()
    val mimeType = filename.mimeType()

    values.put(MediaStore.Images.Media.TITLE, filename)
    values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
    values.put(MediaStore.Images.Media.DESCRIPTION, filename)
    values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

    //values.put("relative_path", "DCIM/demo")//相对路径, 有限制.

    val uri = when {
        mimeType.isImageMimeType() -> contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        mimeType.isVideoMimeType() -> contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            values
        )
        mimeType.isAudioMimeType() -> contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            values
        )
        else -> contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
    }

    return uri?.run {
        try {
            input.use { input ->
                contentResolver.openOutputStream(uri)?.use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    } ?: false
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

/**
 * 请求拿到音频焦点
 */
fun Context.requestAudioFocus() {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.requestAudioFocus(
        null,
        AudioManager.STREAM_MUSIC,
        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
    ) //请求焦点
}

/**
 * 释放音频焦点
 */
fun Context.abandonAudioFocus() {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.abandonAudioFocus(null) //放弃焦点
}

/**从[assets]中读取字符串*/
fun Context.readAssets(fileName: String): String? {
    return try {
        app().assets.open(fileName).reader().readText()
    } catch (e: Exception) {
        L.w(e)
        null
    }
}