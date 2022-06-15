package com.angcyo.component

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.angcyo.fragment.FragmentBridge
import com.angcyo.library.ex.bitmapDegree
import com.angcyo.library.ex.getPathFromUri
import com.angcyo.library.ex.rotate

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/12
 */

/**从相册选择图片*/
fun FragmentActivity.getPhoto(onResult: (Bitmap?) -> Unit) {
    getPhoto(supportFragmentManager, onResult)
}

fun Fragment.getPhoto(onResult: (Bitmap?) -> Unit) {
    context?.getPhoto(parentFragmentManager, onResult)
}

fun Context.getPhoto(fragmentManager: FragmentManager, onResult: (Bitmap?) -> Unit) {
    fragmentManager.getFile("image/*") { uri ->
        if (uri == null) {
            onResult(null)
        } else {
            val path = uri.getPathFromUri()
            val degree = path?.bitmapDegree()
            //val pathBitmap = path?.toBitmap()
            val uriBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            onResult(uriBitmap.rotate(degree?.toFloat() ?: 0f))
        }
    }
}

/**获取一个文件
 * [type] "font/`*`
 * */
fun FragmentManager.getFile(type: String, onResult: (Uri?) -> Unit) {

    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = type

    FragmentBridge.install(this).startActivityForResult(intent) { resultCode, data ->
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            //uri?.getPathFromIntentData()
            onResult(uri)
        } else {
            onResult(null)
        }
    }
}