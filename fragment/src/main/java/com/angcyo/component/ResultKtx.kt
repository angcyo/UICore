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
import com.angcyo.library.ex.*

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

/**
 * [com.angcyo.component.getPhoto]
 * [com.angcyo.picker.DslPicker.takePhoto]
 * [com.angcyo.picker.DslPicker.takePhotoBitmap]
 * */
fun Context.getPhoto(fragmentManager: FragmentManager, onResult: (Bitmap?) -> Unit) {
    fragmentManager.getFile("image/*") { uri ->
        if (uri == null) {
            onResult(null)
        } else {
            val path = uri.getPathFromUri()
            val degree = path?.bitmapDegree() ?: 0
            //val pathBitmap = path?.toBitmap()

            //val newPath = uri.saveToFolder()
            //val newBitmap = newPath.toBitmap()

            val uriBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            onResult(uriBitmap.rotate(degree.toFloat()))
        }
    }
}

/**获取一个文件 */
fun FragmentActivity.getFile(type: String, onResult: (Uri?) -> Unit) {
    supportFragmentManager.getFile(type, onResult)
}

/**获取一个文件*/
fun Fragment.getFile(type: String, onResult: (Uri?) -> Unit) {
    parentFragmentManager.getFile(type, onResult)
}

/**获取一个文件
 * [type] "font/`*`
 * [com.angcyo.core.component.FileSelectorFragment.fileSelector]
 * */
fun FragmentManager.getFile(type: String, onResult: (Uri?) -> Unit) {
    //val action = if (type.isImageMimeType()) Intent.ACTION_PICK else Intent.ACTION_GET_CONTENT
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
