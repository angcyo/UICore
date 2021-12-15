package com.angcyo.library.ex

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.annotation.ColorInt
import androidx.annotation.UiThread
import androidx.core.net.toUri
import androidx.core.text.getSpans
import com.angcyo.library.L
import com.angcyo.library.R
import com.angcyo.library.app
import com.angcyo.library.utils.PATTERN_EMAIL
import com.angcyo.library.utils.PATTERN_MOBILE_SIMPLE
import com.angcyo.library.utils.PATTERN_URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.regex.Matcher
import java.util.regex.Pattern


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
            throw  e
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

fun String.decode(enc: String = "UTF-8"): String = URLDecoder.decode(this, enc)

@ColorInt
fun String.toColorInt(): Int = this.toColor()

@ColorInt
fun String.toColor(): Int =
    if (startsWith("#")) Color.parseColor(this) else Color.parseColor("#$this")

fun CharSequence?.or(default: CharSequence = "--") =
    if (this.isNullOrEmpty()) default else this

fun CharSequence?.orString(default: CharSequence = "--"): String =
    if (this.isNullOrEmpty()) default.toString() else this.toString()

fun CharSequence?.toString(): String = orString("")

fun Any.toStr(): String = when (this) {
    is String -> this
    else -> toString()
}

fun String.wrapLog() = "\n${nowTimeString()} ${Thread.currentThread().name}\n${this}\n"

/**将列表连成字符串*/
fun Iterable<*>.connect(
    divide: CharSequence = "," /*连接符*/,
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
        safe()
    }
}

fun Array<*>.connect(
    divide: CharSequence = "," /*连接符*/,
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
        safe()
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
    } else if (this.toString().toLowerCase() == "null") {
    } else if (separator.isNullOrEmpty()) {
    } else {
        for (s in this.split(separator.toRegex(), Int.MAX_VALUE)) {
            if (s.isNullOrEmpty() && !allowEmpty) {
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

/**将base64字符串, 转换成图片*/
fun String.toBitmapOfBase64(): Bitmap {
    val bytes = Base64.decode(this, Base64.NO_WRAP)
    return bytes.toBitmap()
}

fun String.toBase64(): String {
    return Base64.encodeToString(toByteArray(), Base64.NO_WRAP).replace("\\+", "%2B")
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

/**url中的参数获取*/
fun String.queryParameter(key: String): String? {
    val uri = Uri.parse(this)
    return uri.getQueryParameter(key)
}

/**返回文件扩展名
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

/**获取url或者文件扩展名 对应的mimeType
 * https://www.iana.org/assignments/media-types/media-types.xhtml
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

fun CharSequence?.patternList(
    regex: String?,
    orNoFind: String? = null /*未找到时, 默认*/
): MutableList<String> {
    return this.patternList(regex?.toPattern(), orNoFind)
}

/**获取字符串中所有匹配的数据(部分匹配), 更像是contains的关系*/
fun CharSequence?.patternList(
    pattern: Pattern?,
    orNoFind: String? = null /*未找到时, 默认*/
): MutableList<String> {
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

fun CharSequence?.pattern(regex: String?, allowEmpty: Boolean = true): Boolean {
    return pattern(regex?.toPattern(), allowEmpty)
}

/**是否匹配成功(完成匹配)*/
fun CharSequence?.pattern(pattern: Pattern?, allowEmpty: Boolean = true): Boolean {

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
    return matcher.matches()
}

/**列表中的所有正则都匹配*/
fun CharSequence?.pattern(regexList: Iterable<String>, allowEmpty: Boolean = true): Boolean {
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

    // 有BUG的代码, if 条件永远不会为true, 虽然你已经给 result 赋值了true
    //    regexList.forEach {
    //        result = this.pattern(it, allowEmpty)
    //        if (result) {
    //            return@forEach
    //        }
    //    }

    regexList.forEach {
        //闭包外面声明的变量, 虽然已经修改了, 但是修改后的值, 不影响 if 条件判断
        if (this.pattern(it, allowEmpty)) {
            result = true
            return@forEach
        }
    }

    return result
}

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

/**host/url*/
fun String?.connectUrl(url: String?): String {
    val h = this?.trimEnd('/') ?: ""
    val u = url?.trimStart('/') ?: ""
    return "$h/$u"
}

fun String?.md5(): String? {
    return this?.toByteArray(Charsets.UTF_8)?.encrypt()?.toHexString()
}

/**字节大小*/
fun String.byteSize(): Int {
    return toByteArray(Charsets.UTF_8).size
}

/**[CharSequence]中是否包含指定[text]
 * [match] 是否是全匹配, 否则包含即可*/
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
        return null
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
 * 对错字符*/
fun Boolean.toDC() = if (this) "√" else "×"

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