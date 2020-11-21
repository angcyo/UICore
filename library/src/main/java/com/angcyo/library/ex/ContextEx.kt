package com.angcyo.library.ex

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.hardware.Camera
import android.media.AudioManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.Window
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import com.angcyo.library.L
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

/**获取根的宽度*/
fun Context.getRootWidth(): Int {
    var displayWidth = 0
    val display: Display =
        activityContent()?.windowManager?.defaultDisplay ?: return displayWidth
    val point = Point()
    displayWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        display.getRealSize(point)
        point.x
    } else {
        val dm = DisplayMetrics()
        activityContent()?.windowManager?.defaultDisplay?.getMetrics(dm)
        dm.widthPixels
    }
    return displayWidth
}

/**获取根的高度*/
fun Context.getRootHeight(): Int {
    var displayHeight = 0
    val display: Display = activityContent()?.windowManager?.defaultDisplay ?: return displayHeight
    val point = Point()
    displayHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        display.getRealSize(point)
        point.y
    } else {
        val dm = DisplayMetrics()
        activityContent()?.windowManager?.defaultDisplay?.getMetrics(dm)
        dm.heightPixels
    }
    return displayHeight
}

/**如果有权限, 则直接返回true. 否则返回false, 并请求权限, 但是权限回调无法拿到
 * [com.angcyo.base.ActivityEx.checkAndRequestPermission)]
 * */
fun Context.checkPermissions(vararg permissions: String): Boolean =
    if (havePermissions(*permissions)) {
        true
    } else {
        if (this is Activity) {
            ActivityCompat.requestPermissions(this, permissions, 999)
        } else {
            L.w("context is not activity.")
        }
        false
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

/**保存到相册[DCIM]*/
fun Context.saveToDCIM(file: File): Pair<Boolean, Uri?> {
    val filename: String = file.name
    return try {
        return saveToDCIM(file.inputStream(), filename)
    } catch (e: Exception) {
        e.printStackTrace()
        false to null
    }
}

fun Context.saveToDCIM(input: InputStream, filename: String): Pair<Boolean, Uri?> {

    checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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

    val result = uri?.run {
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

    return result to uri
}

fun Context.scanUri(uri: Uri) {
    val path = uri.loadUrl()
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(path)

    // Implicit broadcasts will be ignored for devices running API level >= 24
    // so if you only target API level 24+ you can remove this statement
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        if (mimeType.isImageMimeType()) {
            sendBroadcast(Intent(Camera.ACTION_NEW_PICTURE, uri))
        } else if (mimeType.isImageMimeType()) {
            sendBroadcast(Intent(Camera.ACTION_NEW_VIDEO, uri))
        }
    }

    // If the folder selected is an external media directory, this is unnecessary
    // but otherwise other apps will not be able to access our images unless we
    // scan them using [MediaScannerConnection]
    MediaScannerConnection.scanFile(
        this,
        arrayOf(path),
        arrayOf(mimeType)
    ) { path, uri1 ->
        //uri 为空, 表示失败.
        L.i("$path $uri1")
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

/**
 * 请求拿到音频焦点
 * [AudioManager.AUDIOFOCUS_REQUEST_FAILED]
 * [AudioManager.AUDIOFOCUS_REQUEST_GRANTED]
 * [AudioManager.AUDIOFOCUS_REQUEST_DELAYED]
 */
fun Context.requestAudioFocus(
    streamType: Int = AudioManager.STREAM_MUSIC,//请求流的类型
    durationHint: Int = AudioManager.AUDIOFOCUS_GAIN,//请求焦点大概要多长时间
    onAudioFocusChange: (focusChange: Int) -> Unit = {}//监听其他焦点请求的改变
): Int {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return audioManager.requestAudioFocus(
        onAudioFocusChange,
        streamType,
        durationHint
    ) //请求焦点
}

/**
 * 释放音频焦点
 */
fun Context.abandonAudioFocus(change: ((focusChange: Int) -> Unit)? = null): Int {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return audioManager.abandonAudioFocus(change) //放弃焦点
}

/**从[assets]中读取字符串*/
fun Context.readAssets(fileName: String): String? {
    return try {
        assets.open(fileName).reader().readText()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**过滤[assets]中指定[path]路径下的所有文件*/
fun Context.filterAssets(path: String = "", predicate: (String) -> Boolean): List<String>? {
    return try {
        assets.list(path)?.filter(predicate)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**获取[Context]包含的[Activity]*/
fun Context?.activityContent(max: Int = 10): Activity? {
    var ctx = this
    var i = 0
    while (i < max && ctx !is Activity && ctx is ContextWrapper) {
        ctx = ctx.baseContext
        i++
    }
    return if (ctx is Activity) {
        ctx
    } else {
        null
    }
}

/**获取应用程序正在运行的[RunningTaskInfo], 5.0之后获取不到其他应用程序的信息了*/
fun Context?.runningTasks(maxNum: Int = Int.MAX_VALUE): List<ActivityManager.RunningTaskInfo> {
    val activityManager: ActivityManager? =
        this?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    val runningTaskInfoList = activityManager?.getRunningTasks(maxNum)
    return runningTaskInfoList ?: emptyList()
}

/**根据包名[packageName],获取[PackageInfo]*/
fun Context.getPackageInfo(packageName: String): PackageInfo? {
    return try {
        val pm = packageManager
        pm.getPackageInfo(packageName, 0)
    } catch (e: Exception) {
        null
    }
}

/**获取当前进程名*/
fun Context.processName(): String? {
    val pid = Process.myPid()
    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?

    if (am != null) {
        for (process in am.runningAppProcesses) {
            if (process.pid == pid) {
                return process.processName
            }
        }
    }

    return null
}