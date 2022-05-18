package com.angcyo.library.ex

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
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
    return file()?.inputStream()?.bitmapSuffix() ?: "jpg"
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
        L.w(e)
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

fun String.bitmapSize(): IntArray {
    return file()?.inputStream()?.bitmapSize() ?: intArrayOf(-1, -1)
}

/**保存图片*/
fun Bitmap.save(
    path: String = filePath(PICTURE_FOLDER_NAME, fileNameUUID(".jpeg")),
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 90
): File {
    return path.file()!!.apply {
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

/**[rotate]纠正旋转角度*/
fun File.toBitmap(rotate: Boolean = true) = absolutePath.toBitmap(rotate)

/**将文件路径转换成[Bitmap]对象*/
fun String.toBitmap(rotate: Boolean = true): Bitmap {
    val file = File(this)
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