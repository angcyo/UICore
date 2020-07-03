package com.angcyo.library.ex

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import com.angcyo.library.L
import com.angcyo.library.component.queryActivities

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/12
 */

fun Int.isResultOk() = this == Activity.RESULT_OK

fun Int.isResultCanceled() = this == Activity.RESULT_CANCELED

fun Intent.baseConfig(context: Context) {
    if (context !is Activity) {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

fun Intent.uriConfig(context: Context, uri: Uri) {
    putExtra(MediaStore.EXTRA_OUTPUT, uri)
    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    if (Build.VERSION.SDK_INT < 21) {
        context.grantUriPermission(
            context.packageName, uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }

    baseConfig(context)
}

/**系统拍照[android.Manifest.permission.CAMERA]*/
fun takePhotoIntent(context: Context, saveUri: Uri): Intent {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    intent.uriConfig(context, saveUri)
    return intent
}

/**系统录制[android.Manifest.permission.CAMERA]*/
fun takeVideoIntent(
    context: Context,
    saveUri: Uri,
    videoQuality: Int = 1,
    maxSize: Long = Long.MAX_VALUE, //字节
    maxDuration: Int = -1//秒
): Intent {
    val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
    intent.uriConfig(context, saveUri)

    intent.apply {
        //视频质量, 0:低质量, 1:高质量
        putExtra(MediaStore.EXTRA_VIDEO_QUALITY, videoQuality)
        //最大录制大小
        putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxSize)
        //最大录制时长, 秒
        putExtra(MediaStore.EXTRA_DURATION_LIMIT, maxDuration)
    }

    return intent
}

/**
 * 打开网页的[Intent],
 * [component] 可以指定打开的应用程序组件. 指定应用.
 * */
fun String.urlIntent(component: ComponentName? = null): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse(this)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setComponent(component)
    }
}

/**
 * Intent.FILL_IN_ACTION or
 * Intent.FILL_IN_CATEGORIES or
 * Intent.FILL_IN_CLIP_DATA or
 * Intent.FILL_IN_COMPONENT or
 * Intent.FILL_IN_DATA or
 * Intent.FILL_IN_IDENTIFIER or
 * Intent.FILL_IN_PACKAGE or
 * Intent.FILL_IN_SELECTOR or
 * Intent.FILL_IN_SOURCE_BOUNDS
 *
 * 默认填充所有类型的数据.
 *
 * 使用0, 可以只填充[mExtras]的数据
 *
 * */
fun Intent.fillFrom(other: Intent?, flag: Int = 255): Intent {
    if (other != null) {
        fillIn(other, flag)
    }
    return this
}

/**跳转应用详情页面*/
fun Context.toApplicationDetailsSettings(packageName: String = getPackageName()) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

/**打开程序, 启动应用*/
fun Context.openApp(packageName: String? = this.packageName, className: String? = null): Intent? {
    if (packageName.isNullOrBlank()) {
        L.w("packageName is null!")
        return null
    }
    val intent = if (className.isNullOrEmpty()) {
        packageManager.getLaunchIntentForPackage(packageName)
    } else {
        Intent().run {
            setClassName(packageName, className)
            if (queryActivities().isEmpty()) {
                null
            } else {
                this
            }
        }
    }

    intent?.baseConfig(this)

    if (intent == null) {
        L.w("packageName launch intent is null!")
        return intent
    }

    startActivity(intent)
    return intent
}

