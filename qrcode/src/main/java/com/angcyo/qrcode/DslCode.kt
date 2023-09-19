package com.angcyo.qrcode

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.angcyo.fragment.dslBridge
import com.angcyo.library.L
import com.angcyo.library.ex.dpi
import com.angcyo.qrcode.code.toBarcodeConfig
import com.angcyo.rcode.RCode
import com.angcyo.rcode.ScanActivity
import com.angcyo.rcode.ScanFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

/**
 * 二维码扫码界面, 并获取扫码结果
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/28
 */

class DslCode {

    companion object {
        /**二维码时, 生成的图片默认宽高, 正方形*/
        val DEFAULT_CODE_SIZE = 100 * dpi

        /**条码时, 生成的图片默认宽高, 长方形*/
        val DEFAULT_CODE_WIDTH = 300 * dpi
        val DEFAULT_CODE_HEIGHT = 100 * dpi
    }

    /**承载[codeFragment]的[Activity]*/
    var codeActivity: Class<out ScanActivity>? = CodeScanActivity::class.java

    /**扫码[Fragment]*/
    var codeFragment: Class<out ScanFragment>? = CodeScanFragment::class.java

    var onConfigIntent: (Intent) -> Unit = {}

    /**二维码扫码返回数据, null 表示取消, 需要使用[FragmentActivity]启动才有效*/
    var onResult: (String?) -> Unit = {}

    fun doIt(activity: Activity?) {
        if (activity == null) {
            L.w("activity is null.")
        } else if (codeActivity != null) {
            val intent = Intent(activity, codeActivity)
            intent.putExtra(ScanActivity.KEY_TARGET, codeFragment)

            onConfigIntent(intent)

            if (activity is FragmentActivity) {
                dslBridge(activity.supportFragmentManager) {
                    startActivityForResult(intent) { resultCode, data ->
                        onResult(ScanActivity.onResult(0, resultCode, data))
                    }
                }
            } else {
                activity.startActivityForResult(intent, ScanActivity.REQUEST_CODE)
            }

        } else {
            L.w("codeActivity is invalid.")
        }
    }
}

/**快速启动二维码扫一扫界面, 并且获取返回值*/
fun dslCode(activity: Activity?, action: DslCode.() -> Unit) {
    DslCode().apply {
        action()
        doIt(activity)
    }
}

/**扩展*/
fun Fragment?.dslCode(action: DslCode.() -> Unit) {
    dslCode(this?.activity, action)
}

/**使用字符串, 创建二维码*/
fun CharSequence.createQRCode(
    size: Int = 100 * dpi,
    foregroundColor: Int = Color.BLACK,
    backgroundColor: Int = Color.WHITE,
    logo: Bitmap? = null,
    format: BarcodeFormat = BarcodeFormat.QR_CODE
): Bitmap? = RCode.syncEncodeCode(
    this.toString(),
    size,
    size,
    foregroundColor,
    backgroundColor,
    logo,
    format
)

/**使用字符串, 创建条形码, 不支持中文*/
fun CharSequence.createBarCode(
    width: Int = 300 * dpi,
    height: Int = 100 * dpi,
    foregroundColor: Int = Color.BLACK,
    backgroundColor: Int = Color.WHITE,
    logo: Bitmap? = null,
    format: BarcodeFormat = BarcodeFormat.CODE_128
): Bitmap? = RCode.syncEncodeCode(
    this.toString(),
    width,
    height,
    foregroundColor,
    backgroundColor,
    logo,
    format
)

/**创建一个条形码, 二维码是条形码的一种
 * [BarcodeFormat.MAXICODE] 不支持
 * [BarcodeFormat.RSS_14] 不支持
 * [BarcodeFormat.RSS_EXPANDED] 不支持
 * [BarcodeFormat.UPC_EAN_EXTENSION] 不支持
 * */
fun createBarcode(
    content: String?,
    width: Int,
    height: Int = width,
    format: BarcodeFormat = BarcodeFormat.QR_CODE,
    foregroundColor: Int = Color.BLACK,
    backgroundColor: Int = Color.WHITE,
    hints: Map<EncodeHintType, Any>? = RCode.HINTS
): Bitmap? = try {
    val matrix = MultiFormatWriter().encode(content, format, width, height, hints)
    val w = matrix.width
    val h = matrix.height
    val pixels = IntArray(w * h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val index = y * w + x
            if (matrix[x, y]) {
                pixels[index] = foregroundColor
            } else {
                pixels[index] = backgroundColor
            }
        }
    }
    val bitmap = Bitmap.createBitmap(
        w,
        h,
        if (backgroundColor.alpha != 255) Bitmap.Config.ARGB_4444 else Bitmap.Config.RGB_565
    )
    bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
    bitmap
} catch (e: Exception) {
    e.printStackTrace()
    null
}

/**当前的内容是否可以生成指定的条码格式*/
fun String.canCreateBarcode(content: String?) = toBarcodeConfig { }?.encode(content) != null

