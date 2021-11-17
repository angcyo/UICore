package com.angcyo.library.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

/**
 * 系统[Intent]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object SysIntent {

    /**调用系统的界面选择图片*/
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getPhoto(fragment: Fragment, requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val mimetypes = arrayOf("image/*", "video/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)

        fragment.startActivityForResult(intent, requestCode)
    }


}

//must call registerForActivityResult() before they are created

/**请求权限*/
fun ActivityResultCaller.requestPermissionLauncher(callback: ActivityResultCallback<Boolean>): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.RequestPermission(), callback)
}

/**请求多个权限*/
fun ActivityResultCaller.requestMultiplePermissionsLauncher(callback: ActivityResultCallback<Map<String, Boolean>>): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), callback)
}

/**拍照, 直接获取[Bitmap]*/
fun ActivityResultCaller.takePicturePreviewLauncher(callback: ActivityResultCallback<Bitmap?>): ActivityResultLauncher<Void> {
    return registerForActivityResult(ActivityResultContracts.TakePicturePreview(), callback)
}

/**拍照, 并将结果保存到[uri]中*/
fun ActivityResultCaller.takePictureLauncher(callback: ActivityResultCallback<Boolean>): ActivityResultLauncher<Uri> {
    return registerForActivityResult(ActivityResultContracts.TakePicture(), callback)
}

/**录制, 并将结果保存到[uri]中, 同时还会返回缩略图*/
fun ActivityResultCaller.takeVideoLauncher(callback: ActivityResultCallback<Bitmap?>): ActivityResultLauncher<Uri> {
    return registerForActivityResult(ActivityResultContracts.TakeVideo(), callback)
}

/**选择联系人*/
fun ActivityResultCaller.pickContactLauncher(callback: ActivityResultCallback<Uri?>): ActivityResultLauncher<Void> {
    return registerForActivityResult(ActivityResultContracts.PickContact(), callback)
}

/**获取内容, 选择文件/图片/等*/
fun ActivityResultCaller.getContentLauncher(callback: ActivityResultCallback<Uri?>): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.GetContent(), callback)
}

/**获取图片或者视频*/
@RequiresApi(Build.VERSION_CODES.KITKAT)
fun ActivityResultCaller.getImageOrVideoLauncher(callback: ActivityResultCallback<Uri?>): ActivityResultLauncher<Unit?> {
    class ImageOrVideoResult : ActivityResultContract<Unit?, Uri?>() {

        override fun createIntent(context: Context, input: Unit?): Intent {
            return Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
        }
    }

    return registerForActivityResult(ImageOrVideoResult(), callback)
}

/**多选获取内容*/
fun ActivityResultCaller.getMultipleContentsLauncher(callback: ActivityResultCallback<List<Uri>>): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.GetMultipleContents(), callback)
}

/**打开文档*/
fun ActivityResultCaller.openDocumentLauncher(callback: ActivityResultCallback<Uri?>): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.OpenDocument(), callback)
}

/**打开多个文档*/
fun ActivityResultCaller.openMultipleDocumentsResult(callback: ActivityResultCallback<List<Uri>>): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments(), callback)
}

fun ActivityResultCaller.openDocumentTreeLauncher(callback: ActivityResultCallback<Uri?>): ActivityResultLauncher<Uri> {
    return registerForActivityResult(ActivityResultContracts.OpenDocumentTree(), callback)
}

/**直接启动[Intent], 并且获取返回值*/
fun ActivityResultCaller.onActivityLauncher(callback: ActivityResultCallback<ActivityResult>): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult(), callback)
}