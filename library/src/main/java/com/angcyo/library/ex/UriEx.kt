package com.angcyo.library.ex

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.model.MediaBean
import java.io.*
import java.util.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/03
 */

fun File.toFileUri(context: Context = app()): Uri = fileUri(context, this)

fun fileUri(context: Context, file: String): Uri {
    return fileUri(context, File(file))
}

/**非本应用的文件, 请勿使用[permission]*/
fun fileUri(context: Context, file: File, permission: Boolean = true): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && permission) {
        //content://com.angcyo.uicore.demo/sdcard/Android/data/com.angcyo.uicore.demo/files/demo/camera/2020-02-13_15-02-38-993.jpeg
        //content://com.hingin.l1.hiprint/sdcard/Android/data/com.hingin.l1.hiprint/files/apk/LaserPecker2.apk
        //scheme:content
        FileProvider.getUriForFile(context, context.packageName, file).apply {
            context.grantUriPermission(
                context.packageName,
                this,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    } else {
        //file:///storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/demo/camera/2020-02-13_15-03-59-694.jpeg
        //scheme:file
        Uri.fromFile(file)
    }

    //https://www.baidu.com
    //Uri.parse("https://www.baidu.com") scheme:https

    //content://media/external/file/101295
    //scheme:content
}

fun Uri.inputStream(context: Context): InputStream? {
    return context.contentResolver.openInputStream(this)
}

fun <R> Uri.use(context: Context, block: (InputStream) -> R): R? {
    return inputStream(context)?.use(block)
}

fun Uri.fd(context: Context?): FileDescriptor? {
    val resolver = context?.contentResolver
    val parcelFileDescriptor = resolver?.openFileDescriptor(this, "r")
    return parcelFileDescriptor?.fileDescriptor
}

/**是否是http的uri资源*/
fun Uri?.isHttpScheme(): Boolean {
    if (this == null || scheme == null) {
        return false
    }
    return scheme?.startsWith("http") == true
}

fun String?.isHttpScheme(): Boolean {
    if (this.isNullOrBlank()) {
        return false
    }
    val case = this.toLowerCase(Locale.getDefault())
    return case.startsWith("http://") || case.startsWith("https://")
}

fun String?.isDataScheme(): Boolean {
    if (this.isNullOrBlank()) {
        return false
    }
    val case = this.toLowerCase(Locale.getDefault())
    return case.startsWith("data://")
}

/**是否是可直接读写file的uri资源*/
fun Uri?.isFileScheme(): Boolean {
    if (this == null || path.isNullOrBlank()) {
        return false
    }
    return scheme?.startsWith("file") == true
    //return path.isFileExist() //有性能损耗
}

fun Uri.query(context: Context = app()): MediaBean? {
    /**
     * 全部媒体数据 - PROJECTION
     * 需要返回的数据库字段
     */
    val ALL_PROJECTION = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.SIZE
    )

    return context.contentResolver.query(
        this,
        ALL_PROJECTION,
        null,
        null,
        MediaStore.Images.Media._ID
    )?.use {
        val count = it.count
        if (count <= 0) {
            null
        } else {
            it.moveToFirst()
            val data = it
            val mediaBean = MediaBean()
            mediaBean.id = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[0]))
            mediaBean.localPath = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[1]))
            mediaBean.displayName = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[2]))
            mediaBean.addTime = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[3]))
            mediaBean.mimeType = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[4]))
            mediaBean.fileSize = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[5]))
            L.d("$this->$mediaBean")
            mediaBean
        }
    }
}

/**根据scheme获取, 能加载的url*/
fun Uri?.loadUrl(): String? {
    return when {
        this == null -> null
        isFileScheme() -> path
        isHttpScheme() -> toString()
        //this?.encodedPath
        //Uri.decode(this?.encodedPath)
        else -> toString()
    }
}

/**拍照返回后, 从[Uri]中获取文件路径*/
@RequiresApi(Build.VERSION_CODES.KITKAT)
fun Uri.getPathFromUri(): String? {
    var path: String? = null
    try {
        val sdkVersion = Build.VERSION.SDK_INT
        path = if (sdkVersion >= 19) {
            getPathFromIntentData()
        } else {
            getRealFilePath()
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return path
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
fun Uri.isExternalStorageDocument(): Boolean {
    return "com.android.externalstorage.documents" == authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
fun Uri.isDownloadsDocument(): Boolean {
    return "com.android.providers.downloads.documents" == authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
fun Uri.isMediaDocument(): Boolean {
    return "com.android.providers.media.documents" == authority
}

/**从[Uri]中获取文件路径, 复制文件*/
fun Uri.getPathByCopyFile(context: Context = app()): String? {
    val uri = this
    val fileName = uri.getFileName(context)
    val cacheDir: File = getDocumentCacheDir(context)
    val file: File? = generateFileName(fileName, cacheDir)
    var destinationPath: String? = null
    if (file != null) {
        destinationPath = file.absolutePath
        uri.saveFileFromUri(destinationPath)
    }
    return destinationPath
}

fun getDocumentCacheDir(context: Context): File {
    val dir = File(context.cacheDir, "documents")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    return dir
}

fun Uri.getFileName(context: Context = app()): String? {
    val uri = this
    val mimeType = context.contentResolver.getType(uri)
    var filename: String? = null
    if (mimeType == null) {
        filename = uri.toString().fileNameByPath()
    } else {
        val returnCursor = context.contentResolver.query(
            uri, null,
            null, null, null
        )
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            filename = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
    }
    return filename
}

/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context       The context.
 * @param uri           The Uri to query.
 * @param selection     (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)
    try {
        cursor = context.contentResolver.query(
            uri!!, projection, selection, selectionArgs,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(columnIndex)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    return null
}

/**
 * 相册选择图片后获取图片
 * 专为Android4.4以上设计的从Uri获取文件路径
 * [Intent.ACTION_PICK]
 * [Intent.ACTION_GET_CONTENT]
 * [Intent.ACTION_OPEN_DOCUMENT]
 * https://blog.csdn.net/xietansheng/article/details/115763279
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
fun Uri.getPathFromIntentData(context: Context = app()): String? {
    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    val uri = this
    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if (uri.isExternalStorageDocument()) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            return if ("primary".equals(type, ignoreCase = true)) {
                Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else {
                uri.getPathByCopyFile(context)
            }
        } else if (uri.isDownloadsDocument()) {
            val id = DocumentsContract.getDocumentId(uri)
            if (id.startsWith("raw:")) {
                return id.replaceFirst("raw:".toRegex(), "")
            }
            val contentUriPrefixesToTry = arrayOf(
                "content://downloads/public_downloads",
                "content://downloads/my_downloads",
                "content://downloads/all_downloads"
            )
            for (contentUriPrefix in contentUriPrefixesToTry) {
                val contentUri =
                    ContentUris.withAppendedId(Uri.parse(contentUriPrefix), id.toLong())
                try {
                    val path: String? = getDataColumn(
                        context,
                        contentUri,
                        null,
                        null
                    )
                    if (path != null && Build.VERSION.SDK_INT < 29) {
                        return path
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 在某些android8+的手机上，无法获取路径，所以用拷贝的方式，获取新文件名，然后把文件发出去
            return uri.getPathByCopyFile(context)
        } else if (uri.isMediaDocument()) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            if ("image" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if ("video" == type) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if ("audio" == type) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            var path: String? = getDataColumn(
                context,
                contentUri,
                selection,
                selectionArgs
            )
            if (TextUtils.isEmpty(path) || Build.VERSION.SDK_INT >= 29) {
                path = uri.getPathByCopyFile(context)
            }
            return path
        }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) {
        var path: String? = getDataColumn(context, uri, null, null)
        if (TextUtils.isEmpty(path) || Build.VERSION.SDK_INT >= 29) {
            // 在某些华为android9+的手机上，无法获取路径，所以用拷贝的方式，获取新文件名，然后把文件发出去
            path = uri.getPathByCopyFile(context)
        }
        return path
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        return uri.path
    }
    return null
}

/**将[Uri]对应的流, 保存到文件[destinationPath]*/
fun Uri.saveFileFromUri(destinationPath: String, context: Context = app()) {
    val uri = this
    var `is`: InputStream? = null
    var bos: BufferedOutputStream? = null
    try {
        `is` = context.contentResolver.openInputStream(uri)
        bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
        val buf = ByteArray(1024)
        `is`!!.read(buf)
        do {
            bos.write(buf)
        } while (`is`.read(buf) != -1)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            `is`?.close()
            bos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun Uri.getRealFilePath(content: Context = app()): String? {
    val uri = this
    val scheme = uri.scheme
    var data: String? = null
    if (scheme == null) {
        data = uri.path
    } else if (ContentResolver.SCHEME_FILE == scheme) {
        data = uri.path
    } else if (ContentResolver.SCHEME_CONTENT == scheme) {
        val cursor: Cursor? = content.contentResolver.query(
            uri,
            arrayOf(MediaStore.Images.ImageColumns.DATA),
            null,
            null,
            null
        )
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                if (index > -1) {
                    data = cursor.getString(index)
                }
            }
            cursor.close()
        }
    }
    return data
}