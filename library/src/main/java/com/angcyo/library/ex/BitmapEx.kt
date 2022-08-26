package com.angcyo.library.ex

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.Constant.PICTURE_FOLDER_NAME
import com.angcyo.library.utils.fastBlur
import com.angcyo.library.utils.fileNameUUID
import com.angcyo.library.utils.filePath
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

/**
 * 将图片转成base64字符串
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

/**
 * data:image/png;base64,xxx
 * [com.angcyo.library.ex.StringExKt.toBitmapOfBase64]
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
    quality: Int = 90 //PNG时,此属性无效.
): File {
    return path.file().apply {
        outputStream().use {
            compress(format, quality, it)
        }
    }
}

/**将图片旋转指定角度
 * [degrees] 需要旋转的角度[0-360]
 * */
fun Bitmap.rotate(degrees: Float = 0f): Bitmap = if (degrees > 0) {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    val rotatedBitmap = Bitmap.createBitmap(
        this,
        0, 0,
        width, height,
        matrix,
        false
    )
    if (rotatedBitmap != rotatedBitmap) {
        // 有时候 createBitmap会复用对象
        recycle()
    }
    rotatedBitmap
} else {
    this
}

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

/**[rotate]纠正旋转角度*/
fun File.toBitmap(rotate: Boolean = true) = absolutePath.toBitmap(rotate)

/**将文件路径转换成[Bitmap]对象*/
fun String.toBitmap(rotate: Boolean = true): Bitmap? {
    val file = File(this)
    if (!file.exists()) {
        return null
    }
    return BitmapFactory.decodeFile(this).run {
        if (rotate) {
            rotate(file.exifOrientation().toFloat())
        } else {
            this
        }
    }
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

/**色彩通道提取
 * [channelType] 通道类型 [Color.RED] [Color.GREEN] [Color.BLUE]*/
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
                else -> 0xFF
            }
            channelColor = convert(color, channelColor)
            channelColor = max(0, min(channelColor, 0xFF)) //限制0~255
            result[y * width + x] = channelColor.toByte()
        }
    }
    return result
}

/**枚举通道颜色
 * [channelType] 通道类型 [Color.RED] [Color.GREEN] [Color.BLUE]*/
fun Bitmap.eachColorChannel(
    channelType: Int = Color.RED,
    action: (wIndex: Int, hIndex: Int, color: Int) -> Unit = { _, _, _ -> }
) {
    val width = width
    val height = height

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = getPixel(x, y)
            val channelColor = when (channelType) {
                Color.RED -> Color.red(color)
                Color.GREEN -> Color.green(color)
                Color.BLUE -> Color.blue(color)
                Color.TRANSPARENT -> Color.alpha(color)
                else -> 0xFF
            }
            action(x, y, channelColor)
        }
    }
}

/**将图片转灰度
 * [alphaBgColor] 透明像素时的替换颜色*/
fun Bitmap.grayHandle(alphaBgColor: Int = Color.TRANSPARENT): Bitmap {
    val width = width
    val height = height
    val result = Bitmap.createBitmap(width, height, config)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = getPixel(x, y)

            if (color == Color.TRANSPARENT) {
                //透明颜色
                result.setPixel(x, y, alphaBgColor)
            } else {
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                var value = (r + g + b) / 3
                value = max(0, min(value, 255)) //限制0~255
                result.setPixel(x, y, Color.rgb(value, value, value))
            }
        }
    }
    return result
}

/**将图片转灰度, 并且返回一张没有透明像素的图片
 * [alphaBgColor] 透明像素时的替换颜色*/
fun Bitmap.grayHandleAlpha(alphaBgColor: Int = Color.TRANSPARENT): Array<Bitmap> {
    val width = width
    val height = height

    val resultBitmap = Bitmap.createBitmap(width, height, config)
    val alphaBitmap = Bitmap.createBitmap(width, height, config)

    val result = arrayOf(resultBitmap, alphaBitmap)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = getPixel(x, y)

            if (color == Color.TRANSPARENT) {
                //透明颜色
                resultBitmap.setPixel(x, y, alphaBgColor)
                alphaBitmap.setPixel(x, y, Color.WHITE)//默认白色背景
            } else {
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                var value = (r + g + b) / 3
                value = max(0, min(value, 255)) //限制0~255

                resultBitmap.setPixel(x, y, Color.rgb(value, value, value))
                alphaBitmap.setPixel(x, y, Color.rgb(r, g, b))
            }
        }
    }
    return result
}

/**将图片转黑白
 * [threshold] 阈值, [0~255] [黑色~白色] 大于这个值的都是白色
 * [invert] 反色, 是否要将黑白颜色颠倒
 * [alphaBgColor] 透明像素时的替换颜色*/
fun Bitmap.blackWhiteHandle(
    threshold: Int = 120,
    invert: Boolean = false,
    alphaBgColor: Int = Color.TRANSPARENT
): Bitmap {
    val width = width
    val height = height
    val result = Bitmap.createBitmap(width, height, config)

    for (y in 0 until height) {
        for (x in 0 until width) {
            var color = getPixel(x, y)

            if (color == Color.TRANSPARENT) {
                //透明颜色
                color = alphaBgColor
            }

            if (color == Color.TRANSPARENT) {
                //依旧是透明
                result.setPixel(x, y, color)
            } else {
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                var value = (r + g + b) / 3
                value = max(0, min(value, 255)) //限制0~255

                value = if (value >= threshold) {
                    //白色
                    if (invert) {
                        0x00
                    } else {
                        0xff
                    }
                } else {
                    //黑色
                    if (invert) {
                        0xff
                    } else {
                        0x00
                    }
                }

                result.setPixel(x, y, Color.rgb(value, value, value))
            }
        }
    }
    return result
}

/**额外返回一张没有透明背景的图片*/
fun Bitmap.blackWhiteHandleAlpha(
    threshold: Int = 120,
    invert: Boolean = false,
    alphaBgColor: Int = Color.TRANSPARENT
): Array<Bitmap> {
    val width = width
    val height = height

    val resultBitmap = Bitmap.createBitmap(width, height, config)
    val alphaBitmap = Bitmap.createBitmap(width, height, config)

    val result = arrayOf(resultBitmap, alphaBitmap)

    for (y in 0 until height) {
        for (x in 0 until width) {
            var color = getPixel(x, y)

            if (color == Color.TRANSPARENT) {
                //透明颜色
                color = alphaBgColor
            }

            if (color == Color.TRANSPARENT) {
                //依旧是透明
                resultBitmap.setPixel(x, y, color)
                alphaBitmap.setPixel(x, y, Color.WHITE)//默认白色背景
            } else {
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                var value = (r + g + b) / 3
                value = max(0, min(value, 255)) //限制0~255

                value = if (value >= threshold) {
                    //白色
                    if (invert) {
                        0x00
                    } else {
                        0xff
                    }
                } else {
                    //黑色
                    if (invert) {
                        0xff
                    } else {
                        0x00
                    }
                }

                resultBitmap.setPixel(x, y, Color.rgb(value, value, value))
                alphaBitmap.setPixel(x, y, Color.rgb(r, g, b))
            }
        }
    }
    return result
}

/**色彩通道转换[Bitmap]可视化对象*/
fun ByteArray.toChannelBitmap(width: Int, height: Int, channelType: Int = Color.RED): Bitmap {
    val channelBitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(channelBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = if (channelType == Color.TRANSPARENT) Color.BLACK else channelType
        this.style = Paint.Style.FILL
        strokeWidth = 1f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    val bytes = this
    for (y in 0 until height) {
        for (x in 0 until width) {
            val value: Int = bytes[y * width + x].toHexInt()
            paint.color = Color.argb(
                if (channelType == Color.TRANSPARENT) value else 255,
                if (channelType == Color.RED) value else 0,
                if (channelType == Color.GREEN) value else 0,
                if (channelType == Color.BLUE) value else 0
            )
            canvas.drawCircle(x.toFloat(), y.toFloat(), 1f, paint)//绘制圆点
        }
    }
    return channelBitmap
}

/**
 * 逐行扫描 清除边界空白
 *
 * @param margin 边距留多少个像素
 * @param color 需要移除的边界颜色值
 * @return 清除边界后的Bitmap
 */
fun Bitmap.trimEdgeColor(color: Int = Color.TRANSPARENT, margin: Int = 0): Bitmap {
    var blank = margin

    val height = height
    val width = width
    var widthPixels = IntArray(width)
    var isStop: Boolean

    var top = 0
    var left = 0
    var right = 0
    var bottom = 0

    //top
    for (y in 0 until height) {
        getPixels(widthPixels, 0, width, 0, y, width, 1)
        isStop = false
        for (pix in widthPixels) {
            if (pix != color) {
                top = y
                isStop = true
                break
            }
        }
        if (isStop) {
            break
        }
    }

    //bottom
    for (y in height - 1 downTo 0) {
        getPixels(widthPixels, 0, width, 0, y, width, 1)
        isStop = false
        for (pix in widthPixels) {
            if (pix != color) {
                bottom = y
                isStop = true
                break
            }
        }
        if (isStop) {
            break
        }
    }

    //left
    widthPixels = IntArray(height)
    for (x in 0 until width) {
        getPixels(widthPixels, 0, 1, x, 0, 1, height)
        isStop = false
        for (pix in widthPixels) {
            if (pix != color) {
                left = x
                isStop = true
                break
            }
        }
        if (isStop) {
            break
        }
    }
    //right
    for (x in width - 1 downTo 1) {
        getPixels(widthPixels, 0, 1, x, 0, 1, height)
        isStop = false
        for (pix in widthPixels) {
            if (pix != color) {
                right = x
                isStop = true
                break
            }
        }
        if (isStop) {
            break
        }
    }
    if (blank < 0) {
        blank = 0
    }
    //
    left = if (left - blank > 0) left - blank else 0
    top = if (top - blank > 0) top - blank else 0
    right = if (right + blank > width - 1) width - 1 else right + blank
    bottom = if (bottom + blank > height - 1) height - 1 else bottom + blank
    //
    return Bitmap.createBitmap(this, left, top, right - left, bottom - top)
}

/**[Canvas]*/
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