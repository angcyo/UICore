package com.angcyo.library.ex

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.component.lastContext
import com.angcyo.library.libCacheFile
import com.angcyo.library.libCacheFolderPath
import com.angcyo.library.model.MediaBean
import com.angcyo.library.utils.fileNameUUID
import java.io.*
import java.nio.charset.Charset
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

fun Uri.inputStream(context: Context = app(), flags: Int = 0): InputStream? {
    val takeFlags =
        flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    // 如果 provider 支持 persistable 且 intent 带有这个标志，可以取持久权限（大多数 FileProvider 不支持）
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            context.contentResolver.takePersistableUriPermission(this, takeFlags)
        }
    } catch (e: Exception) {
        // ignore or log；许多 FileProvider 不允许持久化
        e.printStackTrace()
    }
    return context.contentResolver.openInputStream(this)
}

/**转存数据流
 * @return 文件路径*/
fun Uri.saveTo(
    filePath: String = libCacheFile().absolutePath,
    context: Context = app(),
    flags: Int = 0
): String {
    inputStream(context, flags)?.copyToFile(filePath)
    return filePath
}

/**将流读出并写入文件[filePath]*/
fun InputStream.copyToFile(filePath: String, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var bytesCopied: Long = 0
    use {
        val out = FileOutputStream(File(filePath))
        bytesCopied = it.copyTo(out, bufferSize)
        out.close()
    }
    return bytesCopied
}

/**默认按照原文件名存储
 * [folderPath] 需要存储到文件夹
 * [fileName] 重命名文件, 包含扩展名. 不指定则使用原文件名
 * @return 文件路径
 * @RequiresApi(Build.VERSION_CODES.KITKAT)
 * */
fun Uri.saveToFolder(
    folderPath: String = libCacheFolderPath(),
    fileName: String? = null,
    context: Context = app()
): String {
    val name = if (fileName == null) {
        val name = getShowName()
        name.ifBlank {
            val path = getPathFromUri()
            path?.lastName() ?: fileNameUUID()
        }
    } else {
        fileName
    }
    return saveTo(File(folderPath, name).absolutePath, context)
}

/**从[Uri]中读取字节数组数据
 * [ByteArray]*/
fun Uri.readBytes(context: Context = app()): ByteArray? {
    return inputStream(context)?.use {
        it.readBytes()
    }
}

/**从[Uri]中读取字符串*/
fun Uri.readString(context: Context = app(), charset: Charset = Charsets.UTF_8): String? {
    return readBytes(context)?.run { String(this, charset) }
}

/**从[Uri]中读取字符串*/
fun Uri.readBitmap(context: Context = app()): Bitmap? {
    return readBytes(context)?.run { toBitmap() }
}

fun <R> Uri.use(context: Context, block: (InputStream) -> R): R? {
    return inputStream(context)?.use(block)
}

/**[FileDescriptor]*/
fun Uri.fd(context: Context? = lastContext): FileDescriptor? = pfd(context)?.fileDescriptor

/**[ParcelFileDescriptor]*/
fun Uri.pfd(context: Context? = lastContext): ParcelFileDescriptor? {
    val resolver = context?.contentResolver
    return resolver?.openFileDescriptor(this, "r")
}

/**[ParcelFileDescriptor]*/
fun File.pfd(mode: Int = ParcelFileDescriptor.MODE_READ_ONLY): ParcelFileDescriptor? =
    ParcelFileDescriptor.open(this, mode)

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
    val case = this.lowercase(Locale.getDefault())
    return case.startsWith("http://") || case.startsWith("https://")
}

fun String?.isDataScheme(): Boolean {
    if (this.isNullOrBlank()) {
        return false
    }
    val case = this.lowercase(Locale.getDefault())
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

/**是否是可直接读写file的uri资源
 * content://com.angcyo.uicore.demo/sdcard/Android/data/com.angcyo.uicore.demo/files/demo/camera/2022-12-13_10-00-20-098.jpeg
 * */
fun Uri?.isContentScheme(): Boolean {
    this ?: return false
    return scheme?.startsWith("content") == true
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
        isContentScheme() -> path ?: getPathFromUri()
        //this?.encodedPath
        //Uri.decode(this?.encodedPath)
        else -> toString()
    }
}

/**获取[Uri]对应的文件显示名
 * [Uri.getDisplayName]*/
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
        returnCursor?.use {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            filename = returnCursor.getString(nameIndex)
        }
    }
    return filename
}

/**不为空的返回值[getDisplayName]*/
fun Uri.getShowName(containEx: Boolean = true, context: Context = app()): String =
    getDisplayName(context, containEx) ?: "$this".decode().lastName()

/**从[Uri]中获取对应的显示名称
 * [containEx] 是否要包含扩展名
 * [Uri.getFileName]*/
fun Uri.getDisplayName(context: Context = app(), containEx: Boolean = false): String? {
    var result: String? = null
    var lastResult: String? = null

    val contentResolver = context.contentResolver
    val entityColumns = arrayOf(
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.TITLE
    )
    val cursor: Cursor? = contentResolver.query(
        this,
        entityColumns,
        null,
        null,
        null
    )

    if (cursor != null && cursor.moveToFirst()) {
        cursor.use {

            //1.
            var index = cursor.getColumnIndex(entityColumns[1])
            if (index != -1) {
                val fileName = cursor.getString(index)// G10.dxf /ke.gcode
                result = fileName
                if (containEx && result?.contains(".") == false) {
                    //不包含扩展名
                    lastResult = result
                    result = null
                }
            }

            //2.
            if (result.isNullOrBlank()) {
                index = cursor.getColumnIndex(entityColumns[2])
                if (index != -1) {
                    val fileTitle = cursor.getString(index)
                    result = fileTitle

                    if (containEx && result?.contains(".") == false) {
                        //不包含扩展名
                        lastResult = result
                        result = null
                    }
                }

                //3.
                if (result.isNullOrBlank()) {
                    index = cursor.getColumnIndex(entityColumns[0])
                    if (index != -1) {
                        val filePath = cursor.getString(index)// /storage/emulated/0/G10.dxf
                        result = filePath?.decode()?.lastName()
                        if (containEx && result?.contains(".") == false) {
                            //不包含扩展名
                            lastResult = result
                            result = null
                        }
                    }
                }
            }
        }
    }
    if (containEx && result.isNullOrBlank()) {
        //需要扩展名, 但是又没有获取到
        result = getPathFromUri()?.lastName()
        if (result?.contains(".") == false) {
            //不包含扩展名
            result = lastResult
        }
    }
    return result
}


/**拍照返回后, 从[Uri]中获取文件路径*/
fun Uri.getPathFromUri(): String? {
    var path: String? = null
    try {
        val sdkVersion = Build.VERSION.SDK_INT
        path = if (sdkVersion >= Build.VERSION_CODES.KITKAT) {
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
            if (columnIndex != -1) {
                return cursor.getString(columnIndex)
            }
            return null
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
fun Uri.getPathFromIntentData(context: Context = app()): String? {
    val uri = this
    // DocumentProvider
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
        DocumentsContract.isDocumentUri(context, uri)
    ) {
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

/**删除所有[Uri]
 * 返回结果表示, 是否全部删除成功*/
fun List<Uri>.deleteAllUri(context: Context = app()): Boolean {
    var result = true
    forEach {
        if (!it.delete(context)) {
            result = false
        }
    }
    return result
}

/**删除指定的uri
 * 如果uri不存在, 则会删除失败
 * [java.lang.SecurityException: com.angcyo.acc.helper has no access to content://media/external/images/media/160819]
 * */
fun Uri.delete(context: Context = app()): Boolean {
    val int = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.contentResolver.delete(this, null)
        } else {
            0
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
    L.d("删除:${this} ${int}个.")
    return int > 0
}

/**返回指定的uri是否有数据*/
fun Uri.exists(context: Context = app()): Boolean {
    val count = try {
        val cursor = context.contentResolver.query(
            this,
            arrayOf(MediaStore.MediaColumns.DATA),
            null,
            null,
            null
        )
        val count = cursor?.count
        cursor?.close()
        count ?: 0
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
    return count > 0
}