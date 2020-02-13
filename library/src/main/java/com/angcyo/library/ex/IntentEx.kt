package com.angcyo.library.ex

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/12
 */

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