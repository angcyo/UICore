package com.angcyo.library.ex

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.icu.text.Bidi
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.view.View
import android.webkit.MimeTypeMap
import androidx.annotation.ColorInt
import androidx.annotation.UiThread
import androidx.core.graphics.PathParser
import androidx.core.net.toUri
import androidx.core.text.BidiFormatter
import androidx.core.text.getSpans
import com.angcyo.library.L
import com.angcyo.library.R
import com.angcyo.library.app
import com.angcyo.library.component.lastContext
import com.angcyo.library.component.pool.acquireTempRect
import com.angcyo.library.component.pool.release
import com.angcyo.library.extend.IToText
import com.angcyo.library.libAppFile
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.LogFile
import com.angcyo.library.utils.PATTERN_EMAIL
import com.angcyo.library.utils.PATTERN_MOBILE_SIMPLE
import com.angcyo.library.utils.PATTERN_URL
import com.angcyo.library.utils.writeTo
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.BreakIterator
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.min


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**复制文本
 * 需要在主线程执行*/
@UiThread
fun CharSequence.copy(context: Context = app(), throwable: Boolean = false): Boolean {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    try {
        return try {
            clipboard.setPrimaryClip(ClipData.newPlainText("text", this))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    "text",
                    this/*.subSequence(0, 100).toString() + "...more"*/
                )
            )
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        if (throwable) {
            throw e
        }
        return false
    }
}

/**获取剪切板内容
 * 兼容性问题: 只有应用程序在前台的时候,才能读取剪切板*/
fun getPrimaryClip(context: Context = app()): CharSequence? {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val primaryClip = clipboard.primaryClip
        if (primaryClip != null && primaryClip.itemCount > 0) {
            return primaryClip.getItemAt(0).coerceToText(context)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun logClipboard(context: Context = app()): String {
    return buildString {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            clipboard.primaryClip?.apply {
                append("label:")
                appendln(description.label)
                for (i in 0 until itemCount) {
                    append("item:$i->")
                    append("mimeType:")
                    append(description.getMimeType(i))
                    appendln()

                    val item = getItemAt(i)
                    append("uri:")
                    append(item.uri)
                    appendln()

                    append("intent:")
                    append(item.intent)
                    appendln()

                    append("text:")
                    append(item.text)
                    appendln()

                    append("htmlText:")
                    append(item.htmlText)

                    appendln()
                    appendln()
                }
            }
        }
    }
}

fun String.encode(enc: String = "UTF-8"): String = URLEncoder.encode(this, enc)
fun String.urlEncode(enc: String = "UTF-8"): String = URLEncoder.encode(this, enc)

fun String.decode(enc: String = "UTF-8"): String = URLDecoder.decode(this, enc)

fun String.urlDecode(enc: String = "UTF-8"): String = URLDecoder.decode(this, enc)

@ColorInt
fun String.toColorInt(): Int = this.toColor()

/**颜色解析
 * if (startsWith("#")) Color.parseColor(this) else Color.parseColor("#$this")
 * */
@ColorInt
fun String.toColor(): Int {
    var colorString = this
    val a: Int
    var r: Int
    val g: Int
    var b = 0
    if (colorString.startsWith("#")) {
        colorString = colorString.substring(1)
    }
    if (colorString.isEmpty()) {
        r = 0
        a = 255
        g = 0
    } else if (colorString.length <= 2) {
        a = 255
        r = 0
        b = colorString.toInt(16)
        g = 0
    } else if (colorString.length == 3) {
        a = 255
        r = colorString.substring(0, 1).toInt(16)
        g = colorString.substring(1, 2).toInt(16)
        b = colorString.substring(2, 3).toInt(16)
    } else if (colorString.length == 4) {
        a = 255
        r = colorString.substring(0, 2).toInt(16)
        g = r
        r = 0
        b = colorString.substring(2, 4).toInt(16)
    } else if (colorString.length == 5) {
        a = 255
        r = colorString.substring(0, 1).toInt(16)
        g = colorString.substring(1, 3).toInt(16)
        b = colorString.substring(3, 5).toInt(16)
    } else if (colorString.length == 6) {
        a = 255
        r = colorString.substring(0, 2).toInt(16)
        g = colorString.substring(2, 4).toInt(16)
        b = colorString.substring(4, 6).toInt(16)
    } else if (colorString.length == 7) {
        a = colorString.substring(0, 1).toInt(16)
        r = colorString.substring(1, 3).toInt(16)
        g = colorString.substring(3, 5).toInt(16)
        b = colorString.substring(5, 7).toInt(16)
    } else if (colorString.length == 8) {
        a = colorString.substring(0, 2).toInt(16)
        r = colorString.substring(2, 4).toInt(16)
        g = colorString.substring(4, 6).toInt(16)
        b = colorString.substring(6, 8).toInt(16)
    } else {
        b = -1
        g = -1
        r = -1
        a = -1
    }
    return Color.argb(a, r, g, b)
}

fun CharSequence?.or(default: CharSequence = "--") =
    if (this.isNullOrEmpty()) default else this

fun CharSequence?.orString(default: CharSequence = "--"): String =
    if (this.isNullOrEmpty()) default.toString() else this.toString()

fun CharSequence?.toString(): String = orString("")

fun Any?.toStr(): String = when (this) {
    null -> ""
    is IToText -> toText().toStr()
    is Char -> java.lang.String.valueOf(this)
    is String -> this
    is Throwable -> stackTraceToString()
    else -> toString()
}

fun CharSequence.wrapLog() = "\n${nowTimeString()} ${Thread.currentThread().name}\n${this}\n"

/**写入日志*/
fun Any.writeLogTo(name: String = LogFile.error) {
    val str = toStr()
    if (name == LogFile.error) {
        L.e(str)
    }
    str.wrapLog().writeTo(libAppFile(name, Constant.LOG_FOLDER_NAME))
}

/**将列表连成字符串
 * [removeLast] 是否删除最后一个字符
 * */
fun <T> Iterable<T>.connect(
    divide: CharSequence = "," /*连接符*/,
    removeLast: Boolean = divide.isNotEmpty() /*是否删除最后一个字符*/,
    convert: (T) -> CharSequence? = { it.toStr() }
): String {
    return buildString {
        this@connect.forEach {
            it?.apply {
                val charSequence = convert(it)
                if (charSequence.isNullOrEmpty()) {

                } else {
                    append(charSequence).append(divide)
                }
            }
        }
        if (removeLast) {
            safe()
        }
    }
}

fun Array<*>.connect(
    divide: CharSequence = "," /*连接符*/,
    removeLast: Boolean = divide.isNotEmpty() /*是否删除最后一个字符*/,
    convert: (Any) -> CharSequence? = { it.toString() }
): String {
    return buildString {
        this@connect.forEach {
            it?.apply {
                val charSequence = convert(it)
                if (charSequence.isNullOrEmpty()) {

                } else {
                    append(charSequence).append(divide)
                }
            }
        }
        if (removeLast) {
            safe()
        }
    }
}

/**分割字符串*/
fun CharSequence?.splitList(
    separator: String = ",",
    allowEmpty: Boolean = false,
    checkExist: Boolean = false,
    maxCount: Int = Int.MAX_VALUE
): MutableList<String> {
    val result = mutableListOf<String>()

    if (this.isNullOrEmpty()) {
    } else if (this.toString().lowercase(Locale.getDefault()) == "null") {
    } else if (separator.isEmpty()) {
    } else {
        //for (s in this.split(separator.toRegex(), Int.MAX_VALUE)) {
        for (s in this.split(delimiters = *arrayOf(separator), false, Int.MAX_VALUE)) {
            if (s.isEmpty() && !allowEmpty) {
                continue
            }
            if (result.contains(s) && checkExist) {
                continue
            }

            result.add(s)

            if (result.size >= maxCount) {
                break
            }
        }
    }

    return result
}

/** 安全的去掉字符串的最后一个字符 */
fun CharSequence.safe(last: Char? = null): CharSequence {
    return if (last == null || endsWith(last)) {
        subSequence(0, kotlin.math.max(0, length - 1))
    } else {
        this
    }
}

/**删除最后一个字符*/
fun StringBuilder.safe(last: Char? = null): StringBuilder {
    return if (last == null || endsWith(last)) {
        delete(kotlin.math.max(0, lastIndex), kotlin.math.max(0, length))
    } else {
        this
    }
}

fun SpannableStringBuilder.safe(): SpannableStringBuilder {
    return delete(kotlin.math.max(0, lastIndex), kotlin.math.max(0, length))
}

/**判断字符串是否是纯数字, 支持正负整数,正负浮点数*/
fun String?.isNumber(regex: String = "^[-\\+]?[\\d.]*\\d+$"): Boolean {
    if (isNullOrEmpty()) {
        return false
    }
    val pattern = Pattern.compile(regex)
    return pattern.matcher(this).matches()
}

fun String?.isUrl(regex: String = PATTERN_URL): Boolean {
    if (isNullOrEmpty()) {
        return false
    }
    val pattern = Pattern.compile(regex)
    return pattern.matcher(this).matches()
}

/**判断字符串是否是手机号码*/
fun String?.isPhone(regex: String = PATTERN_MOBILE_SIMPLE): Boolean {
    if (this.isNullOrEmpty()) {
        return false
    }
    return matches(regex.toRegex())
}

/**判断字符串是否是邮箱地址*/
fun String?.isEmail(regex: String = PATTERN_EMAIL): Boolean {
    if (this.isNullOrEmpty()) {
        return false
    }
    return matches(regex.toRegex())
}

/**将base64字符串, 转换成图片
 * [Bitmap.toBase64Data]*/
fun String.toBitmapOfBase64(opts: BitmapFactory.Options? = null): Bitmap {
    var data = this
    if (startsWith("data:")) {
        val index = indexOf(",")
        if (index != -1) {
            data = substring(index + 1, length)
        }
    }
    val bytes = Base64.decode(data, Base64.NO_WRAP)
    return bytes.toBitmap(opts)
}

fun String.toBase64(): String {
    return Base64.encodeToString(toByteArray(Charsets.UTF_8), Base64.NO_WRAP).replace("\\+", "%2B")
}

fun String.fromBase64(): String {
    return Base64.decode(toByteArray(Charsets.UTF_8), Base64.NO_WRAP).toString(Charsets.UTF_8)
}

fun String?.toUri(): Uri? {
    return try {
        when {
            isFileExist() -> this?.file()?.toFileUri()
            isHttpScheme() -> Uri.parse(this)
            else -> Uri.parse(this)
        }
    } catch (e: Exception) {
        L.w(e)
        null
    }
}

/**
 * [keyType] 0:原样 >0:大写 <0:小写
 * */
fun Uri.queryParameterMap(keyType: Int = 0): Map<String, String?> {
    val map = hashMapOf<String, String?>()
    queryParameterNames?.forEach { key ->
        val value = getQueryParameter(key)
        map[when {
            keyType > 0 -> key.uppercase()
            keyType < 0 -> key.lowercase()
            else -> key
        }] = value
    }
    return map
}


/**将文本转换成[Path], 获取文本轮廓的[Path]*/
fun String.toTextPath(paint: Paint, result: Path = Path()): Path {
    val textBounds = acquireTempRect()
    paint.getTextBounds(this, 0, length, textBounds)
    paint.getTextPath(
        this, 0, length,
        -textBounds.left.toFloat(),
        -textBounds.top.toFloat(),
        result
    )
    textBounds.release()
    return result
}

/**url中的参数获取*/
fun String.queryParameter(key: String): String? {
    val uri = Uri.parse(this)
    return uri.getQueryParameter(key)
}

/**返回文件扩展名, 小写后缀名. 不包含`.`
 * [MimeTypeMap.getFileExtensionFromUrl]*/
fun String.ext(): String {  //=MimeTypeMap.getFileExtensionFromUrl(this.encode())
    // 修复 android.webkit.MimeTypeMap 的 getFileExtensionFromUrl 方法不支持中文的问题
    var url = this
    if (!TextUtils.isEmpty(url)) {
        val fragment = url.lastIndexOf('#')
        if (fragment > 0) {
            url = url.substring(0, fragment)
        }
        val query = url.lastIndexOf('?')
        if (query > 0) {
            url = url.substring(0, query)
        }
        val filenamePos = url.lastIndexOf('/')
        val filename = if (0 <= filenamePos) url.substring(filenamePos + 1) else url

        // if the filename contains special characters, we don't
        // consider it valid for our matching purposes:
        // 去掉正则表达式判断以添加中文支持
//          if (!filename.isEmpty() && Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename))
        if (filename.isNotEmpty()) {
            val dotPos = filename.lastIndexOf('.')
            if (0 <= dotPos) {
                // 后缀转为小写
                return filename.substring(dotPos + 1).toLowerCase()
            }
        }
    }
    return ""
}

fun String.extName(): String = ext()

/**获取不带扩展名的文件名*/
fun String.noExtName(): String {
    if (this.isNotEmpty()) {
        val dot = lastIndexOf('.')
        if (dot > -1 && dot < length) {
            return substring(0, dot)
        }
    }
    return this
}

/**\/后的名字*/
fun String.lastName(): String {
    if (this.isEmpty()) {
        return this
    }
    //包含?, 需要分割
    val index = lastIndexOf("?")
    if (index > -1) {
        return substring(lastIndexOf("/") + 1, index)
    }
    return substring(lastIndexOf("/") + 1, length)
}

/**
 * mimeType 转成对应的扩展名 txt
 * [text/plain] -> [txt]
 * [font/otf] -> [ttf]
 *
 * https://www.toutiao.com/article/7045075347141886467/
 * */
fun String?.mimeTypeToExtName() = MimeTypeMap.getSingleton().getExtensionFromMimeType(this)

/**获取url或者文件扩展名 对应的mimeType
 * https://www.iana.org/assignments/media-types/media-types.xhtml
 *
 * xxx.jpg->image/jpeg
 * xxx.log->null
 * xxx.zip->application/zip
 * */
fun String?.mimeType(): String? {
    //text/html
    return this?.run {
        /*val extension = if (this.isHttpScheme()) {
            "html"
        } else {
            val url = try {
                this.toUri().path?.encode()
            } catch (e: Exception) {
                this.encode()
            }
            //如果url中有+号, 返回值就会是空字符
            //MimeTypeMap.getFileExtensionFromUrl(url)
            url?.extName()
        }*/
        val url = try {
            this.toUri().path?.encode()
        } catch (e: Exception) {
            this.encode()
        }
        val extension = url?.extName()
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}

fun String?.isVideoMimeType(): Boolean {
    return this?.startsWith("video", true) ?: false
}

/**[text/html]*/
fun String?.isHttpMimeType(): Boolean {
    return this == "text/html" || (this?.split("?")?.getOrNull(0)?.endsWith("html", true) ?: false)
}

/**[android.media.MediaFile#isPlayListMimeType]*/
fun String?.isAudioMimeType(): Boolean {
    return this?.run {
        this.startsWith(
            "audio",
            true
        ) || this == "application/ogg"
    } ?: false
}

fun String?.isImageMimeType(): Boolean {
    return this?.startsWith("image", true) ?: false
}

fun String?.isTextMimeType(): Boolean {
    return this?.startsWith("text", true) ?: false
}

/**是否是图片类型
 * [File.fileType]
 * [com.angcyo.library.utils.FileType.getFileType]
 * */
fun String?.isImageType(): Boolean {
    val lc = this?.lowercase() ?: return false
    if (lc.mimeType()?.startsWith("image", true) == true) {
        return true
    }
    return lc.endsWith(".jpg", true) ||
            lc.endsWith(".jpeg", true) ||
            lc.endsWith(".png", true) ||
            lc.endsWith(".bmp", true) ||
            lc.endsWith(".webp", true)
}

/**是否是视频类型*/
fun String?.isVideoType(): Boolean {
    val lc = this?.lowercase() ?: return false
    if (lc.mimeType()?.startsWith("video", true) == true) {
        return true
    }
    return lc.endsWith(".mp4", true) ||
            lc.endsWith(".avi", true) ||
            lc.endsWith(".rmvb", true) ||
            lc.endsWith(".3gpp", true)
}

/**是否是音频类型*/
fun String?.isAudioType(): Boolean {
    val lc = this?.lowercase() ?: return false
    if (lc.mimeType()?.startsWith("audio", true) == true) {
        return true
    }
    return lc.endsWith(".mp3", true) ||
            lc.endsWith(".wmv", true) ||
            lc.endsWith(".wav", true) ||
            lc.endsWith(".amr", true)
}

/**是否是字体
 * https://developer.android.com/guide/topics/resources/font-resource?hl=zh-cn
 * */
fun String?.isFontType(): Boolean {
    val lc = this?.lowercase() ?: return false
    if (lc.mimeType()?.startsWith("font", true) == true) {
        return true
    }
    return lc.endsWith(".ttf") || lc.endsWith(".otf") || lc.endsWith(".ttc")
}

fun CharSequence?.patternList(
    regex: String?,
    orNoFind: String? = null /*未找到时, 默认*/
): List<String> {
    return this.patternList(regex?.toPattern(), orNoFind)
}

/**获取字符串中所有匹配的数据(部分匹配), 更像是contains的关系*/
fun CharSequence?.patternList(
    pattern: Pattern?,
    orNoFind: String? = null /*未找到时, 默认*/
): List<String> {
    val result = mutableListOf<String>()
    if (this == null) {
        return result
    }
    pattern?.let {
        val matcher = it.matcher(this)
        var isFind = false
        while (matcher.find()) {
            isFind = true
            result.add(matcher.group())
        }
        if (!isFind && orNoFind != null) {
            result.add(orNoFind)
        }
    }
    return result
}

/**
 * 正则表达式之获取中文
 * https://www.w3cschool.cn/regexp/nck51pqj.html*/
fun String?.getChineseList() = this?.patternList("[\u4e00-\u9fa5]+")

fun CharSequence?.pattern(
    regex: String?,
    allowEmpty: Boolean = true,
    contain: Boolean = false
): Boolean {
    return pattern(regex?.toPattern(), allowEmpty, contain)
}

/**是否匹配成功(完整匹配)
 * [contain] 是否模糊匹配, 否则就是完整匹配
 * [java.util.regex.Matcher.matches]
 * [java.util.regex.Matcher.find]
 * */
fun CharSequence?.pattern(
    pattern: Pattern?,
    allowEmpty: Boolean = true,
    contain: Boolean = false
): Boolean {

    if (TextUtils.isEmpty(this)) {
        if (allowEmpty) {
            return true
        }
    }

    if (this == null) {
        return false
    }
    if (pattern == null) {
        if (allowEmpty) {
            return true
        }
        return !TextUtils.isEmpty(this)
    }
    val matcher = pattern.matcher(this)
    return if (contain) {
        matcher.find()
    } else {
        matcher.matches()
    }
}

/**列表中的所有正则都匹配*/
fun CharSequence?.pattern(
    regexList: Iterable<String>,
    allowEmpty: Boolean = true,
    contain: Boolean = false
): Boolean {
    if (TextUtils.isEmpty(this)) {
        if (allowEmpty) {
            return true
        }
    }

    if (this == null) {
        return false
    }

    if (!regexList.iterator().hasNext()) {
        if (allowEmpty) {
            return true
        }
        return !TextUtils.isEmpty(this)
    }

    var result = false

    for (regex in regexList) {
        //闭包外面声明的变量, 虽然已经修改了, 但是修改后的值, 不影响 if 条件判断
        if (this.pattern(regex, allowEmpty, contain)) {
            result = true
            break
        }
    }

    return result
}

/**注意数据量的大小, 太大会崩*/
fun String.shareText(
    context: Context,
    title: String,
    shareQQ: Boolean = false /*强制使用QQ分享*/,
    chooser: Boolean = true
) {
    var intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    if (TextUtils.isEmpty(title)) {
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享")
        intent.putExtra(Intent.EXTRA_TEXT, this)
    } else {
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享：$title")
        intent.putExtra(Intent.EXTRA_TEXT, "$title $this")
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    //List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
    if (shareQQ) {
        configQQIntent(intent)
        if (chooser) {
            intent = Intent.createChooser(intent, "选择分享")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } else {
        context.startActivity(
            Intent.createChooser(intent, "选择分享")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

/**从指定位置开始, 移除Span*/
fun Spannable.clearSpans(start: Int = -1) {
    getSpans<Any>().forEach {
        val startPosition = getSpanStart(it)
        if (startPosition >= start) {
            removeSpan(it)
        }
    }
}

/**将类名转换成Class*/
fun String.toClass(): Class<*>? {
    return try {
        Class.forName(this)
    } catch (e: Exception) {
        null
    }
}

/**打开应用程序*/
fun String.openApp(flags: Int = if (this == app().packageName) Intent.FLAG_ACTIVITY_SINGLE_TOP else 0) =
    app().openApp(this, flags = flags)

/**拼接上另一个字符串*/
fun String?.connect(str: String?): String? {
    if (this == null || str == null) {
        return this
    }
    return "$this$str"
}

/**host/url*/
fun String?.connectUrl(url: String?): String {
    val h = this?.trimEnd('/') ?: ""
    val u = url?.trimStart('/') ?: ""
    return "$h/$u"
}

/**加密字节数据
 * [algorithm] 加密算法 MD2/MD5/SHA1/SHA224/SHA256/SHA384/SHA512
 * */
fun String?.toMd5(algorithm: String = "MD5") = md5(algorithm)

/**加密字节数据
 * [algorithm] 加密算法 MD2/MD5/SHA1/SHA224/SHA256/SHA384/SHA512
 * */
fun String?.md5(algorithm: String = "MD5"): String? {
    return this?.toByteArray(Charsets.UTF_8)?.encrypt(algorithm)?.toHexString()
}

/**文件路径转文件转md5值, 也就是获取文件的md5值*/
fun String?.fileMd5(algorithm: String = "MD5"): String? {
    return this?.file()?.md5(algorithm)
}

/**字节大小, 返回字节数量*/
fun String.byteSize(): Int {
    return toByteArray(Charsets.UTF_8).size
}

/**[CharSequence]中是否包含指定[text], 支持正则表达式
 * [match] 是否是全匹配, 否则包含即可
 *
 * [contains] 也可以使用 [String.contains], 但是不支持正则表达式
 * */
fun CharSequence?.have(text: CharSequence?, match: Boolean = false): Boolean {
    if (text == null) {
        return false
    }
    if (this == null) {
        return false
    }
    if (this.str() == text.str()) {
        return true
    }
    return try {
        val regex = text.toString().toRegex()
        if (match) {
            this.matches(regex)
        } else {
            this.contains(regex)
        }
    } catch (e: Exception) {
        //java.util.regex.PatternSyntaxException: Missing closing bracket in character class near index 19
        e.printStackTrace()
        false
    }
}

/**统计字符[char]出现的次数*/
fun CharSequence.count(char: Char) = count { it == char }

/**获取分割字符串[partition]之前的全部字符串*/
fun String.subStart(partition: String, ignoreCase: Boolean = true): String? {
    val indexOf = indexOf(partition, 0, ignoreCase)
    if (indexOf == -1) {
        return this
    }
    return substring(0, indexOf)
}

/**获取分割字符串[partition]之后的全部字符串*/
fun String.subEnd(
    partition: String,
    fromLast: Boolean = false,
    ignoreCase: Boolean = true
): String? {
    val indexOf = if (fromLast) lastIndexOf(partition, lastIndex, ignoreCase) else indexOf(
        partition,
        0,
        ignoreCase
    )
    if (indexOf == -1) {
        return null
    }
    return substring(indexOf + 1, length)
}

/**从指定的索引位置开始, 取指定数量的字符*/
fun CharSequence.subCount(startIndex: Int, count: Int) =
    subSequence(startIndex, min(startIndex + count, length))

fun String.base64Encode(): String =
    Base64.encodeToString(toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

fun String.base64Decoder(): String =
    Base64.decode(toByteArray(Charsets.UTF_8), Base64.NO_WRAP).toString(Charsets.UTF_8)

/**将字符串展示成密码形式[xx****xx]
 * [beforeCount] 首至少显示多少个字符
 * [afterCount] 尾至少显示多少个字符
 * [secureCount] 星星的数量
 * */
fun CharSequence.toSecureShow(
    beforeCount: Int = 2,
    afterCount: Int = 2,
    secureCount: Int = 4
): String {
    val p: StringBuilder = StringBuilder()
    for (i in 0 until secureCount) {
        p.append("*")
    }
    if (length < beforeCount + afterCount) {
        return p.toString()
    }
    val start = substring(0, kotlin.math.min(beforeCount, length))
    val end = substring(kotlin.math.max(length - afterCount, 0), length)
    return "$start$p$end"
}

/**重复字符多少次, 如果为负数, 清空字符*/
fun String.repeat2(n: Int): String {
    return if (n < 0) {
        ""
    } else {
        repeat(n)
    }
}

/**如果是负数, 则反向取值
 * 如果大于size, 则取模*/
fun String.getSafe(index: Int): Char? {
    val newIndex = if (index < 0) {
        length + index
    } else {
        index
    }
    val size = length
    if (newIndex >= size) {
        return getOrNull(newIndex % size)
    }
    return getOrNull(newIndex)
}

/**转换成对错字符显示
 * 对错字符
 * https://manual.toulan.fun/posts/macos-type-right-wrong-symbol/
 *
 * ✅
 * ❎
 * ❌
 * ✖
 * 红色
 * ✘
 * ✔︎
 * ✓
 * ✗
 * */
fun Boolean?.toDC() = if (this == true) "✔︎" else "✘" //if (this == true) "√" else "×"

/**从一堆字符串中, 获取到第一个不为空时返回*/
fun notEmptyOf(vararg str: String?): String {
    for (s in str) {
        if (!s.isNullOrEmpty()) {
            return s
        }
    }
    return ""
}

/**高亮匹配的文本*/
fun CharSequence.highlight(
    pattern: String,
    color: Int = _color(R.color.colorAccent)
): SpannableStringBuilder {

    if (TextUtils.isEmpty(this)) {
        return SpannableStringBuilder("")
    }

    if (TextUtils.isEmpty(pattern)) {
        return SpannableStringBuilder(this)
    }

    //关键代码
    val builder = SpannableStringBuilder(this)

    val p = Pattern.compile(pattern)
    val m: Matcher = p.matcher(this)
    while (m.find()) {
        val start = m.start()
        val end = m.end()
        builder.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return builder
}

/**移除指定类型的span*/
fun CharSequence.removeSpan(cls: Class<*>) {
    if (this is Spannable) {
        val spans = getSpans(0, length, Any::class.java)
        for (span in spans) {
            if (span.javaClass == cls) {
                this.removeSpan(span)
            }
        }
    }
}

/**转义正则特殊字符 （$()*+.[]?\^{},|）*/
fun CharSequence.escapeExprSpecialWord(): CharSequence {
    var keyword: String = this.toString()
    if (!TextUtils.isEmpty(this)) {
        val fbsArr = arrayOf("\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|")
        for (key in fbsArr) {
            if (keyword.contains(key)) {
                keyword = keyword.replace(key, "\\" + key)
            }
        }
    }
    return keyword
}

/**确保文件后缀*/
fun String.ensureName(end: String = ".json") = if (endsWith(end)) this else "$this${end}"

/**确保扩展名是[this],
 * 如果是.开头, 则直接追加, 否则自动加上.*/
fun String.ensureExtName() = ensurePrefix(".")

/**确保字符串前缀*/
fun String.ensurePrefix(prefix: String) = if (startsWith(prefix)) this else "$prefix$this"

/**确保字符串后缀*/
fun String.ensureSuffix(suffix: String) = if (endsWith(suffix)) this else "$this$suffix"

/**字符串的长度*/
fun String?.size() = this?.length ?: 0

/**首字母小写*/
fun CharSequence.lowerFirst(): CharSequence {
    if (length > 0) {
        val first = get(0)
        return "${first.lowercaseChar()}${subSequence(1, length)}"
    }
    return this
}

/**从url中获取文件名, 如果没有指定[attachment]则从url后面截取字符当做文件名
 * attachment;filename="百度手机助手(360手机助手).apk"
 * attachment;filename="baidusearch_AndroidPhone_1021446w.apk"
 * */
fun String?.getFileAttachmentName(attachment: String? = null): String? {
    val url = this
    var name: String? = null
    name = getFileNameFromAttachment(attachment)
    if (!name.isNullOrBlank()) {
        return name.trim('"')
    }

    name = getFileNameFromAttachment(url)
    if (!name.isNullOrBlank()) {
        return name.trim('"')
    }

    //最后一步, 截取url后面的文件名
    url?.split("?")?.getOrNull(0)?.run {
        val indexOf = lastIndexOf("/")
        if (indexOf != -1) {
            name = this.substring(indexOf + 1, this.length)
        }
    }
    return name?.trim('"')
}

/**
 * attachment; filename=YYB.998886.4c1b4029188a9b5f2ad007e997da02d4.1004112.apk
 * attachment;filename="baidusearch_AndroidPhone_1021446w.apk"
 * */
fun getFileNameFromAttachment(attachment: String?): String? {
    var name: String? = null
    attachment?.run {
        if (isNotEmpty()) {
            val decode = Uri.decode(this)

            //正则匹配filename
            decode.patternList("filename=\"(.*)\"").firstOrNull()?.run {
                name = this.split("filename=").getOrNull(1)

                if (!name.isNullOrBlank()) {
                    return name
                }
            }
            decode.patternList("filename=(.*)").firstOrNull()?.run {
                name = this.split("filename=").getOrNull(1)

                if (!name.isNullOrBlank()) {
                    return name
                }
            }

            //正则匹配name
            decode.patternList("name=\"(.*)\"").firstOrNull()?.run {
                name = this.split("name=").getOrNull(1)

                if (!name.isNullOrBlank()) {
                    return name
                }
            }
            decode.patternList("name=(.*)").firstOrNull()?.run {
                name = this.split("name=").getOrNull(1)

                if (!name.isNullOrBlank()) {
                    return name
                }
            }

            //uri查询
            Uri.parse(decode).run {
                //filename
                getQueryParameter("filename")?.run {
                    name = this

                    if (!name.isNullOrBlank()) {
                        return name
                    }
                }
                //name
                getQueryParameter("name")?.run {
                    name = this

                    if (!name.isNullOrBlank()) {
                        return name
                    }
                }
            }
        }
    }
    return name
}

/**[com.pixplicity.sharp.Sharp.loadPath]
 * [androidx.core.graphics.PathParser.createPathFromPathData]
 *
 * [M12 21.593c-5.63-5.539-11-10.297-11-14.402 0-3.791 3.068-5.191 5.281-5.191 1.312 0 4.151.501 5.719 4.457 1.59-3.968 4.464-4.447 5.726-4.447 2.54 0 5.274 1.621 5.274 5.181 0 4.069-5.136 8.625-11 14.402]
 * */
fun String.toPath(): Path? = PathParser.createPathFromPathData(this)

/**
 * https://developer.android.com/training/basics/supporting-devices/languages?hl=zh-cn
 *
 * Unicode 字符“从右到左标记”（U+200F）
 * */
fun CharSequence.toUnicodeWrap(): CharSequence {
    return bidiFormatter.unicodeWrap(this)
    //return "\u200F" + this
}

private val bidiFormatter: BidiFormatter by lazy {
    BidiFormatter.getInstance()
}

/**上下文环境是rtl*/
val isRtlContext: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        lastContext.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    } else {
        bidiFormatter.isRtlContext
    }

/**当前的字符编码方向, 大部分的字符方向
 * https://www.unicode.org/reports/tr9/
 * */
fun CharSequence.isRTL(): Boolean {
    return bidiFormatter.isRtl(this)
}

/**全部都是rtl字符*/
fun String.isFullRTL(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Bidi(this, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isRightToLeft
    } else {
        java.text.Bidi(this, java.text.Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isRightToLeft
    }
}

/**支持双向字符序列
 * [android.icu.text.Bidi]*/
fun CharSequence.reverseCharSequenceIfRtl(): String = toStr().reverseStringIfRtl()

/**
 * Android's ICU (International Components for Unicode) enhancements
 * https://icu.unicode.org/
 * */
fun CharSequence.toBreakString(): String {
    val result = mutableListOf<String>()
    val it = BreakIterator.getCharacterInstance()
    val text = toStr()
    it.setText(text)
    var start = it.first()
    var end = it.next()
    while (end != BreakIterator.DONE) {
        val grapheme = text.substring(start, end)
        result.add(grapheme)
        start = end
        end = it.next()
    }
    return result.connect("")
}

/**[CharSequence.toBreakString]*/
inline fun String.forEachBreak(action: (String) -> Unit) {
    val it = BreakIterator.getCharacterInstance()
    it.setText(this)
    var start = it.first()
    var end = it.next()
    while (end != BreakIterator.DONE) {
        val grapheme = substring(start, end)
        action(grapheme)
        start = end
        end = it.next()
    }
}

/**[CharSequence.toBreakString]*/
inline fun String.forEachBreakIndexed(action: (Int, String) -> Unit) {
    val it = BreakIterator.getCharacterInstance()
    it.setText(this)
    var start = it.first()
    var end = it.next()
    var index = 0
    while (end != BreakIterator.DONE) {
        val grapheme = substring(start, end)
        action(index++, grapheme)
        start = end
        end = it.next()
    }
}


/**如果字符方向是RTL, 则反序字符串
 * [android.icu.text.Bidi]
 * */
fun String.reverseStringIfRtl(): String {
    if (isRTL()) {
        val result = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bidi = Bidi(
                this,
                if (bidiFormatter.isRtlContext) Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT else Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT
            )
            val count = bidi.runCount

            for (i in 0 until count) {
                val start = bidi.getRunStart(i)
                val end = bidi.getRunLimit(i)
                val level = bidi.getRunLevel(i)
                val run = substring(start, end)
                //L.i("Run: $run, Level: $level")
                if (run.isRTL()) {
                    result.add(run.reversed())
                } else {
                    result.add(run)
                }
            }
        } else {
            val bidi = java.text.Bidi(
                this,
                if (bidiFormatter.isRtlContext) java.text.Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT else java.text.Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT
            )
            val count = bidi.runCount

            for (i in 0 until count) {
                val start = bidi.getRunStart(i)
                val end = bidi.getRunLimit(i)
                val level = bidi.getRunLevel(i)
                val run = substring(start, end)
                //L.i("Run: $run, Level: $level")
                if (run.isRTL()) {
                    result.add(run.reversed())
                } else {
                    result.add(run)
                }
            }
        }
        result.reverse()
        return result.connect("")
    }
    return this
}

/**使用正则替换字符串, 文件名中不能[\/:*?"<>|]这些字符
 *
 * ```
 * "grid[112:*234]\\:/:*\"?<>|[end]".replace("[\\/:*?\"<>|]", "")
 * ```
 *
 * [regex] 正则表达式
 * [replacement] 需要替换的字符串
 * @return 返回一个新字符串*/
fun String.replace(regex: String?, replacement: String) =
    if (regex.isNullOrEmpty()) this else replace(regex.toRegex(), replacement)