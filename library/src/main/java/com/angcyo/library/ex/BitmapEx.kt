package com.angcyo.library.ex

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.*
import com.angcyo.library.utils.Constant.PICTURE_FOLDER_NAME
import java.io.*
import kotlin.math.max
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */

fun configQQIntent(intent: Intent) {
//        intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");//微信朋友
//        intent.setClassName("com.tencent.mobileqq", "cooperation.qqfav.widget.QfavJumpActivity");//保存到QQ收藏
//        intent.setClassName("com.tencent.mobileqq", "cooperation.qlink.QlinkShareJumpActivity");//QQ面对面快传
//        intent.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.qfileJumpActivity");//传给我的电脑
    intent.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity")
    //QQ好友或QQ群
    //        intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");//微信朋友圈，仅支持分享图片
}

fun configWxIntent(intent: Intent) {
    intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");//微信朋友
}

fun ByteArray.toInputStream(): InputStream {
    return ByteArrayInputStream(this)
}

/**分享图片, 先保存图片, 然后通过uri分享*/
fun Bitmap.share(
    context: Context = app(),
    shareQQ: Boolean = false,
    shareWX: Boolean = false,
    chooser: Boolean = true,
    insertPhoto: Boolean = false,
): Boolean {
    val uri: Uri? = if (insertPhoto) {
        val insertImage =
            MediaStore.Images.Media.insertImage(context.contentResolver, this, null, null)
        if (insertImage == null) {
            //插入相册失败
            //写到本地
            save().toUri()
        } else {
            Uri.parse(insertImage)
        }
    } else {
        save().toUri()
    }

    if (uri == null) {
        return false
    }
    var intent = Intent()
    intent.action = Intent.ACTION_SEND //设置分享行为
    intent.type = "image/*" //设置分享内容的类型
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    //List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
    if (shareQQ) {
        configQQIntent(intent)
        if (chooser) {
            intent = Intent.createChooser(intent, "分享图片") //QQ分享
        }
    } else if (shareWX) {
        configWxIntent(intent)
        if (chooser) {
            intent = Intent.createChooser(intent, "分享图片") //WX分享
        }
    } else {
        intent = Intent.createChooser(intent, "分享图片")
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        toastQQ("未安装对应程序")
    }
    return true
}

/**模糊图片*/
fun Bitmap.blur(scale: Float = 1f /*0~1*/, radius: Float = 25f /*1~25*/): Bitmap? {
    return fastBlur(this, scale, radius)
}

/**将图片转成字节数组*/
fun Bitmap.toBytes(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): ByteArray? {
    var out: ByteArrayOutputStream? = null
    var bytes: ByteArray? = null
    try {
        out = ByteArrayOutputStream()
        this.compress(format, quality, out)
        out.flush()

        bytes = out.toByteArray()

        out.close()
    } finally {
        out?.close()
    }
    return bytes
}

/** 将图片转成base64字符串, 不包含 `data:image/` 开头
 * [toBase64Data] 包含 `data:image/` 开头
 * */
fun Bitmap.toBase64(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): String {
    var result = ""
    toBytes(format, quality)?.let {
        result = Base64.encodeToString(it, Base64.NO_WRAP /*去掉/n符*/)
    }
    return result
}

/**带协议头的数据
 * data:image/png;base64,xxx
 * [String.toBitmapOfBase64]
 * */
fun Bitmap.toBase64Data(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): String {
    val base64 = toBase64(format, quality)
    return "data:image/${if (format == Bitmap.CompressFormat.PNG) "png" else "jpeg"};base64,${base64}"
}

/**从流中获取图片类型*/
fun InputStream.bitmapSuffix(): String {
    return try {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(this, null, options)
        options.outMimeType.replace("image/", "")
    } catch (e: Exception) {
        L.w(e)
        "jpg"
    }
}

/**从流中获取图片类型*/
fun String.bitmapSuffix(): String {
    return file().inputStream().bitmapSuffix()
}

/**从流中获取图片宽高*/
fun InputStream.bitmapSize(): IntArray {
    val result = intArrayOf(-1, -1)
    try {
        use {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            options.inSampleSize = 1
            BitmapFactory.decodeStream(it, null, options)
            result[0] = options.outWidth
            result[1] = options.outHeight
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

/**从流中读取图片的旋转的角度*/
fun Uri.bitmapDegree(context: Context = app()) = inputStream(context)?.bitmapDegree() ?: 0

/**从流中读取图片的旋转的角度*/
fun InputStream.bitmapDegree(): Int {
    return try {
        val exifInterface = ExifInterface(this)
        val orientation: Int = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

/**
 * 读取图片的旋转的角度
 */
fun String.bitmapDegree(): Int {
    var degree = 0
    try {
        // 从指定路径下读取图片，并获取其EXIF信息
        val exifInterface = ExifInterface(this)
        degree = exifInterface.bitmapDegree()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return degree
}

fun ExifInterface.bitmapDegree(): Int {
    var degree = 0
    try {
        // 获取图片的旋转信息
        val orientation = getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return degree
}

/**获取图片宽高*/
fun String.bitmapSize(): IntArray {
    return file().bitmapSize()
}

/**获取图片宽高*/
fun File.bitmapSize(): IntArray {
    return inputStream().bitmapSize()
}

/**保存图片*/
fun Bitmap.save(
    path: String = filePath(PICTURE_FOLDER_NAME, fileNameUUID(".png")),
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, //JPEG才支持压缩
    quality: Int = 90, //PNG时,此属性无效.
    recycle: Boolean = false //保存完成之后, 是否自动回收图片
): File {
    return save(path.file(), format, quality, recycle)
}

/**保存图片
 * [save]*/
fun Bitmap.save(
    file: File,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, //JPEG才支持压缩
    quality: Int = 90, //PNG时,此属性无效.
    recycle: Boolean = false //保存完成之后, 是否自动回收图片
): File {
    return file.apply {
        outputStream().use {
            compress(format, quality, it)
        }
        if (recycle) {
            recycle()
        }
    }
}

/**将图片旋转指定角度, 旋转之后的图片没有透明像素了
 * [degrees] 需要旋转的角度[0-360]
 * */
fun Bitmap.rotate(degrees: Float = 0f): Bitmap = if (degrees != 0f) {
    val matrix = Matrix()
    matrix.setRotate(degrees)
    val rotatedBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    if (rotatedBitmap != this) {
        // 有时候 createBitmap会复用对象
        recycle()
    }
    rotatedBitmap.setHasAlpha(hasAlpha())//开启透明像素, 这样在保存成文件时, 旋转后的背景不会是黑色的
    rotatedBitmap
} else {
    this
}

/**变换图片*/
fun Bitmap.transform(matrix: Matrix?): Bitmap {
    if (matrix == null) return this
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/**缩放图片*/
fun Bitmap.scaleBitmap(sx: Int, sy: Int): Bitmap {
    return if (sx != 1 || sy != 1) scale(
        width * sx,
        height * sy
    ) else this
}

/**如果图片的宽/高小于指定的大小, 则将其缩放方法处理*/
fun Bitmap.scaleToMinSize(minWidth: Int, minHeight: Int): Bitmap {
    val sw = minWidth * 1f / width
    val sh = minHeight * 1f / height
    val scale = max(sw, sh)
    return if (scale > 1f) scale(
        (width * scale).floor().toInt(),
        (height * scale).floor().toInt()
    ) else this
}

/**如果图片的宽/高大于指定的大小, 则将其缩放方法处理*/
fun Bitmap.scaleToMaxSize(maxWidth: Int, maxHeight: Int): Bitmap {
    val sw = maxWidth * 1f / width
    val sh = maxHeight * 1f / height
    val scale = min(sw, sh)
    return if (scale < 1f) scale(
        (width * scale).floor().toInt(),
        (height * scale).floor().toInt()
    ) else this
}

/**水平/垂直翻转图片*/
fun Bitmap.flip(scaleX: Float, scaleY: Float): Bitmap {
    if (scaleX == 1f && scaleY == 1f) {
        return this
    }
    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, width / 2f, height / 2f)
    val flipBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    if (flipBitmap != this) {
        // 有时候 createBitmap会复用对象
        recycle()
    }
    flipBitmap.setHasAlpha(hasAlpha())//开启透明像素, 这样在保存成文件时, 旋转后的背景不会是黑色的
    return flipBitmap
}

fun InputStream.toBitmap(opts: BitmapFactory.Options? = null) =
    BitmapFactory.decodeStream(this, null, opts)

fun ByteArray.toBitmap(opts: BitmapFactory.Options? = null): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size, opts)
}

/**[rotate]纠正旋转角度*/
fun File.toBitmap(rotate: Boolean = true) = absolutePath.toBitmap(rotate)

/**将文件路径转换成[Bitmap]对象
 * 读取sd卡的文件, 需要权限
 * [android.Manifest.permission.READ_EXTERNAL_STORAGE]*/
fun String.toBitmap(rotate: Boolean = true): Bitmap? = try {
    val file = File(this)
    if (!file.exists()) {
        null
    } else {
        BitmapFactory.decodeFile(this).run {
            if (rotate) {
                rotate(file.exifOrientation().toFloat())
            } else {
                this
            }
        }
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

/**获取图片的旋转角度*/
fun File.exifOrientation() = try {
    val orientation =
        ExifInterface(this.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
} catch (e: Exception) {
    0
}

/**从[Resources]中获取[Bitmap]对象
 * [R.drawable.drawable.xxx]*/
fun Context.getBitmapFromRes(id: Int) = BitmapFactory.decodeResource(resources, id)

/**从[AssetManager]中获取[Bitmap]对象*/
fun Context.getBitmapFromAssets(name: String) = BitmapFactory.decodeStream(assets.open(name))

/**获取图片中的所有像素色值*/
fun Bitmap.getPixels(): IntArray {
    val w = width
    val h = height
    val pix = IntArray(w * h)
    getPixels(pix, 0, w, 0, 0, w, h)
    return pix
}

/**图片像素色值转换成图片*/
fun IntArray.toBitmap(width: Int, height: Int, config: Bitmap.Config? = null): Bitmap {
    return createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888).apply {
        setPixels(this@toBitmap, 0, width, 0, 0, width, height)
    }
}

/**色彩通道提取
 * [channelType] 通道类型 [Color.RED] [Color.GREEN] [Color.BLUE] [Color.GRAY]*/
fun Bitmap.colorChannel(
    channelType: Int = Color.RED,
    convert: (color: Int, channelColor: Int) -> Int = { _, channelColor -> channelColor }
): ByteArray {
    val width = width
    val height = height
    val result = ByteArray(width * height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = getPixel(x, y)
            var channelColor = when (channelType) {
                Color.RED -> Color.red(color)
                Color.GREEN -> Color.green(color)
                Color.BLUE -> Color.blue(color)
                Color.TRANSPARENT -> Color.alpha(color)
                else -> color.toGrayInt()//Color.GRAY
            }
            channelColor = convert(color, channelColor)
            channelColor = max(0, min(channelColor, 0xFF)) //限制0~255
            result[y * width + x] = channelColor.toByte()
        }
    }
    return result
}

/**[Canvas]
 * Canvas: trying to draw too large(1099123560bytes) bitmap.
 * 174584760
 * 174584760bytes
 * */
fun bitmapCanvas(
    width: Int,
    height: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888,
    action: Canvas.() -> Unit
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, config)
    val canvas = Canvas(bitmap)
    canvas.action()
    return bitmap
}

/**日志信息*/
fun Bitmap.logInfo(): String = buildString {
    append("图片宽:$width")
    append(" 高:$height")
    append(" :${byteCount.toSizeString()}")
}