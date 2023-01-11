package com.angcyo.library.ex

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.text.format.Formatter
import androidx.annotation.AnyThread
import com.angcyo.library.app
import com.angcyo.library.model.Page
import com.angcyo.library.toastQQ
import java.io.*
import java.nio.channels.FileChannel
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * /kotlin/io/FileReadWrite.kt
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

enum class SizeUnit {
    Byte, KB, MB, GB, TB, PB, Auto
}

/**是否是文件路径*/
fun String?.isFilePath(): Boolean = this?.startsWith(File.separatorChar) == true

/**文件是否存在, 此方法会有性能损耗*/
fun String?.isFileExist(): Boolean {
    return try {
        if (this.isNullOrBlank()) {
            return false
        }
        val file = File(this)
        file.exists() && file.canRead()
    } catch (e: Exception) {
        false
    }
}

/**将文件内容, 转移到另一个文件*/
fun String.transferToFile(filePath: String): File? {
    if (this.equals(filePath, ignoreCase = true)) {
        return null
    }
    var outputChannel: FileChannel? = null
    var inputChannel: FileChannel? = null
    try {
        val target = File(filePath)
        inputChannel = FileInputStream(File(this)).channel
        outputChannel = FileOutputStream(target).channel
        inputChannel.transferTo(0, inputChannel.size(), outputChannel)
        inputChannel.close()
        return target
    } finally {
        inputChannel?.close()
        outputChannel?.close()
    }
}

/**将文件路径对应的文件, 复制到
 * [kotlin.io.Utils.kt:217]*/
fun String?.copyFileTo(target: File, overwrite: Boolean = true): File? {
    return try {
        val file = this?.let { File(it) }
        file?.copyTo(target, overwrite)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**获取文件md5值*/
fun File.md5(): String? {
    return getFileMD5()?.toHexString()
}

fun File.copyTo(path: String, overwrite: Boolean = true) = copyTo(File(path), overwrite)

fun String.file(): File = File(this)

fun String?.fileName(): String? {
    return this?.file()?.name
}

/**从路径中直接获取文件名*/
fun String?.fileNameByPath(): String? {
    if (this == null) {
        return null
    }
    val index = lastIndexOf('/')
    return substring(index + 1)
}

/**文件路径获取文件大小*/
fun String?.fileSize(def: Long = 0L): Long {
    if (TextUtils.isEmpty(this)) {
        return def
    }
    val file = this?.file()
    return if (file?.exists() == true) {
        file.length()
    } else {
        def
    }
}

//---

/**格式化文件大小, 根据系统版本号选择实现方式*/
fun formatFileSize(context: Context, size: Long): String {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        size.fileSizeString()
    } else Formatter.formatFileSize(context, size)
}

/**文件路径获取文件大小*/
fun String?.fileSizeString(): String {
    return fileSize().fileSizeString()
}

/**格式化字节大小 [Long.fileSizeString]*/
fun Int.toSizeString(unit: SizeUnit = SizeUnit.Auto): String = toLong().toSizeString(unit)

/**格式化字节大小 [Long.fileSizeString]*/
fun Long.toSizeString(unit: SizeUnit = SizeUnit.Auto): String = fileSizeString(unit)

/**[this] b 字节数量
 * 返回 [xxKB] [xxMB] [xxGB] [xxTB] [xxPB]
 * */
fun Long.fileSizeString(unit: SizeUnit = SizeUnit.Auto): String {
    val size = this
    var _unit = unit
    if (size < 0) {
        return ""
    }
    val KB = 1024.0
    val MB = KB * 1024
    val GB = MB * 1024
    val TB = GB * 1024
    val PB = TB * 1024
    if (_unit == SizeUnit.Auto) {
        _unit = if (size < KB) {
            SizeUnit.Byte
        } else if (size < MB) {
            SizeUnit.KB
        } else if (size < GB) {
            SizeUnit.MB
        } else if (size < TB) {
            SizeUnit.GB
        } else if (size < PB) {
            SizeUnit.TB
        } else {
            SizeUnit.PB
        }
    }
    return when (_unit) {
        SizeUnit.Byte -> size.toString() + "B"
        SizeUnit.KB -> String.format(Locale.US, "%.2fKB", size / KB)
        SizeUnit.MB -> String.format(Locale.US, "%.2fMB", size / MB)
        SizeUnit.GB -> String.format(Locale.US, "%.2fGB", size / GB)
        SizeUnit.TB -> String.format(Locale.US, "%.2fTB", size / TB)
        SizeUnit.PB -> String.format(Locale.US, "%.2fPB", size / PB)
        else -> size.toString() + "B"
    }
}

//---

/**
 * 获取文件的MD5校验码
 *
 * @param file 文件
 * @return 文件的MD5校验码
 */
fun File.getFileMD5(): ByteArray? {
    var dis: DigestInputStream? = null
    try {
        val fis = FileInputStream(this)
        var md = MessageDigest.getInstance("MD5")
        dis = DigestInputStream(fis, md)
        val buffer = ByteArray(1024 * 256)
        while (dis.read(buffer) > 0);
        md = dis.messageDigest
        return md.digest()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        dis?.close()
    }
    return null
}

/**打开文件*/
fun File.open(context: Context = app()) {
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    //设置intent的Action属性
    intent.action = Intent.ACTION_VIEW
    //获取文件file的MIME类型
    val type: String = this.absolutePath.mimeType() ?: "text/plain"
    //设置intent的data和Type属性。
    intent.setDataAndType(fileUri(context, this), type)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    //跳转
    try {
        //这里最好try一下，有可能会报错。
        // 比如说你的MIME类型是打开邮箱，但是你手机里面没装邮箱客户端，就会报错。
        context.startActivity(intent)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

//    public static void shareBitmap(Context context, byte[] data, boolean shareQQ) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_SEND);//设置分享行为
//        intent.setType("image/*");//设置分享内容的类型
//        intent.putExtra(Intent.EXTRA_STREAM, data);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
//
//        if (shareQQ) {
//            configQQIntent(intent);
//        } else {
//            intent = Intent.createChooser(intent, "分享图片");
//        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
//    }

/** 分享文件 */
@AnyThread
fun String.shareFile(
    fileProvider: Boolean = true,
    toast: Boolean = true,
    context: Context = app()
): Boolean = file().shareFile(context, fileProvider, toast)

/** 分享文件 */
@AnyThread
fun File.shareFile(
    context: Context = app(),
    fileProvider: Boolean = true,
    toast: Boolean = false
): Boolean {
    if (!exists()) {
        if (toast) {
            toastQQ("文件不存在")
        }
        return false
    }

    val fileUri = fileUri(context, this, fileProvider)
    val mimeType = name.mimeType() ?: "*/*"
    val share = Intent(Intent.ACTION_SEND)//Intent.ACTION_SEND_MULTIPLE
    share.putExtra(Intent.EXTRA_STREAM, fileUri) //此处可发送多种文件
    share.type = mimeType
    /*val share = Intent(Intent.ACTION_VIEW)
    share.setDataAndType(fileUri, mimeType)*/
    share.baseConfig(context)
    if (fileProvider) {
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
    try {
        val chooser = Intent.createChooser(share, "发送给...")
        chooser.baseConfig(context)
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return true
}

/**分享图片文件*/
fun File.shareImage(context: Context = app(), content: String?) {
    val share = Intent(Intent.ACTION_SEND)
    share.putExtra(Intent.EXTRA_TEXT, content)
    share.putExtra(Intent.EXTRA_STREAM, fileUri(context, this))
    share.type = "image/*"
    share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(
        Intent.createChooser(share, "发送给...")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

/**分享视频文件*/
fun File.shareVideo(context: Context = app(), content: String?) {
    val share = Intent(Intent.ACTION_SEND)
    share.putExtra(Intent.EXTRA_TEXT, content)
    share.putExtra(Intent.EXTRA_STREAM, fileUri(context, this))
    share.type = "video/*"
    share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(
        Intent.createChooser(share, "发送给...")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun File?.isFile(): Boolean = this?.isFile == true

fun File?.isFolder(): Boolean = this?.isDirectory == true

fun File?.readText() = try {
    if (this?.exists() == true) {
        this.readText(Charsets.UTF_8)
    } else {
        null
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

/**从最后一行开始读取文本, 倒序一行一行读取
 * [limit] 限制需要多少行
 * [truncated] 超限后追加的字符
 * [reversed]  是否是文件末尾开始一行一行读取
 */
fun File?.readReverseText(
    limit: Int = -1,
    truncated: CharSequence = "...",
    reversed: Boolean = true,
) = try {
    this?.readLines(Charsets.UTF_8)?.run {
        (if (reversed) reversed() else this).joinToString(
            "\n",
            limit = limit,
            truncated = truncated
        )
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

/**[readReverseText]*/
fun File?.readTextLines(
    limit: Int = -1,
    truncated: CharSequence = "...",
    reversed: Boolean = false
) = readReverseText(limit, truncated, reversed)

/**读取文件最后多少行的数据*/
fun File?.readTextLastLines(
    limit: Int = -1,
    truncated: CharSequence = "..."
) = try {
    this?.readLines(Charsets.UTF_8)?.run {
        val lastLineIndex = if (limit >= 0) this.size - limit else 0
        this.filterIndexed { index, _ -> index >= lastLineIndex }.joinToString(
            "\n",
            limit = limit,
            truncated = truncated
        )
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun String.readTextLastLines(
    limit: Int = -1,
    truncated: CharSequence = "..."
) = try {
    lines().run {
        val lastLineIndex = if (limit >= 0) this.size - limit else 0
        this.filterIndexed { index, _ -> index >= lastLineIndex }.joinToString(
            "\n",
            limit = limit,
            truncated = truncated
        )
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

/**返回文件的行数*/
fun File?.lines(): Int {
    if (this == null) {
        return -1
    }
    val lineReader = LineNumberReader(FileReader(this))
    return lineReader.use {
        it.skip(Long.MAX_VALUE)
        lineReader.lineNumber + 1
    }
}

fun String.writeText(text: String, append: Boolean = true) = file().writeText(text, append)

/**向文件中写入[text]
 * [append] 是否追加, 否则就是重写*/
fun File.writeText(text: String, append: Boolean = true) {
    if (append) {
        writeText(text)
    } else {
        appendText(text)
    }
}

fun File.toUri(context: Context = app()) = fileUri(context, this)

fun generateFileName(name: String?, directory: File): File? {
    var _name = name
    if (_name == null) {
        return null
    }
    var file = File(directory, _name)
    if (file.exists()) {
        var fileName: String = _name
        var extension = ""
        val dotIndex = _name.lastIndexOf('.')
        if (dotIndex > 0) {
            fileName = _name.substring(0, dotIndex)
            extension = _name.substring(dotIndex)
        }
        var index = 0
        while (file.exists()) {
            index++
            _name = "$fileName($index)$extension"
            file = File(directory, _name)
        }
    }
    try {
        if (!file.createNewFile()) {
            return null
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
    return file
}

/**
 * 枚举文件
 * [recursively] 是否递归子文件夹*/
fun File.eachFile(recursively: Boolean = true, block: (File) -> Unit) {
    val files = listFiles() ?: return
    for (file in files) {
        try {
            if (file.isDirectory && recursively) {
                file.eachFile(recursively, block)
            } else {
                block(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun File.deleteSafe() {
    try {
        delete()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun File.deleteRecursivelySafe() {
    try {
        deleteRecursively()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 通过uri的方式,将文件保存至相册.
 * 返回是否成功和对应的Uri
 * [com.angcyo.library.ex.ContextExKt.saveToDCIM]*/
fun File.saveToDCIM(context: Context = app()) = context.saveToDCIM(this)

/**分页获取文件列表
 * [sortModified] 是否按照时间降序排序, true: 降序排序, false: 升序排序, null:不排序*/
fun File.page(page: Page, sortModifiedDesc: Boolean? = true): List<File> {
    val list = listFiles() ?: return emptyList()
    val targetList = if (sortModifiedDesc == null) {
        list.toList()
    } else if (sortModifiedDesc) {
        list.sortedByDescending { it.lastModified() }
    } else {
        list.sortedBy { it.lastModified() }
    }

    val result = mutableListOf<File>()
    targetList.forEachIndexed { index, file ->
        if (index in page.currentStartIndex until page.currentEndIndex) {
            result.add(file)
        }
    }

    return result
}