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
            onResult(MediaStore.Images.Media.getBitmap(contentResolver, uri))
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