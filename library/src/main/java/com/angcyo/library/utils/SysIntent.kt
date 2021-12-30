package com.angcyo.library.utils

import android.Manifest
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.angcyo.library.app
import com.angcyo.library.ex.*
import kotlin.random.Random.Default.nextInt


/**
 * 系统[Intent]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**
 * https://github.com/Atinerlengs/InsertDemo
 * https://github.com/AndroidStudioIst/InsertDemo
 * */
data class SystemBatchBean(
    val name: String = "", // 姓名
    val number: String = "", // 号码
    val callLogType: Int = listOf(1, 2, 3).randomGetOnce()!!, // 通话记录的状态 1:来电 2:去电 3:未接
    val callLogDate: Long = nowTime(), // 通话记录日期
    val callLogDuration: Int = nextInt(10, 160), // 通话记录时长, 秒
)

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

    /**批量插入通话记录
     * requires android.permission.READ_CALL_LOG or android.permission.WRITE_CALL_LOG
     * https://github.com/Atinerlengs/InsertDemo
     * https://github.com/AndroidStudioIst/InsertDemo
     * */
    fun batchAddCallLogs(
        list: List<SystemBatchBean>,
        context: Context = app()
    ): Array<out ContentProviderResult> {

        if (!context.checkPermissions(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG
            )
        ) {
            return emptyArray()
        }

        val ops = ArrayList<ContentProviderOperation>()
        val values = ContentValues()
        for (call in list) {
            values.clear()
            values.put(CallLog.Calls.NUMBER, call.number)
            values.put(CallLog.Calls.TYPE, call.callLogType)
            values.put(CallLog.Calls.DATE, call.callLogDate)
            values.put(CallLog.Calls.DURATION, call.callLogDuration)
            values.put(CallLog.Calls.NEW, "0")
            ops.add(
                ContentProviderOperation
                    .newInsert(CallLog.Calls.CONTENT_URI).withValues(values)
                    .withYieldAllowed(true).build()
            )
        }
        if (ops.isNotEmpty()) {
            return context.contentResolver.applyBatch(CallLog.AUTHORITY, ops)
        }
        return emptyArray()
    }

    /**批量添加通讯录
     * requires android.permission.READ_CONTACTS or android.permission.WRITE_CONTACTS
     * https://github.com/Atinerlengs/InsertDemo
     * https://github.com/AndroidStudioIst/InsertDemo
     * */
    fun batchAddContacts(
        list: List<SystemBatchBean>,
        context: Context = app()
    ): Array<out ContentProviderResult> {

        if (!context.checkPermissions(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
            )
        ) {
            return emptyArray()
        }

        val ops = ArrayList<ContentProviderOperation>()
        var rawContactInsertIndex = 0
        for (contact in list) {
            rawContactInsertIndex = ops.size()
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .withYieldAllowed(true).build()
            )

            // 添加姓名
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        contact.name
                    )
                    .withYieldAllowed(true).build()
            )
            // 添加号码
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.number)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, "")
                    .withYieldAllowed(true).build()
            )
        }
        if (ops.isNotEmpty()) {
            return context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        }
        return emptyArray()
    }
}

/**
 * 注意:
 * must call registerForActivityResult() before they are created
 * 必须在生命周期 Lifecycle.State.STARTED 之前注册
 * [androidx.activity.result.ActivityResultRegistry.register]
 * [androidx.fragment.app.Fragment.prepareCallInternal]
 * */

/**请求单个权限
 * 获取一个请求权限的发射器, 通过这个发射器, 可以用来请求权限, 并且返回请求权限的结果.
 *
 * context.requestPermissionLauncher(ActivityResultCallback {
 *   if (it) {
 *     //有权限
 *   } else {
 *     //无权限
 *   }
 * }).launch(Manifest.permission.ACCESS_FINE_LOCATION)
 *
 * [callback] 请求权限的回调结果
 * */
fun ActivityResultCaller.requestPermissionLauncher(callback: ActivityResultCallback<Boolean>): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.RequestPermission(), callback)
}

/**请求多个权限
 * val permissions = AMapHelper.permissions(true).toTypedArray()
 * context.requestMultiplePermissionsLauncher(ActivityResultCallback {
 *   var result = true
 *   for (p in permissions) {
 *     if (it[p] == false) {
 *       result = false
 *       break
 *     }
 *   }
 *   if (result) {
 *     //有权限
 *   } else {
 *     //无权限
 *   }
 * }).launch(AMapHelper.permissions(true).toTypedArray())
 * */
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